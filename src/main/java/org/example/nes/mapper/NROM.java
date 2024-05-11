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
    public boolean catchCpuRead(short address) {
        final int uIntAddr = toUint(address);
        return uIntAddr >= ROM_OFFSET && uIntAddr <= 0xFFFF;
    }

    @Override
    public boolean catchCpuWrite(short address) {
        return false;
    }

    @Override
    public void notifyCpuRead(short address, byte value) {

    }

    @Override
    public void notifyCpuWrite(short address, byte value) {

    }

    @Override
    public byte readCpu(short address) {
        return pgrRom[(toUint(address) - ROM_OFFSET) % pgrRom.length];
    }

    @Override
    public void writeCpu(short address, byte value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean catchPpuRead(short address) {
        final int uIntAddr = toUint(address);
        return uIntAddr < 0x2000;
    }

    @Override
    public boolean catchPpuWrite(short address) {
        return false;
    }

    @Override
    public void notifyPpuRead(short address, byte value) {

    }

    @Override
    public void notifyPpuWrite(short address, byte value) {

    }

    @Override
    public byte readPpu(short address) {
        return chrRom[address];
    }

    @Override
    public void writePpu(short address, byte value) {

    }
}
