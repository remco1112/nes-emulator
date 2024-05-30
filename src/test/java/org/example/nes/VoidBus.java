package org.example.nes;

import org.example.nes.bus.Bus;

public class VoidBus implements Bus {

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public byte read(short address) {
        return 0;
    }

    @Override
    public void write(short address, byte value) {

    }
}
