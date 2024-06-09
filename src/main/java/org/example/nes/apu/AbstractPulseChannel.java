package org.example.nes.apu;

import static org.example.nes.utils.UInt.toUint;

abstract class AbstractPulseChannel extends EnvelopeChannel {
    private final PulseWaveGenerator pulseWaveGenerator = new PulseWaveGenerator();
    private final PulseTimer pulseTimer = new PulseTimer();
    private final AbstractSweepUnit sweepUnit;

    AbstractPulseChannel(AbstractSweepUnit sweepUnit) {
        this.sweepUnit = sweepUnit;
    }

    @Override
    void writeVolumeRegister(byte value) {
        super.writeVolumeRegister(value);
        pulseWaveGenerator.setDuty(toUint(value) >>> 6);
    }

    void writeSweepRegister(byte value) {
        final boolean enabled = (value & 0x80) != 0;
        final int dividerPeriod = (value & 0x70) >>> 4;
        final boolean negate = (value & 0x4) != 0;
        final int shiftCount = value & 0x3;

        sweepUnit.configure(enabled, dividerPeriod, negate, shiftCount);
    }

    void writeTimerLowRegister(byte value) {
        pulseTimer.setPeriod((pulseTimer.getPeriod() & 0x700) | toUint(value));
    }

    @Override
    void writeLengthCounterRegister(byte value) {
        super.writeLengthCounterRegister(value);
        pulseTimer.setPeriod((pulseTimer.getPeriod() & 0xFF) | ((value & 0x7) << 8));
        pulseWaveGenerator.resetPeriod();
    }

    @Override
    boolean isSilenced() {
        return super.isSilenced()
                || !pulseWaveGenerator.isWaveHigh()
                || sweepUnit.isMuted(pulseTimer.getPeriod())
                || !pulseTimer.isActive();
    }

    @Override
    void onApuTick() {
        super.onApuTick();
        if (pulseTimer.tick()) {
            pulseWaveGenerator.tick();
        }
    }

    @Override
    void onHalfFrameTick() {
        super.onHalfFrameTick();
        int newPeriod = sweepUnit.tick(pulseTimer.getPeriod());
        if (newPeriod != -1) {
            pulseTimer.setPeriod(newPeriod);
        }
    }
}
