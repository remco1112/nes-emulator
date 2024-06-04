package org.example.nes.apu;

public class Mixer {
    private static final short[] LOOKUP_TABLE;

    static {
        LOOKUP_TABLE = new short[31 * 203];
        for (int i = 0; i < 203; i++) {
            double tnd = i == 0 ? 0 : 163.67 / (24329.0 / i + 100);
            for (int j = 0; j < 31; j++) {
                LOOKUP_TABLE[31 * i + j] = toShort(tnd + (j == 0 ? 0 : 95.52 / (8128.0 / j + 100)));
            }
        }
    }

    short mix(int pulse1, int pulse2, int triangle, int noise, int dmc) {
        return LOOKUP_TABLE[31 * (3 * triangle + 2 * noise + dmc) + pulse1 + pulse2];
    }

    private static short toShort(double value) {
        return (short) Math.round((value - 0.5) * (1L << Short.SIZE));
    }
}
