package org.example.nes.apu;

public class EnvelopeGenerator {
    private static final int VOLUME_MASK = 0xF;
    private static final int CONSTANT_MASK = 0x10;
    private static final int LOOP_MASK = 0x20;

    private final DecayLevelCounter decayLevelCounter = new DecayLevelCounter();
    private final Divider divider = new Divider();

    private int volume;
    private boolean constant;
    private boolean loop;
    private boolean start;

    void configure(byte configuration) {
        volume = configuration & VOLUME_MASK;
        constant = (configuration & CONSTANT_MASK) == CONSTANT_MASK;
        loop = (configuration & LOOP_MASK) == LOOP_MASK;
    }

    void setStart() {
        start = true;
    }

    int tick() {
        if (start) {
            decayLevelCounter.reload();
            divider.reload(volume);
            start = false;
        } else if (divider.tick(volume)){
                decayLevelCounter.tick(loop);
        }
        return constant ? volume : decayLevelCounter.getValue();
    }
}
