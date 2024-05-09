package org.example.nes.ppu;

import org.example.nes.Bus;

import static org.example.nes.UInt.toUint;

public class PPU2C02Bus implements Bus {

    private final byte[] paletteRam = new byte[32];

    @Override
    public byte read(short address) {
        final int addr = toUint(address);
        if (isPaletteRamAddress(addr)) {
            return paletteRam[addr % 32];
        }
        return 0;
    }

    @Override
    public void write(short address, byte value) {
        final int addr = toUint(address);
        if (isPaletteRamAddress(addr)) {
            paletteRam[addr % 32] = value;
        }
    }

    static boolean isPaletteRamAddress(short address) {
        return isPaletteRamAddress(toUint(address));
    }

    private static boolean isPaletteRamAddress(int addr) {
        return addr >= 0x3F00 && addr <= 0x3FFF;
    }
}
