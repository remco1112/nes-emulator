package org.example.nes.apu;

public class Divider {
    private int period;
    private int value;

    Divider() {
        this(0);
    }

    Divider(int period) {
        this.period = period;
    }

    void reload() {
        this.value = period;
    }

    boolean tick() {
        if (value == 0) {
            reload();
            return true;
        }
        value--;
        return false;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getPeriod() {
        return period;
    }
}
