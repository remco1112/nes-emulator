package org.example.nes.bus;

public class ROM implements Memory {
    private final RAM ram;

    public ROM(byte[] data) {
        ram = new RAM(data);
    }

    @Override
    public byte read(short address) {
        return ram.read(address);
    }

    @Override
    public void write(short address, byte value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSize() {
        return ram.getSize();
    }
}
