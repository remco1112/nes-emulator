package org.example.nes.mapper;

import static org.example.nes.UInt.toUint;

public class NROM implements Mapper {
    private static final int ROM_OFFSET = 0x8000;

    private final byte[] pgrRom;
    private final byte[] chrRom;

    NROM(byte[] pgrRom, byte[] chrRom) {
        this.pgrRom = pgrRom;
        this.chrRom = chrRom;
    }

    @Override
    public boolean catchRead(short address) {
        final int uIntAddr = toUint(address);
        return uIntAddr >= ROM_OFFSET && uIntAddr <= 0xFFFF;
    }

    @Override
    public boolean catchWrite(short address) {
        return false;
    }

    @Override
    public void notifyRead(short address, byte value) {

    }

    @Override
    public void notifyWrite(short address, byte value) {

    }

    @Override
    public byte read(short address) {
        return pgrRom[(toUint(address) - ROM_OFFSET) % pgrRom.length];
    }

    @Override
    public void write(short address, byte value) {
        throw new UnsupportedOperationException();
    }
}
