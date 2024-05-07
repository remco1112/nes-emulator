package org.example.nes;

import java.util.ArrayList;
import java.util.List;

class RecordingBus extends RAMBus {
    private final List<Cycle> log = new ArrayList<>();

    RecordingBus(byte[] ram) {
        super(ram);
    }

    @Override
    public byte read(short address) {
        final byte value = super.read(address);
        log.add(new Cycle(true, address, value));
        return value;
    }

    @Override
    public void write(short address, byte value) {
        log.add(new Cycle(false, address, value));
        super.write(address, value);
    }

    public List<Cycle> getLog() {
        return log;
    }
}
