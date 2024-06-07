package org.example.nes.sequencer.data;

import org.example.nes.sequencer.SequenceEventCollection;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

record DataSequenceEventCollection(int size) implements SequenceEventCollection<DataSequenceEvent> {
    private static final Set<DataSequenceEvent> DATA_SEQUENCE_EVENTS = EnumSet.of(DataSequenceEvent.DATA);

    @Override
    public Set<DataSequenceEvent> get(int index) {
        assert Objects.checkIndex(index, size) == index;
        return DATA_SEQUENCE_EVENTS;
    }
}
