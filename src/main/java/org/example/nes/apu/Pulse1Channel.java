package org.example.nes.apu;

public class Pulse1Channel extends AbstractPulseChannel {
    Pulse1Channel() {
        super(new OnesComplementSweepUnit());
    }
}
