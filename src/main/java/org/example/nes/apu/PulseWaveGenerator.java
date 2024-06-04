package org.example.nes.apu;

import static org.example.nes.utils.UInt.toUint;

public class PulseWaveGenerator {
    private static final int SEQUENCE_LENGTH = 8;
    private static final byte[] LOOKUP_TABLE = new byte[] {
            (byte) 0b01000000,
            (byte) 0b01100000,
            (byte) 0b01111000,
            (byte) 0b10011111
    };

    private final Divider timer = new Divider();

    private int duty;
    private int cycle;
    private boolean waveValue;
    private boolean muted;

    void setDuty(int duty) {
        this.duty = duty;
    }

    void setTimer(int value) {
        timer.setPeriod(value);
        muted = value < SEQUENCE_LENGTH;
    }

    public int getTimerPeriod() {
        return timer.getPeriod();
    }

    boolean tick() {
        if (muted) {
            return false;
        }
        if (timer.tick()) {
            waveValue = toUint(LOOKUP_TABLE[duty]) >>> SEQUENCE_LENGTH - getAndDecrementCycle() - 1 == 1;
        }
        return waveValue;
    }

    private int getAndDecrementCycle() {
        final int currentCycle = cycle;
        if (cycle == 0) {
            cycle = SEQUENCE_LENGTH - 1;
        } else {
            cycle--;
        }
        return currentCycle;
    }

    void resetPeriod() {
        cycle = 0;
    }
}
