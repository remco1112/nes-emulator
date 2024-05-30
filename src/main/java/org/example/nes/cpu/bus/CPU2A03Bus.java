package org.example.nes.cpu.bus;

import org.example.nes.bus.AbstractBus;
import org.example.nes.bus.Bus;
import org.example.nes.bus.BusConfiguration;
import org.example.nes.ppu.PPU2C02;

public class CPU2A03Bus extends AbstractBus implements Bus {
    public CPU2A03Bus(BusConfiguration busConfiguration, PPU2C02 ppu, DMAController dmaController) {
        super(new CPUBusConfiguration(ppu, busConfiguration, dmaController));
    }
}
