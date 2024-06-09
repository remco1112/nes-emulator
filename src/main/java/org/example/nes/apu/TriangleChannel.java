package org.example.nes.apu;

class TriangleChannel extends StandardPeriodTimerWaveChannel<WaveGenerator> {
    private final LinearCounter linearCounter = new LinearCounter();

    TriangleChannel() {
        super(new Timer(), new TriangleWaveGenerator(), true);
    }

    @Override
    void writeRegister0(byte value) {
        super.writeRegister0(value);
        final int reloadValue = value & 0x7F;
        final boolean control = (value & 0x80) != 0;
        linearCounter.configure(control, reloadValue);
        setLengthCounterHalted(control);
    }

    @Override
    void writeRegister3(byte value) {
        super.writeRegister3(value);
        linearCounter.setReload();
    }

    @Override
    void onQuarterFrameTick() {
        super.onQuarterFrameTick();
        linearCounter.tick();
    }

    @Override
    protected boolean shouldTickWaveGenerator() {
        return !isSilenced();
    }

    @Override
    boolean isSilenced() {
        return super.isSilenced() || linearCounter.isZero();
    }
}
