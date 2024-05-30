package org.example.nes.bus;

public interface Memory {
    int getSize();

    byte read(short address);

    void write(short address, byte value);
}
