package org.example.nes.apu;

abstract class WaveChannel<W extends WaveGenerator> extends Channel {
    private final LengthCounter lengthCounter = new LengthCounter();
    protected final W waveGenerator;

    WaveChannel(W waveGenerator) {
        this.waveGenerator = waveGenerator;
    }

    @Override
    void writeRegister0(byte value) {
        if (waveGenerator instanceof EnvelopeWaveGenerator) {
            ((EnvelopeWaveGenerator) waveGenerator).configureVolume(value);
            setLengthCounterHalted((value & 0x20) != 0);
        }
    }

    @Override
    void writeRegister3(byte value) {
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
    void onQuarterFrameTick() {
        super.onQuarterFrameTick();
        if (waveGenerator instanceof EnvelopeWaveGenerator) {
            ((EnvelopeWaveGenerator) waveGenerator).tickEnvelope();
        }
    }

    @Override
    final int getVolume() {
        return waveGenerator.getCurrentValue();
    }
}
