package org.example.nes.cpu;

enum Operation {
    LDA,
    LDX,
    LDY,
    STA(true),
    STX(true),
    STY(true),
    TAX,
    TAY,
    TSX,
    TXA,
    TXS,
    TYA,
    PHA(true),
    PHP(true),
    PLA,
    PLP,
    DEC(true),
    DEX,
    DEY,
    INC(true),
    INX,
    INY,
    ADC,
    SBC,
    AND,
    EOR,
    ORA,
    ASL(true),
    LSR(true),
    ROL(true),
    ROR(true),
    CLC,
    CLD,
    CLI,
    CLV,
    SEC,
    SED,
    SEI,
    CMP,
    CPX,
    CPY,
    BCC,
    BCS,
    BEQ,
    BMI,
    BNE,
    BPL,
    BVC,
    BVS,
    JMP(true),
    JSR(true),
    RTS,
    BRK,
    RTI,
    BIT,
    NOP;

    final boolean writesToMemory;

    Operation() {
        this(false);
    }

    Operation(boolean writesToMemory) {
        this.writesToMemory = writesToMemory;
    }
}
