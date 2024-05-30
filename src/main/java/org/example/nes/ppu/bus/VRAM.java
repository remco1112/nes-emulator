package org.example.nes.ppu.bus;

import org.example.nes.bus.RAM;

public class VRAM extends RAM {
    VRAM() {
        super(0x4000);
    }
}
