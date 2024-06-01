package org.example.nes.apu;

public class OnesComplementSweepUnit extends AbstractSweepUnit {
    @Override
    int negate(int changeAmount) {
        return -changeAmount - 1;
    }
}
