package org.example.nes.bus;

import static org.example.nes.utils.StringUtils.addressToString;
import static org.example.nes.utils.StringUtils.writeToString;

public abstract class AbstractBus implements Bus {
    private final BusConfiguration busConfiguration;
    private final int size;

    protected AbstractBus(BusConfiguration busConfiguration) {
        this.busConfiguration = busConfiguration;
        this.size = busConfiguration.getMappedMemoryList()
                .stream()
                .mapToInt(MappedMemory::getSize)
                .sum();
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public byte read(short address) {
        for (MappedMemory mappedMemory : busConfiguration.getMappedMemoryList()) {
            if (mappedMemory.isAddressInRange(address)) {
                return readAndNotify(address, mappedMemory);
            }
        }
        System.out.println("Warning: received unmapped read: " + addressToString(address));
        return 0;
    }

    private byte readAndNotify(short address, Memory memory) {
        final byte value = memory.read(address);
        for (ReadListener readListener : busConfiguration.getReadListeners()) {
            readListener.onRead(address, value);
        }
        return value;
    }

    @Override
    public void write(short address, byte value) {
        for (MappedMemory mappedMemory : busConfiguration.getMappedMemoryList()) {
            if (mappedMemory.isAddressInRange(address)) {
                writeAndNotify(address, value, mappedMemory);
                return;
            }
        }
        System.out.println("Warning: received unmapped write: " + writeToString(address, value));
    }

    private void writeAndNotify(short address, byte value, Memory writableMemory) {
        writableMemory.write(address, value);
        for (WriteListener writeListener : busConfiguration.getWriteListeners()) {
            writeListener.onWrite(address, value);
        }
    }
}
