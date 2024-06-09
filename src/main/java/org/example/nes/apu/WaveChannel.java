package org.example.nes.apu;

abstract class WaveChannel extends Channel {
    private final LengthCounter lengthCounter = new LengthCounter();

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
}
