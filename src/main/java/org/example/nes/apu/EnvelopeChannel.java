package org.example.nes.apu;

abstract class EnvelopeChannel extends WaveChannel {
    private final EnvelopeGenerator envelopeGenerator = new EnvelopeGenerator();

    void writeVolumeRegister(byte value) {
        final int volume = value & 0xF;
        final boolean constant  = (value & 0x10) != 0;
        final boolean loopHalt = (value & 0x20) != 0;

        envelopeGenerator.configure(volume, constant, loopHalt);
        setLengthCounterHalted(loopHalt);
    }

    @Override
    void writeLengthCounterRegister(byte value) {
        super.writeLengthCounterRegister(value);
        envelopeGenerator.setStart();
    }

    @Override
    int getVolume() {
        return envelopeGenerator.getVolume();
    }

    @Override
    void onQuarterFrameTick() {
        super.onQuarterFrameTick();
        envelopeGenerator.tick();
    }
}
