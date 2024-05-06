package org.example.nes;

public enum AddressMode {
    ACCUMULATOR(0, 1, "ACC"),
    ABSOLUTE(3, 3, "ABS"),
    ABSOLUTE_X_INDEXED(3, 3, "ABX"),
    ABSOLUTE_Y_INDEXED(3, 3, "ABY"),
    IMMEDIATE(0, 2, "IMM"),
    IMPLIED(0, 1, "IMP"),
    INDIRECT(4, 3, "IND"),
    X_INDEXED_INDIRECT(4, 2, "XIN"),
    INDIRECT_Y_INDEXED(4, 2, "INY"),
    RELATIVE(0, 2, "REL"),
    ZEROPAGE(1, 2, "ZPG"),
    ZEROPAGE_X_INDEXED(2, 2, "ZPX"),
    ZEROPAGE_Y_INDEXED(2, 2, "ZPY");

    final int cycles;
    final int instructionSize;
    final String shortName;

    AddressMode(int cycles, int instructionSize, String shortName) {
        this.cycles = cycles;
        this.instructionSize = instructionSize;
        this.shortName = shortName;
    }
}
