package org.example.nes.ppu;

import org.example.nes.Bus;
import org.example.nes.mapper.Mapper;

import static org.example.nes.UInt.toUint;
import static org.example.nes.mapper.Mapper.addrToString;
import static org.example.nes.mapper.Mapper.writeToString;

public class PPU2C02Bus implements Bus {

    private final byte[] paletteRam = new byte[32];
    private final byte[] vram = new byte[0x4000];
    
    private final Mapper mapper;
    
    PPU2C02Bus(Mapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public byte read(short address) {
        final int addr = toUint(address);
        if (isPaletteRamAddress(addr)) {
            final int mirroredAddress = addr - (addr == 0x3F10 || addr == 0x3F14 || addr == 0x3F18 || addr == 0x3F1C ? 0x10 : 0);
            return paletteRam[mirroredAddress % 32];
        }
        if (mapper.catchPpuRead(address)) {
            return mapper.readPpu(address);
        }
        if (isVramAddress(addr)) {
            final byte value = vram[(addr - 0x2000) % 0x4000];
            mapper.notifyPpuRead(address, value);
            return value;
        }
        System.out.println("Warning: received unmapped ppu read: " + addrToString(address));
        return 0;
    }

    private boolean isVramAddress(int addr) {
        return addr >= 0x2000 && addr <= 0x3EFF;
    }

    @Override
    public void write(short address, byte value) {
        final int addr = toUint(address);
        if (isPaletteRamAddress(addr)) {
            final int mirroredAddress = addr - (addr == 0x3F10 || addr == 0x3F14 || addr == 0x3F18 || addr == 0x3F1C ? 0x10 : 0);
            paletteRam[mirroredAddress % 32] = value;
            return;
        }
        if (mapper.catchPpuWrite(address)) {
            mapper.writePpu(address, value);
            return;
        }
        if (isVramAddress(addr)) {
            vram[(addr - 0x2000) % 0x4000] = value;
            mapper.notifyPpuWrite(address, value);
            return;
        }
        System.out.println("Warning: received unmapped ppu write: " + writeToString(addr, value));
    }

    static boolean isPaletteRamAddress(short address) {
        return isPaletteRamAddress(toUint(address));
    }

    private static boolean isPaletteRamAddress(int addr) {
        return addr >= 0x3F00 && addr <= 0x3FFF;
    }
}
