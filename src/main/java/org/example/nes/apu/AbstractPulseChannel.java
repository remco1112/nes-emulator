package org.example.nes.apu;

abstract class AbstractPulseChannel extends StandardPeriodTimerWaveChannel<WaveGenerator> {
    private final AbstractSweepUnit sweepUnit;

    AbstractPulseChannel(AbstractSweepUnit sweepUnit) {
        super(new PulseTimer(), new PulseWaveGenerator(), false);
        this.sweepUnit = sweepUnit;
    }

    @Override
    void writeRegister1(byte value) {
        final boolean enabled = (value & 0x80) != 0;
        final int dividerPeriod = (value & 0x70) >>> 4;
        final boolean negate = (value & 0x4) != 0;
        final int shiftCount = value & 0x3;

        sweepUnit.configure(enabled, dividerPeriod, negate, shiftCount);
    }

    @Override
    boolean isSilenced() {
        return super.isSilenced() || sweepUnit.isMuted(getTimerPeriod());
    }

    @Override
    void onHalfFrameTick() {
        super.onHalfFrameTick();
        int newPeriod = sweepUnit.tick(getTimerPeriod());
        if (newPeriod != -1) {
            setTimerPeriod(newPeriod);
        }
    }
}
