package org.example.nes.bridge;

import org.example.nes.apu.APURP2A03;
import org.example.nes.cpu.CPU2A03;
import org.example.nes.display.PixelConsumer;
import org.example.nes.input.InputController;
import org.example.nes.mapper.Mapper;
import org.example.nes.ppu.PPU2C02;
import org.example.nes.sound.SoundSampleConsumer;

public class MasterClock {
    final CPU2A03 cpu2A03;
    final PPU2C02 ppu2C02;
    final APURP2A03 apu;
    final PixelConsumer pixelConsumer;
    final SoundSampleConsumer soundSampleConsumer;

    int counter = 0;

    public MasterClock(Mapper mapper, PixelConsumer pixelConsumer, InputController inputController, SoundSampleConsumer soundSampleConsumer) {
        final NESInterruptController nesInterruptController = new NESInterruptController();
        this.pixelConsumer = pixelConsumer;
        this.soundSampleConsumer = soundSampleConsumer;
        this.apu = new APURP2A03();
        this.ppu2C02 = new PPU2C02(mapper.getPpuBusConfiguration(), nesInterruptController);
        this.cpu2A03 = new CPU2A03(mapper.getCpuBusConfiguration(), ppu2C02, nesInterruptController, inputController, apu);
    }

    public void start() {
        while (!Thread.interrupted()) {
            tick();
        }
    }

    public void tick() {
        final short pixel = ppu2C02.tick();
        if (pixel != -1) {
            pixelConsumer.onPixel(pixel);
        }
        if (counter == 0) {
            cpu2A03.tick();
            final short soundSample = apu.tick();
            soundSampleConsumer.onSample(soundSample);
        }
        counter = (counter + 1) % 3;
    }
}
