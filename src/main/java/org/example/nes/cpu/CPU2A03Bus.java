package org.example.nes.cpu;

import org.example.nes.Bus;
import org.example.nes.mapper.Mapper;
import org.example.nes.ppu.PPU2C02;

import static org.example.nes.mapper.Mapper.addrToString;
import static org.example.nes.mapper.Mapper.writeToString;

public class CPU2A03Bus implements Bus {
    private final byte[] ram = new byte[0x800];

    private final Mapper mapper;
    private final PPU2C02 ppu;

    CPU2A03Bus(Mapper mapper, PPU2C02 ppu) {
        this.mapper = mapper;
        this.ppu = ppu;
    }

    @Override
    public byte read(short address) {
        if (mapper.catchCpuRead(address)) {
            return mapper.readCpu(address);
        }
        return handleReadAndNotify(address);
    }

    private byte handleReadAndNotify(short address) {
        byte value = handleRead(address);
        mapper.notifyCpuRead(address, value);
        return value;
    }

    private byte handleRead(short address) {
        final int addr = Short.toUnsignedInt(address);
        if (addr < 0x2000) {
            return readFromRam(addr);
        }
        if (addr < 0x4000) {
            return readFromPpu(addr);
        }
        if (addr < 0x4018) {
            return readFromIO(addr);
        }
        System.out.println("Warning: received unmapped read: " + addrToString(address));
        return 0;
    }

    private byte readFromRam(int addr) {
        return ram[addr % 0x800];
    }

    private byte readFromPpu(int addr) {
        return switch (addr % 8) {
            case 2 -> ppu.readRegPpuStatus();
            case 3 -> ppu.getRegOamAddr();
            case 4 -> ppu.getRegOamData();
            case 7 -> ppu.readRegPpuData();
            default -> throw new IllegalStateException("Illegal PPU register read at: " + addrToString(addr));
        };
    }

    private byte readFromIO(int addr) {
        throw new IllegalStateException("Read from unsupported/illegal IO address at: " + addrToString(addr));
    }

    private void writeToRam(int addr, byte value) {
        ram[addr % 0x800] = value;
    }

    private void writeToPpu(int addr, byte value) {
        switch (addr % 8) {
            case 0 -> ppu.writeRegPpuCtrl(value);
            case 1 -> ppu.writeRegPpuMask(value);
            case 3 -> ppu.setRegOamAddr(value);
            case 4 -> ppu.setRegOamData(value);
            case 5 -> ppu.writeRegPpuScroll(value);
            case 6 -> ppu.writeRegPpuAddr(value);
            case 7 -> ppu.writeRegPpuData(value);
            default -> throw new IllegalStateException("Illegal PPU register write: " + writeToString(addr, value));
        }
    }

    private void writeToIo(int addr, byte value) {
        switch (addr % 0x18) {
            case 0x14 -> ppu.setRegOamDma(value);
            default -> throw new IllegalStateException("Illegal IO register write: " + writeToString(addr, value));
        }
    }

    private void handleWriteAndNotify(short addr, byte value) {
        handleWrite(addr, value);
        mapper.notifyCpuWrite(addr, value);
    }

    private void handleWrite(short address, byte value) {
        final int addr = Short.toUnsignedInt(address);
        if (addr < 0x2000) {
            writeToRam(addr, value);
            return;
        }
        if (addr < 0x4000) {
            writeToPpu(addr, value);
            return;
        }
        if (addr < 0x4018) {
            writeToIo(addr, value);
            return;
        }
        System.out.println("Warning: received unmapped write: " + writeToString(addr, value));
    }

    @Override
    public void write(short address, byte value) {
        if (mapper.catchCpuWrite(address)) {
            mapper.writeCpu(address, value);
            return;
        }
        handleWriteAndNotify(address, value);
    }
}
