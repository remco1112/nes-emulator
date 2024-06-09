package org.example.nes.apu;

import java.util.Set;

abstract class Channel {
    final int tick(Set<FrameCounter.ClockResult> clockResult) {
        final int result = isSilenced() ? 0 : getVolume();
        onCpuTick();
        if (clockResult.contains(FrameCounter.ClockResult.APU)) {
            onApuTick();
        }
        if (clockResult.contains(FrameCounter.ClockResult.QUARTER)) {
            onQuarterFrameTick();
        }
        if (clockResult.contains(FrameCounter.ClockResult.HALF)) {
            onHalfFrameTick();
        }
        return result;
    }

    void writeRegister0(byte value) {

    }

    void writeRegister1(byte value) {

    }

    void writeRegister2(byte value) {

    }

    void writeRegister3(byte value) {

    }

    void onCpuTick() {

    }

    void onApuTick() {

    }

    void onQuarterFrameTick() {

    }

    void onHalfFrameTick() {

    }

    boolean isSilenced() {
        return false;
    }

    abstract int getVolume();
}
