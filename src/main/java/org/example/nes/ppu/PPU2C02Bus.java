package org.example.nes.ppu;

import org.example.nes.Bus;

public class PPU2C02Bus implements Bus {

    @Override
    public byte read(short address) {
        return 0;
    }

    @Override
    public void write(short address, byte value) {

    }
}
