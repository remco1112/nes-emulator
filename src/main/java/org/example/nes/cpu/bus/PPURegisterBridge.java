package org.example.nes.cpu.bus;

import org.example.nes.bus.Memory;
import org.example.nes.ppu.PPU2C02;

import static org.example.nes.utils.StringUtils.addressToString;
import static org.example.nes.utils.StringUtils.writeToString;
import static org.example.nes.utils.UInt.toUint;

public class PPURegisterBridge implements Memory {
    private static final int PPU_REGISTER_FILE_SIZE = 8;

    private final PPU2C02 ppu;

    PPURegisterBridge(PPU2C02 ppu) {
        this.ppu = ppu;
    }

    @Override
    public int getSize() {
        return PPU_REGISTER_FILE_SIZE;
    }

    @Override
    public void write(short address, byte value) {
        switch (toUint(address)) {
            case 0 -> ppu.writeRegPpuCtrl(value);
            case 1 -> ppu.writeRegPpuMask(value);
            case 3 -> ppu.writeRegOamAddr(value);
            case 4 -> ppu.writeRegOamData(value);
            case 5 -> ppu.writeRegPpuScroll(value);
            case 6 -> ppu.writeRegPpuAddr(value);
            case 7 -> ppu.writeRegPpuData(value);
            default -> throw new IllegalStateException("Illegal PPU register write: " + writeToString(address, value));
        }
    }

    @Override
    public byte read(short address) {
        return switch (toUint(address)) {
            case 2 -> ppu.readRegPpuStatus();
            case 3 -> ppu.readRegOamAddr();
            case 4 -> ppu.readRegOamData();
            case 7 -> ppu.readRegPpuData();
            default -> throw new IllegalStateException("Illegal PPU register read at: " + addressToString(address));
        };
    }
}
