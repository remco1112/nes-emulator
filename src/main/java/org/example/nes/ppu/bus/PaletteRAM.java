package org.example.nes.ppu.bus;

import org.example.nes.bus.RAM;

import static org.example.nes.utils.UInt.toUint;

public class PaletteRAM extends RAM {
    PaletteRAM() {
        super(0x20);
    }

    @Override
    public byte read(short address) {
        return super.read(mirror(toUint(address)));
    }

    @Override
    public void write(short address, byte value) {
        super.write(mirror(toUint(address)), value);
    }

    private short mirror(int address) {
        // TODO improve
        return (short) (address - ((address == 0x10) || (address == 0x14) || (address == 0x18) || (address == 0x1C) ? 0x10 : 0));
    }
}
