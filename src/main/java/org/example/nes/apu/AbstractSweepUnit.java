package org.example.nes.apu;

abstract class AbstractSweepUnit {
    private static final int CURRENT_PERIOD_MIN_VALUE = 8;
    private static final int TARGET_PERIOD_MAX_VALUE = 0x7ff;

    private final Timer timer = new Timer();

    private boolean enabled;
    private boolean negate;
    private boolean reload;
    private int shiftCount;

    void configure(boolean enabled, int timerPeriod, boolean negate, int shiftCount) {
        this.enabled = enabled;
        this.negate = negate;
        this.shiftCount = shiftCount;

        timer.setPeriod(timerPeriod);

        reload = true;
    }

    boolean isMuted(int currentPeriod) {
        return currentPeriod < CURRENT_PERIOD_MIN_VALUE || computeTargetPeriod(currentPeriod) > TARGET_PERIOD_MAX_VALUE;
    }

    int tick(int currentPeriod) {
        if (reload) {
            timer.reload();
            reload = false;
        } else if (timer.tick() && enabled && shiftCount > 0 && !isMuted(currentPeriod)) {
            return computeTargetPeriod(currentPeriod);
        }
        return -1;
    }

    private int computeTargetPeriod(int currentPeriod) {
        int changeAmount = currentPeriod >>> shiftCount;
        if (negate) {
            changeAmount = negate(changeAmount);
        }
        return Math.max(currentPeriod + changeAmount, 0);
    }

    abstract int negate(int changeAmount);
}
