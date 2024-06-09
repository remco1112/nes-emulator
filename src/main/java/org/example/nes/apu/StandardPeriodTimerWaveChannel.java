package org.example.nes.apu;

import static org.example.nes.utils.UInt.toUint;

class StandardPeriodTimerWaveChannel<W extends WaveGenerator> extends TimerWaveChannel<W> {

    StandardPeriodTimerWaveChannel(Timer timer, W waveGenerator, boolean tickWaveGeneratorOnCpu) {
        super(timer, waveGenerator, tickWaveGeneratorOnCpu);
    }

    @Override
    public void writeRegister2(byte value) {
        setTimerPeriod((getTimerPeriod() & 0x700) | toUint(value));
    }

    @Override
    void writeRegister3(byte value) {
        super.writeRegister3(value);
        setTimerPeriod((getTimerPeriod() & 0xFF) | ((value & 0x7) << 8));
    }
}
