package org.example.nes.apu;

class Counter {
    private int value;

    boolean tick() {
        if (value == 0) {
            return true;
        }
        value--;
        return false;
    }

    void set(int value) {
        this.value = value;
    }

    int get() {
        return value;
    }

    boolean isZero() {
        return get() == 0;
    }
}
