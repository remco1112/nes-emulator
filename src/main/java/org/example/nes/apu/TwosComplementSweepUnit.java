package org.example.nes.apu;

public class TwosComplementSweepUnit extends AbstractSweepUnit {
    @Override
    int negate(int changeAmount) {
        return -changeAmount;
    }
}
