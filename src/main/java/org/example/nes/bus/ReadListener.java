package org.example.nes.bus;

public interface ReadListener {
    void onRead(short address, byte value);
}
