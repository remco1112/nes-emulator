package org.example.nes;

public interface Bus {

    byte get(short address);

    void set(short address, byte value);
}
