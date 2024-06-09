package org.example.nes.apu;

public class NoiseChannel extends TimerWaveChannel<NoiseWaveGenerator> {
    NoiseChannel() {
        super(new NoiseTimer(), new NoiseWaveGenerator(), false);
    }

    @Override
    void writeRegister2(byte value) {
        super.writeRegister2(value);
        waveGenerator.configure((value & 0x80) != 0);
        setTimerPeriod(value & 0xF);
    }
}
