package org.example.nes.bus;

import static org.example.nes.utils.UInt.toUint;

public class MappedMemory implements Memory {
    private final short startAddress;
    private final Memory memory;

    private MappedMemory(short startAddress, Memory memory) {
        this.startAddress = startAddress;
        this.memory = memory;
    }

    @Override
    public int getSize() {
        return memory.getSize();
    }

    @Override
    public byte read(short address) {
        return memory.read(toInternalAddress(address));
    }

    @Override
    public void write(short address, byte value) {
        memory.write(toInternalAddress(address), value);
    }

    private short toInternalAddress(short address) {
        assert isAddressInRange(address);
        return (short) (address - startAddress);
    }

    boolean isAddressInRange(short address) {
        return toUint(address) >= toUint(startAddress) && toUint(address) < toUint(startAddress) + getSize();
    }

    public static MappedMemory mapAt(short startAddress, Memory memory) {
        return new MappedMemory(startAddress, memory);
    }
}
