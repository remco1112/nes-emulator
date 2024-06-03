package org.example.nes.sequencer;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

class SparseSequenceEventCollection<E> implements SequenceEventCollection<E> {
    private final Map<Integer, Set<E>> map;
    private final int size;

    SparseSequenceEventCollection(Map<Integer, Set<E>> map) {
        this.map = map;
        size = map.keySet().stream().max(Integer::compareTo).orElse(0);
    }

    @Override
    public Set<E> get(int index) {
        return map.getOrDefault(index, Collections.emptySet());
    }

    @Override
    public int size() {
        return size;
    }
}
