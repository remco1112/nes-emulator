package org.example.nes.apu;

abstract class AbstractSweepUnit {
    private static final int ENABLE_MASK = 0x80;
    private static final int DIVIDER_PERIOD_MASK = 0x70;
    private static final int DIVIDER_PERIOD_SHIFT_OFFSET = 4;
    private static final int NEGATE_MASK = 0x4;
    private static final int SHIFT_COUNT_MASK = 0x3;
    private static final int CURRENT_PERIOD_MIN_VALUE = 8;
    private static final int TARGET_PERIOD_MAX_VALUE = 0x7ff;

    private final Divider divider = new Divider();

    private boolean enabled;
    private boolean negate;
    private boolean reload;
    private int dividerPeriod;
    private int shiftCount;

    void configure(byte configuration) {
        enabled = (configuration & ENABLE_MASK) == ENABLE_MASK;
        dividerPeriod = (configuration & DIVIDER_PERIOD_MASK) >>> DIVIDER_PERIOD_SHIFT_OFFSET;
        negate = (configuration & NEGATE_MASK) == NEGATE_MASK;
        shiftCount = configuration & SHIFT_COUNT_MASK;
        reload = true;
    }

    boolean isMuted(int currentPeriod) {
        return currentPeriod < CURRENT_PERIOD_MIN_VALUE || computeTargetPeriod(currentPeriod) > TARGET_PERIOD_MAX_VALUE;
    }

    int tick(int currentPeriod) {
        if (reload) {
            divider.reload(dividerPeriod);
            reload = false;
        } else if (divider.tick(dividerPeriod) && enabled && shiftCount > 0 && !isMuted(currentPeriod)) {
            return computeTargetPeriod(currentPeriod);
        }
        return currentPeriod;
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
