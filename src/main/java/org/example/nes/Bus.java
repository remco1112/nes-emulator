package org.example.nes;

public interface Bus {

    byte read(short address);

    void write(short address, byte value);
}
