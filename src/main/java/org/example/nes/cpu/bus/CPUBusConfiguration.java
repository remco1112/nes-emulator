package org.example.nes.cpu.bus;

import org.example.nes.bus.BusConfiguration;
import org.example.nes.bus.MappedMemory;
import org.example.nes.bus.ReadListener;
import org.example.nes.bus.WriteListener;
import org.example.nes.ppu.PPU2C02;

import java.util.List;
import java.util.stream.Stream;

import static org.example.nes.bus.MappedMemory.mapAt;
import static org.example.nes.bus.MirroredMemory.mirrored;

public class CPUBusConfiguration implements BusConfiguration {
    private final List<MappedMemory> mappedMemoryList;
    private final List<ReadListener> readListeners;
    private final List<WriteListener> writeListeners;

    // TODO prevent override of oam dma register with mapper
    CPUBusConfiguration(PPU2C02 ppu, BusConfiguration mapperBusConfiguration, DMAController dmaController) {
        mappedMemoryList = Stream.concat(
                mapperBusConfiguration.getMappedMemoryList().stream(),
                Stream.of(
                        mapAt((short) 0, mirrored(0x2000, new CPURam())),
                        mapAt((short) 0x2000, mirrored(0x2000, new PPURegisterBridge(ppu))),
                        mapAt((short) 0x4000, new IORegisterBridge(dmaController))
                )
        ).toList();

        readListeners = Stream.concat(
                mapperBusConfiguration.getReadListeners().stream(),
                Stream.of(dmaController)
        ).toList();

        writeListeners = mapperBusConfiguration.getWriteListeners();
    }

    @Override
    public List<MappedMemory> getMappedMemoryList() {
        return mappedMemoryList;
    }

    @Override
    public List<ReadListener> getReadListeners() {
        return readListeners;
    }

    @Override
    public List<WriteListener> getWriteListeners() {
        return writeListeners;
    }
}
