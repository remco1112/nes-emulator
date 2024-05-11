package org.example.nes.mapper;

import static org.example.nes.UInt.toUint;

public interface Mapper {

    boolean catchCpuRead(short address);

    boolean catchCpuWrite(short address);

    void notifyCpuRead(short address, byte value);

    void notifyCpuWrite(short address, byte value);

    byte readCpu(short address);

    void writeCpu(short address, byte value);

    boolean catchPpuRead(short address);

    boolean catchPpuWrite(short address);

    void notifyPpuRead(short address, byte value);

    void notifyPpuWrite(short address, byte value);

    byte readPpu(short address);

    void writePpu(short address, byte value);

    static String addrToString(int address) {
        return Integer.toUnsignedString(address, 16);
    }

    static String writeToString(int address, byte value) {
        return addrToString(address)  + " <- " + Integer.toUnsignedString(toUint(value), 16);
    }
}
