package org.example.nes.sequencer;

import java.util.List;
import java.util.Set;

class DenseSequenceEventCollection<E> implements SequenceEventCollection<E> {
    private final List<Set<E>> list;

    DenseSequenceEventCollection(List<Set<E>> list) {
        this.list = list;
    }

    @Override
    public Set<E> get(int index) {
        return list.get(index);
    }

    @Override
    public int size() {
        return list.size();
    }
}
