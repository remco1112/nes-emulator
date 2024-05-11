package org.example.nes.bridge;

import org.example.nes.cpu.CPU2A03;
import org.example.nes.mapper.Mapper;
import org.example.nes.ppu.PPU2C02;
import org.example.nes.ppu.PixelConsumer;

public class MasterClock {
    final CPU2A03 cpu2A03;
    final PPU2C02 ppu2C02;

    public MasterClock(Mapper mapper, PixelConsumer pixelConsumer) {
        final NESInterruptController nesInterruptController = new NESInterruptController();

        this.ppu2C02 = new PPU2C02(mapper, nesInterruptController, pixelConsumer);
        this.cpu2A03 = new CPU2A03(mapper, ppu2C02, nesInterruptController);
    }

    public void start() {
        int counter = 0;
        try {
            while (true) {
                ppu2C02.tick();
                if (counter++ % 3 == 0) {
                    cpu2A03.tick();
                }
            }
        } catch (Exception e) {
            System.out.println(counter);
            e.printStackTrace();
        }
    }
}
