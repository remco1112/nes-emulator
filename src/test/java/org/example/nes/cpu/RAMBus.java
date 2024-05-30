package org.example.nes.cpu;

import org.example.nes.bus.Bus;

class RAMBus implements Bus {
    private static final int RAM_SIZE = 0x10000;

    private final byte[] ram;

    protected RAMBus(byte[] ram) {
        this();
        System.arraycopy(ram, 0, this.ram, 0, RAM_SIZE);
    }

    public RAMBus() {
        ram = new byte[RAM_SIZE];
    }

    @Override
    public byte read(short address) {
        return ram[Short.toUnsignedInt(address)];
    }

    @Override
    public void write(short address, byte value) {
        ram[Short.toUnsignedInt(address)] = value;
    }

    @Override
    public int getSize() {
        return RAM_SIZE;
    }
}
