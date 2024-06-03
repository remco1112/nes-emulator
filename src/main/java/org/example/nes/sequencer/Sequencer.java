package org.example.nes.sequencer;

import java.util.Map;
import java.util.Set;

public class Sequencer<E extends Enum<E>> {
    private final Map<E, Runnable> eventHandlingMap;
    private final SequenceEventCollection<E> sequence;

    private int sequenceIndex;

    public Sequencer(Map<E, Runnable> eventHandlingMap, SequenceEventCollection<E> sequence) {
        this.eventHandlingMap = eventHandlingMap;
        this.sequence = sequence;
    }

    public void tick() {
        final Set<E> events = sequence.get(sequenceIndex);
        for (E event : events) {
            eventHandlingMap.get(event).run();
        }
        incrementSequenceIndex(1);
    }

    public void reset() {
        sequenceIndex = 0;
    }

    public void skip(int cycles) {
        incrementSequenceIndex(cycles);
    }

    private void incrementSequenceIndex(int cycles) {
        sequenceIndex = (sequenceIndex + cycles) % sequence.size();
    }

    public int getSequenceIndex() {
        return sequenceIndex;
    }
}
