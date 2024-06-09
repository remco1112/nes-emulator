package org.example.nes.apu;

import java.util.List;
import java.util.Objects;

class NoiseTimer extends Timer {
    private static final List<Integer> LOOKUP_TABLE = List.of(
            4, 8, 16, 32, 64, 96, 128, 160, 202, 254, 380, 508, 762, 1016, 2034, 4068
    );

    @Override
    void setPeriod(int period) {
        assert Objects.checkIndex(period, LOOKUP_TABLE.size()) == period;
        super.setPeriod(LOOKUP_TABLE.get(period));
    }
}
