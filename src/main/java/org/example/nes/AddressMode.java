package org.example.nes;

public enum AddressMode {
    ACCUMULATOR(0),
    ABSOLUTE(3),
    ABSOLUTE_X_INDEXED(3),
    ABSOLUTE_Y_INDEXED(3),
    IMMEDIATE(0),
    IMPLIED(0),
    INDIRECT(0),
    X_INDEXED_INDIRECT(0),
    INDIRECT_Y_INDEXED(0),
    RELATIVE(0),
    ZEROPAGE(1),
    ZEROPAGE_X_INDEXED(0),
    ZEROPAGE_Y_INDEXED(0);

    final int cycles;

    AddressMode(int cycles) {
        this.cycles = cycles;
    }
}
