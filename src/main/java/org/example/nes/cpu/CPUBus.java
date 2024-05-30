package org.example.nes.cpu;

import org.example.nes.Bus;

public interface CPUBus extends Bus {
    default byte dmaRead(short address) {
        return read(address);
    }
}
