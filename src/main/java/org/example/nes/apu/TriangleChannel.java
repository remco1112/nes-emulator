package org.example.nes.apu;

import static org.example.nes.utils.UInt.toUint;

class TriangleChannel extends WaveChannel {
    private final LinearCounter linearCounter = new LinearCounter();
    private final Timer timer = new Timer();
    private final TriangleWaveGenerator triangleWaveGenerator = new TriangleWaveGenerator();

    void writeLinearCounterRegister(byte value) {
        final int reloadValue = value & 0x7F;
        final boolean control = (value & 0x80) != 0;
        linearCounter.configure(control, reloadValue);
        setLengthCounterHalted(control);
    }

    void writeTimerLowRegister(byte value) {
        timer.setPeriod((timer.getPeriod() & 0x700) | toUint(value));
    }

    @Override
    void writeLengthCounterRegister(byte value) {
        super.writeLengthCounterRegister(value);
        timer.setPeriod((timer.getPeriod() & 0xFF) | ((value & 0x7) << 8));
        linearCounter.setReload();
    }

    @Override
    void onCpuTick() {
        super.onCpuTick();
        if (timer.tick() && !isSilenced()) {
            triangleWaveGenerator.tick();
        }
    }

    @Override
    void onQuarterFrameTick() {
        super.onQuarterFrameTick();
        linearCounter.tick();
    }

    @Override
    boolean isSilenced() {
        return super.isSilenced() || linearCounter.isZero();
    }

    @Override
    int getVolume() {
        return triangleWaveGenerator.getValue();
    }
}
