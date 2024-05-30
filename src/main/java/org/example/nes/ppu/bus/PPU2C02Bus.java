package org.example.nes.ppu.bus;

import org.example.nes.bus.AbstractBus;
import org.example.nes.bus.BusConfiguration;

public class PPU2C02Bus extends AbstractBus {
    public PPU2C02Bus(BusConfiguration mapperBusConfiguration) {
        super(new PPUBusConfiguration(mapperBusConfiguration));
    }
}
