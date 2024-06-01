package org.example.nes.apu;

public class DecayLevelCounter {
    private static final int START_VALUE = 15;

    private int value;

    void tick(boolean loop) {
        if (value == 0) {
            if (loop) {
                reload();
            }
        } else {
            value--;
        }
    }

    void reload() {
        value = START_VALUE;
    }

    int getValue() {
        return value;
    }
}
