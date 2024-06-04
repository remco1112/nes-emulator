package org.example.nes.apu;

public class EnvelopeGenerator {
    private final DecayLevelCounter decayLevelCounter = new DecayLevelCounter();
    private final Divider divider = new Divider();

    private int volume;
    private boolean constant;
    private boolean loop;
    private boolean start;

    void configure(int volume, boolean constant, boolean loop) {
        this.volume = volume;
        this.constant = constant;
        this.loop = loop;

        divider.setPeriod(volume);
    }

    void setStart() {
        start = true;
    }

    void tick() {
        if (start) {
            decayLevelCounter.reload();
            divider.reload();
            start = false;
        } else if (divider.tick()){
                decayLevelCounter.tick(loop);
        }
    }

    int getVolume() {
        return constant ? volume : decayLevelCounter.getValue();
    }
}
