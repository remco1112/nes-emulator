package org.example.nes;

public class VoidBus implements Bus {

    @Override
    public byte read(short address) {
        return 0;
    }

    @Override
    public void write(short address, byte value) {

    }
}
