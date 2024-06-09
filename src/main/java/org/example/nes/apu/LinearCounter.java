package org.example.nes.apu;

public class LinearCounter extends Counter {
    private boolean reload;
    private boolean control;
    private int reloadValue;

    void configure(boolean control, int reloadValue) {
        this.control = control;
        this.reloadValue = reloadValue;
    }

    @Override
    boolean tick() {
        final boolean value;
        if (reload) {
            set(reloadValue);
            value = false;
        } else {
            value = super.tick();
        }
        reload &= control;
        return value;
    }

    void setReload() {
        reload = true;
    }
}
