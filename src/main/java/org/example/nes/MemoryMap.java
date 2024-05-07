package org.example.nes;

public interface MemoryMap {

    byte get(short address);

    void set(short address, byte value);
}
