package org.example.nes.sequencer.data;

import org.example.nes.sequencer.Sequencer;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class DataSequencer<T> extends Sequencer<DataSequenceEvent> {
    private final List<T> data;
    private T value;

    public DataSequencer(List<T> data) {
        this(data, new LateInitRunnable());
    }

    private DataSequencer(List<T> data, LateInitRunnable lateInitRunnable) {
        super(new EnumMap<>(Map.of(DataSequenceEvent.DATA, lateInitRunnable)), new DataSequenceEventCollection(data.size()));
        this.data = data;
        lateInitRunnable.runnable = this::handleData;
        handleData();
    }

    private void handleData() {
        value = data.get(getSequenceIndex());
    }

    public T getValue() {
        return value;
    }

    private static class LateInitRunnable implements Runnable {
        private Runnable runnable;

        @Override
        public void run() {
            runnable.run();
        }
    }
}
