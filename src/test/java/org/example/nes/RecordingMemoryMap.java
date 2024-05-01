package org.example.nes;

import java.util.ArrayList;
import java.util.List;

class RecordingMemoryMap extends RAMMemoryMap {
    private final List<Cycle> log = new ArrayList<>();

    RecordingMemoryMap(byte[] ram) {
        super(ram);
    }

    @Override
    public byte get(short address) {
        final byte value = super.get(address);
        log.add(new Cycle(true, address, value));
        return value;
    }

    @Override
    public void set(short address, byte value) {
        log.add(new Cycle(false, address, value));
        super.set(address, value);
    }

    public List<Cycle> getLog() {
        return log;
    }
}
