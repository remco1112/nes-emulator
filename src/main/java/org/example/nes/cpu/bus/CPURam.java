package org.example.nes.cpu.bus;

import org.example.nes.bus.RAM;

public class CPURam extends RAM {
    CPURam() {
        super(0x800);
    }
}
