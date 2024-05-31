package org.example.nes.input;

public interface InputDevice {

    byte read();

    void write(byte value);
}
