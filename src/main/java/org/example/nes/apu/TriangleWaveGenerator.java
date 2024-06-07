package org.example.nes.apu;

import org.example.nes.sequencer.data.DataSequencer;

import java.util.List;
import java.util.stream.IntStream;

public class TriangleWaveGenerator {
    private static final List<Integer> SEQUENCER_DATA = IntStream.concat(
                    IntStream.iterate(15, i -> i >= 0, i -> i - 1),
                    IntStream.rangeClosed(0, 15))
            .boxed()
            .toList();

    private final DataSequencer<Integer> sequencer = new DataSequencer<>(SEQUENCER_DATA);

    void tick() {
        sequencer.tick();
    }

    int getValue() {
        return sequencer.getValue();
    }
}
