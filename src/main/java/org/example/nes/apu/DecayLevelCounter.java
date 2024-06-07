package org.example.nes.apu;

class DecayLevelCounter {
    private static final int START_VALUE = 15;

    private final Counter counter = new Counter();

    void tick(boolean loop) {
        if (counter.tick() & loop) reload();
    }

    void reload() {
        counter.set(START_VALUE);
    }

    int getValue() {
        return counter.get();
    }
}
