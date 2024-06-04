package org.example.nes.apu;

import static org.example.nes.utils.UInt.toUint;

public class LengthCounter {
    private static final byte[] LOOKUP_TABLE = new byte[] {
            10, (byte) 254, 20,  2, 40,  4, 80,  6, (byte) 160,  8, 60, 10, 14, 12, 26, 14,
            12, 16, 24, 18, 48, 20, 96, 22, (byte) 192, 24, 72, 26, 16, 28, 32, 30
    };

    private boolean enabled;
    private boolean halted;

    private int counter;

    void tick() {
        if (counter > 0 && !halted) {
            counter--;
        }
    }

    boolean isMuted() {
        return counter == 0;
    }

    void setLength(int lengthIndex) {
        assert lengthIndex < LOOKUP_TABLE.length;
        if (enabled) {
            counter = toUint(LOOKUP_TABLE[lengthIndex]);
        }
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            counter = 0;
        }
    }

    void setHalted(boolean halted) {
        this.halted = halted;
    }
}
