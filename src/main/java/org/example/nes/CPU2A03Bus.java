package org.example.nes;

import org.example.nes.mapper.Mapper;

public class CPU2A03Bus implements Bus {
    private final byte[] ram = new byte[0x800];

    private Mapper mapper;

    CPU2A03Bus(Mapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public byte read(short address) {
        if (mapper.catchRead(address)) {
            return mapper.read(address);
        }
        final int addr = Short.toUnsignedInt(address);
        if (addr < 0x2000) {
            byte value = ram[addr % 0x800];
            mapper.notifyRead(address, value);
            return value;
        }
        System.out.println("Warning: received unmapped read: " + addr);
        return 0;
    }

    @Override
    public void write(short address, byte value) {
        if (mapper.catchWrite(address)) {
            mapper.write(address, value);
        }
        final int addr = Short.toUnsignedInt(address);
        if (addr < 0x2000) {
            ram[addr % 0x800] = value;
            mapper.notifyWrite(address, value);
            return;
        }
        System.out.println("Warning: received unmapped write: " + addr + " <- " + value);
    }
}
