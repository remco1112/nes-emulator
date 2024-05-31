package org.example.nes.input;

public class NullInputDevice implements InputDevice {

    @Override
    public byte read() {
        return 0;
    }

    @Override
    public void write(byte value) {

    }
}
