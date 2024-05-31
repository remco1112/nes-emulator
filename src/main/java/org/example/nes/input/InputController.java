package org.example.nes.input;

import static org.example.nes.utils.UInt.toUint;

public class InputController {
    private final InputDevice device1;
    private final InputDevice device2;

    public InputController(InputDevice device1, InputDevice device2) {
        this.device1 = device1;
        this.device2 = device2;
    }

    public void writeOutRegister(byte value) {
        final int valueInt = toUint(value);
        assert (valueInt & 0b111) == valueInt;
        device1.write(value);
        device2.write(value);
    }

    public byte readDevice1() {
        return device1.read();
    }

    public byte readDevice2() {
        return device2.read();
    }
}
