package org.example.nes.apu;

public class EnvelopeGenerator {
    private final DecayLevelCounter decayLevelCounter = new DecayLevelCounter();
    private final Timer timer = new Timer();

    private int volume;
    private boolean constant;
    private boolean loop;
    private boolean start;

    void configure(int volume, boolean constant, boolean loop) {
        this.volume = volume;
        this.constant = constant;
        this.loop = loop;

        timer.setPeriod(volume);
    }

    void setStart() {
        start = true;
    }

    void tick() {
        if (start) {
            decayLevelCounter.reload();
            timer.reload();
            start = false;
        } else if (timer.tick()) {
            decayLevelCounter.tick(loop);
        }
    }

    int getVolume() {
        return constant ? volume : decayLevelCounter.getValue();
    }
}
