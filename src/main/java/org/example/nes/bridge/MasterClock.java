package org.example.nes.bridge;

import org.example.nes.cpu.CPU2A03;
import org.example.nes.display.PixelConsumer;
import org.example.nes.mapper.Mapper;
import org.example.nes.ppu.OAM;
import org.example.nes.ppu.PPU2C02;

public class MasterClock {
    final CPU2A03 cpu2A03;
    final PPU2C02 ppu2C02;
    final PixelConsumer pixelConsumer;
    final OAM oam;

    int counter = 0;

    public MasterClock(Mapper mapper, PixelConsumer pixelConsumer) {
        final NESInterruptController nesInterruptController = new NESInterruptController();
        this.oam = new OAM();
        this.pixelConsumer = pixelConsumer;
        this.ppu2C02 = new PPU2C02(mapper, nesInterruptController, oam);
        this.cpu2A03 = new CPU2A03(mapper, ppu2C02, nesInterruptController, oam);
    }

    public void start() {
        long prevTime = System.nanoTime();
        while (true) {
            if (counter == 0) {
                long curTime = System.nanoTime();
                try {
                    final long sleepTime = 16_666_666L - (curTime - prevTime);
                    prevTime = curTime;
                    if (sleepTime > 0) {
                        Thread.sleep(sleepTime / 1_000_000, (int) (sleepTime % 1_000_000));
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            tick();
        }
    }

    public void tick() {
        final short pixel = ppu2C02.tick();
        if (pixel != -1) {
            pixelConsumer.onPixel(pixel);
        }
        if (counter % 3 == 0) {
            cpu2A03.tick();
        }
        counter = (counter + 1) % 89341;
    }
}
