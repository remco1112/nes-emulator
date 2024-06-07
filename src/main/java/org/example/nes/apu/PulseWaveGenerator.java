package org.example.nes.apu;

import org.example.nes.sequencer.data.DataSequencer;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class PulseWaveGenerator {
    private static final int SEQUENCE_LENGTH = 8;
    private static final List<List<Boolean>> SEQUENCERS_DATA = IntStream.of(0b01000000, 0b01100000, 0b01111000, 0b10011111)
            .mapToObj(PulseWaveGenerator::intToBooleanList)
            .toList();

    private final List<DataSequencer<Boolean>> sequencers = SEQUENCERS_DATA.stream()
            .map(DataSequencer::new)
            .toList();

    private final Divider timer = new Divider();

    private DataSequencer<Boolean> activeSequencer = sequencers.getFirst();
    private boolean active;

    void setDuty(int duty) {
        assert Objects.checkIndex(duty, sequencers.size()) == duty;
        activeSequencer = sequencers.get(duty);
    }

    void setTimer(int value) {
        timer.setPeriod(value);
        active = value >= SEQUENCE_LENGTH;
    }

    public int getTimerPeriod() {
        return timer.getPeriod();
    }

    void tick() {
        if (active && timer.tick()) for (var sequencer : sequencers) sequencer.tick();
    }

    void resetPeriod() {
        for (var sequencer : sequencers) sequencer.reset();
    }

    boolean isWaveHigh() {
        return active && activeSequencer.getValue();
    }

    private static List<Boolean> intToBooleanList(int value) {
        return IntStream.range(0, SEQUENCE_LENGTH)
                .mapToObj(i -> ((value >>> (SEQUENCE_LENGTH - i)) & 1) == 1)
                .toList();
    }
}
