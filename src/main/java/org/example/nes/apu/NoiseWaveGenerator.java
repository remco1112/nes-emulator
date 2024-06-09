package org.example.nes.apu;

class NoiseWaveGenerator extends EnvelopeWaveGenerator {
    private int bitSelect = 1;
    private int shiftRegister = 1;

    void configure(boolean mode) {
        bitSelect = mode ? 6 : 1;
    }

    @Override
    public void tick() {
        shiftRegister = (((shiftRegister & 0x1) ^ ((shiftRegister >>> bitSelect) & 0x1)) << 14) | (shiftRegister >>> 1);
    }

    @Override
    protected boolean isEnvelopeMuted() {
        return (shiftRegister & 0x1) != 0;
    }
}
