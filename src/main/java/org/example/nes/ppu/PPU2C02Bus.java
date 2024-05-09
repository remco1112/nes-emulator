package org.example.nes.ppu;

import org.example.nes.Bus;

public class PPU2C02Bus implements Bus {

    private final byte[] paletteRam = new byte[32];

    @Override
    public byte read(short address) {
        final int addr = Short.toUnsignedInt(address);
        if (addr >= 0x3F00 && addr <= 0x3FFF) {
            return paletteRam[addr % 32];
        }
        return 0;
    }

    @Override
    public void write(short address, byte value) {
        final int addr = Short.toUnsignedInt(address);
        if (addr >= 0x3F00 && addr <= 0x3FFF) {
            paletteRam[addr % 32] = value;
        }
    }
}
