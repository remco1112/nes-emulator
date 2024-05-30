package org.example.nes.bus;

import static org.example.nes.utils.UInt.toUint;

public class MirroredMemory implements Memory {
    private final Memory memory;
    private final int size;

    private MirroredMemory(Memory memory, int size) {
        assert size >= memory.getSize();
        this.memory = memory;
        this.size = size;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public byte read(short address) {
        return memory.read(toMirroredAddress(address));
    }

    @Override
    public void write(short address, byte value) {
        memory.write(toMirroredAddress(address), value);
    }

    private short toMirroredAddress(short address) {
        return (short) (toUint(address) % memory.getSize());
    }

    public static MirroredMemory mirrored(int size, Memory memory) {
        return new MirroredMemory(memory, size);
    }
}
