package org.example.nes.bus;

public interface WriteListener {
    void onWrite(short address, byte value);
}
