package org.example.nes.apu;

abstract class EnvelopeWaveGenerator extends WaveGenerator {
    private final EnvelopeGenerator envelopeGenerator = new EnvelopeGenerator();

    public void configureVolume(byte value) {
        final int volume = value & 0xF;
        final boolean constant  = (value & 0x10) != 0;
        final boolean loop = (value & 0x20) != 0;

        envelopeGenerator.configure(volume, constant, loop);
    }

    protected final void tickEnvelope() {
        envelopeGenerator.tick();
    }

    @Override
    protected void reset() {
        envelopeGenerator.setStart();
    }

    @Override
    public final int getCurrentValue() {
        return isEnvelopeMuted() ? 0 : envelopeGenerator.getVolume();
    }

    protected abstract boolean isEnvelopeMuted();
}
