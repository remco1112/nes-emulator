package org.example.nes;

public enum AddressMode {
    ACCUMULATOR(0, 1),
    ABSOLUTE(3, 3),
    ABSOLUTE_X_INDEXED(3, 3),
    ABSOLUTE_Y_INDEXED(3, 3),
    IMMEDIATE(0, 2),
    IMPLIED(0, 1),
    INDIRECT(0, 3),
    X_INDEXED_INDIRECT(4, 2),
    INDIRECT_Y_INDEXED(4, 2),
    RELATIVE(0, 2),
    ZEROPAGE(1, 2),
    ZEROPAGE_X_INDEXED(2, 2),
    ZEROPAGE_Y_INDEXED(0, 2);

    final int cycles;
    final int instructionSize;

    AddressMode(int cycles, int instructionSize) {
        this.cycles = cycles;
        this.instructionSize = instructionSize;
    }
}
