package org.example.nes.apu;

import java.util.List;
import java.util.Objects;

class LengthCounter extends Counter {
    private static final List<Integer> LOOKUP_TABLE = List.of(
            10, 254, 20, 2, 40, 4, 80, 6, 160,  8, 60, 10, 14, 12, 26, 14,
            12, 16, 24, 18, 48, 20, 96, 22, 192, 24, 72, 26, 16, 28, 32, 30
    );

    private boolean enabled;
    private boolean halted;

    @Override
    boolean tick() {
        return !halted && super.tick();
    }

    @Override
    void set(int lengthIndex) {
        assert Objects.checkIndex(lengthIndex, LOOKUP_TABLE.size()) == lengthIndex;
        if (enabled) {
            super.set(LOOKUP_TABLE.get(lengthIndex));
        }
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            super.set(0);
        }
    }

    void setHalted(boolean halted) {
        this.halted = halted;
    }
}
