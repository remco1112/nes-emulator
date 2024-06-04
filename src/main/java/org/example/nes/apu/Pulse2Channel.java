package org.example.nes.apu;

public class Pulse2Channel extends AbstractPulseChannel {
    Pulse2Channel() {
        super(new TwosComplementSweepUnit());
    }
}
