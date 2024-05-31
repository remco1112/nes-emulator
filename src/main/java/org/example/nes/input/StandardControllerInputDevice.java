package org.example.nes.input;

import java.util.List;

public class StandardControllerInputDevice implements InputDevice {
    private static final byte STROBE_FLAG = 1;
    private static final byte READS_EXHAUSTED = 1;

    private static final List<StandardControllerButton> BUTTON_READ_ORDER = List.of(
      StandardControllerButton.A,
      StandardControllerButton.B,
      StandardControllerButton.SELECT,
      StandardControllerButton.START,
      StandardControllerButton.UP,
      StandardControllerButton.DOWN,
      StandardControllerButton.LEFT,
      StandardControllerButton.RIGHT
    );

    private final StandardControllerAdapter standardControllerAdapter;
    private int currentButton;
    private int shiftRegister;

    public StandardControllerInputDevice(StandardControllerAdapter standardControllerAdapter) {
        this.standardControllerAdapter = standardControllerAdapter;
    }

    @Override
    public byte read() {
        if (currentButton == BUTTON_READ_ORDER.size()) {
            return READS_EXHAUSTED;
        }
        return (byte) ((shiftRegister >>> currentButton++) & 1);
    }

    @Override
    public void write(byte value) {
        if ((value & STROBE_FLAG) == 0) {
            return;
        }
        currentButton = 0;
        shiftRegister = 0;
        for (int i = 0; i < BUTTON_READ_ORDER.size(); i++) {
            if (standardControllerAdapter.isButtonPressed(BUTTON_READ_ORDER.get(i))) {
                shiftRegister |= 1 << i;
            }
        }
    }
}
