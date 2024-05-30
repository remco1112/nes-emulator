package org.example.nes.bus;

import static org.example.nes.utils.UInt.toUint;

public class RAM implements Memory {
    private final byte[] ram;

    protected RAM(int size) {
        assert size >= 0 && size <= 0x10000;
        ram = new byte[size];
    }

    RAM(byte[] ram) {
        this(ram.length);
        System.arraycopy(ram, 0, this.ram, 0, ram.length);
    }

    @Override
    public byte read(short address) {
        return ram[toUint(address)];
    }

    @Override
    public void write(short address, byte value) {
        ram[toUint(address)] = value;
    }

    @Override
    public int getSize() {
        return ram.length;
    }
}
