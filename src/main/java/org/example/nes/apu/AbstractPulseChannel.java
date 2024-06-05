package org.example.nes.apu;

import java.util.Set;

import static org.example.nes.utils.UInt.toUint;

abstract class AbstractPulseChannel {

    private final EnvelopeGenerator envelopeGenerator = new EnvelopeGenerator();
    private final PulseWaveGenerator pulseWaveGenerator = new PulseWaveGenerator();
    private final LengthCounter lengthCounter = new LengthCounter();
    private final AbstractSweepUnit sweepUnit;

    AbstractPulseChannel(AbstractSweepUnit sweepUnit) {
        this.sweepUnit = sweepUnit;
    }

    void writeVolumeRegister(byte value) {
        final int volume = value & 0xF;
        final boolean constant  = (value & 0x10) != 0;
        final boolean loopHalt = (value & 0x20) != 0;
        final int duty = toUint(value) >>> 6;

        envelopeGenerator.configure(volume, constant, loopHalt);
        lengthCounter.setHalted(loopHalt);
        pulseWaveGenerator.setDuty(duty);
    }

    void writeSweepRegister(byte value) {
        final boolean enabled = (value & 0x80) != 0;
        final int dividerPeriod = (value & 0x70) >>> 4;
        final boolean negate = (value & 0x4) != 0;
        final int shiftCount = value & 0x3;

        sweepUnit.configure(enabled, dividerPeriod, negate, shiftCount);
    }

    void writeTimerLowRegister(byte value) {
        pulseWaveGenerator.setTimer((pulseWaveGenerator.getTimerPeriod() & 0x700) | toUint(value));
    }

    void writeLengthCounterRegister(byte value) {
        pulseWaveGenerator.setTimer((pulseWaveGenerator.getTimerPeriod() & 0xFF) | ((value & 0x7) << 8));
        pulseWaveGenerator.resetPeriod();

        lengthCounter.setLength((value & 0xF8) >>> 3);
        envelopeGenerator.setStart();
    }

    void setEnabled(boolean enabled) {
        lengthCounter.setEnabled(enabled);
    }

    int tick(Set<FrameCounter.ClockResult> clockResult) {
        final int result = emitEnvelopeVolume() ? envelopeGenerator.getVolume() : 0;
        if (clockResult.contains(FrameCounter.ClockResult.APU)) {
            pulseWaveGenerator.tick();
        }
        if (clockResult.contains(FrameCounter.ClockResult.QUARTER)) {
            envelopeGenerator.tick();
        }
        if (clockResult.contains(FrameCounter.ClockResult.HALF)) {
            lengthCounter.tick();
            int newPeriod = sweepUnit.tick(pulseWaveGenerator.getTimerPeriod());
            if (newPeriod != -1) {
                pulseWaveGenerator.setTimer(newPeriod);
            }
        }
        return result;
    }

    private boolean emitEnvelopeVolume() {
        return pulseWaveGenerator.isWaveHigh()
                && !sweepUnit.isMuted(pulseWaveGenerator.getTimerPeriod())
                && !lengthCounter.isMuted();
    }
}
