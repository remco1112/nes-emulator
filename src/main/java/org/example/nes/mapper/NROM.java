package org.example.nes.mapper;

import org.example.nes.bus.*;

import java.util.List;

import static org.example.nes.bus.MappedMemory.mapAt;
import static org.example.nes.bus.MirroredMemory.mirrored;

// TODO make package-private
public class NROM implements Mapper {
    private static final short ROM_OFFSET = (short) 0x8000;

    private final byte[] pgrRom;
    private final byte[] chrRom;

    public NROM(byte[] pgrRom, byte[] chrRom) {
        this.pgrRom = pgrRom;
        this.chrRom = chrRom;
    }

    @Override
    public BusConfiguration getCpuBusConfiguration() {
        return new CPUBusConfiguration();
    }

    @Override
    public BusConfiguration getPpuBusConfiguration() {
        return new PPUBusConfiguration();
    }

    private class CPUBusConfiguration implements BusConfiguration {

        @Override
        public List<MappedMemory> getMappedMemoryList() {
            return List.of(
                    mapAt(ROM_OFFSET, mirrored(0x8000, new ROM(pgrRom)))
            );
        }

        @Override
        public List<ReadListener> getReadListeners() {
            return List.of();
        }

        @Override
        public List<WriteListener> getWriteListeners() {
            return List.of();
        }
    }

    private class PPUBusConfiguration implements BusConfiguration {

        @Override
        public List<MappedMemory> getMappedMemoryList() {
            return List.of(
                    mapAt((short) 0, new ROM(chrRom))
            );
        }

        @Override
        public List<ReadListener> getReadListeners() {
            return List.of();
        }

        @Override
        public List<WriteListener> getWriteListeners() {
            return List.of();
        }
    }
}
