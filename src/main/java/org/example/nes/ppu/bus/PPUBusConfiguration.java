package org.example.nes.ppu.bus;

import org.example.nes.bus.BusConfiguration;
import org.example.nes.bus.MappedMemory;
import org.example.nes.bus.ReadListener;
import org.example.nes.bus.WriteListener;

import java.util.List;
import java.util.stream.Stream;

import static org.example.nes.bus.MappedMemory.mapAt;
import static org.example.nes.bus.MirroredMemory.mirrored;

public class PPUBusConfiguration implements BusConfiguration {
    private final List<MappedMemory> mappedMemoryList;
    private final List<ReadListener> readListeners;
    private final List<WriteListener> writeListeners;

    public PPUBusConfiguration(BusConfiguration mapperBusConfiguration) {
        mappedMemoryList = Stream.concat(
                Stream.concat(
                        Stream.of(mapAt((short) 0x3F00, mirrored(0x100, new PaletteRAM()))),
                        mapperBusConfiguration.getMappedMemoryList().stream()
                ),
                Stream.of(mapAt((short) 0x2000, new VRAM())) // TODO missing mirror
        ).toList();

        readListeners = mapperBusConfiguration.getReadListeners();
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
