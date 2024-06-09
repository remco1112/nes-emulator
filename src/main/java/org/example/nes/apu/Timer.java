package org.example.nes.apu;

class Timer {
    private final Counter counter = new Counter();
    private int period;

    Timer() {
        this(0);
    }

    Timer(int period) {
        this.period = period;
    }

    void reload() {
        counter.set(period);
    }

    boolean tick() {
        if (counter.tick()) {
            reload();
            return true;
        }
        return false;
    }

    void setPeriod(int period) {
        this.period = period;
    }

    int getPeriod() {
        return period;
    }

    boolean isActive() {
        return true;
    }
 }
