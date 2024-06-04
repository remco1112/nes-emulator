package org.example.nes.apu;

public class Mixer {
    private static final int[] LOOKUP_TABLE;

    static {
        LOOKUP_TABLE = new int[31 * 203];
        for (int i = 0; i < 203; i++) {
            double tnd = i == 0 ? 0 : 163.67 / (24329.0 / i + 100);
            for (int j = 0; j < 31; j++) {
                LOOKUP_TABLE[31 * i + j] = toInt(tnd + (j == 0 ? 0 : 95.52 / (8128.0 / j + 100)));
            }
        }
    }

    int mix(int pulse1, int pulse2, int triangle, int noise, int dmc) {
        return LOOKUP_TABLE[31 * (3 * triangle + 2 * noise + dmc) + pulse1 + pulse2];
    }

    private static int toInt(double value) {
        return (int) Math.round((value - 0.5) * (1L << 32));
    }
}
