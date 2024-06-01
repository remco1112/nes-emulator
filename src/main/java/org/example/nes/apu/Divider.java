package org.example.nes.apu;

public class Divider {
    private int value;

    void reload(int reloadValue) {
        this.value = reloadValue;
    }

    boolean tick(int reloadValue) {
        if (value == 0) {
            reload(reloadValue);
            return true;
        }
        value--;
        return false;
    }
}
