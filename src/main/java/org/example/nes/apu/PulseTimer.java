package org.example.nes.apu;

public class PulseTimer extends Timer {
    private boolean active;

    @Override
    void setPeriod(int period) {
        super.setPeriod(period);
        active = period >= PulseWaveGenerator.SEQUENCE_LENGTH;
    }

    @Override
    boolean tick() {
        return active && super.tick();
    }

    @Override
    public boolean isActive() {
        return active;
    }
}
