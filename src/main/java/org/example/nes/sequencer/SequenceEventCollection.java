package org.example.nes.sequencer;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SequenceEventCollection<E> {

    Set<E> get(int index);

    int size();


    static <E> SequenceEventCollection<E> ofList(List<Set<E>> sequence) {
        return new DenseSequenceEventCollection<>(sequence);
    }

    static <E> SequenceEventCollection<E> ofMap(Map<Integer, Set<E>> sequence) {
        return new SparseSequenceEventCollection<>(sequence);
    }
}
