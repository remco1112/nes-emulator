package org.example.nes;

public class RAMMemoryMap implements MemoryMap {
    private static final int RAM_SIZE = 0x10000;

    private final byte[] ram;

    protected RAMMemoryMap(byte[] ram) {
        this();
        System.arraycopy(ram, 0, this.ram, 0, RAM_SIZE);
    }

    public RAMMemoryMap() {
        ram = new byte[RAM_SIZE];
    }

    @Override
    public byte get(short address) {
        return ram[Short.toUnsignedInt(address)];
    }

    @Override
    public void set(short address, byte value) {
        ram[Short.toUnsignedInt(address)] = value;
    }

    @Override
    public byte[] asByteArray() {
        final byte[] res = new byte[RAM_SIZE];
        System.arraycopy(ram, 0, res, 0, RAM_SIZE);
        return res;
    }
}
