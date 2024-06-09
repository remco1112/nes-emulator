package org.example.nes.apu;

abstract class WaveChannel<T extends WaveGenerator> extends Channel {
    private final LengthCounter lengthCounter = new LengthCounter();
    protected final T waveGenerator;

    WaveChannel(T waveGenerator) {
        this.waveGenerator = waveGenerator;
    }

    void writeLengthCounterRegister(byte value) {
        lengthCounter.set((value & 0xF8) >>> 3);
    }

    void setEnabled(boolean enabled) {
        lengthCounter.setEnabled(enabled);
    }

    void setLengthCounterHalted(boolean halted) {
        lengthCounter.setHalted(halted);
    }

    @Override
    boolean isSilenced() {
        return super.isSilenced() || lengthCounter.isZero();
    }

    @Override
    void onHalfFrameTick() {
        super.onHalfFrameTick();
        lengthCounter.tick();
    }

    @Override
    final int getVolume() {
        return waveGenerator.getCurrentValue();
    }
}
