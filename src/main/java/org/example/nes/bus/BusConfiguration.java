package org.example.nes.bus;

import java.util.List;

public interface BusConfiguration {
    List<MappedMemory> getMappedMemoryList();

    List<ReadListener> getReadListeners();

    List<WriteListener> getWriteListeners();
}
