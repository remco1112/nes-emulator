package org.example.nes.mapper;

public interface Mapper {

    boolean catchRead(short address);

    boolean catchWrite(short address);

    void notifyRead(short address, byte value);

    void notifyWrite(short address, byte value);

    byte read(short address);

    void write(short address, byte value);
}
