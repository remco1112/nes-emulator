package org.example.nes.sequencer.data;

import org.example.nes.sequencer.Sequencer;

import java.util.EnumMap;
import java.util.Map;

public class DataSequencer<T> {
    private final Sequencer<DataSequenceEvent> sequencer;
    private final T[] data;

    private T next;

    public DataSequencer(T[] data) {
        this.data = data;
        this.sequencer = new Sequencer<>(
                new EnumMap<>(Map.of(DataSequenceEvent.DATA, this::handleData)),
                new DataSequenceEventCollection(data.length)
        );
    }

    public T tick() {
        sequencer.tick();
        return next;
    }

    private void handleData() {
        next = data[sequencer.getSequenceIndex()];
    }
}
