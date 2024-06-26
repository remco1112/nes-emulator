package org.example.nes.cpu;

import static org.example.nes.cpu.AddressMode.*;
import static org.example.nes.cpu.Operation.*;

enum OpCode {
    BRK_IMP((byte) 0x00, BRK, IMPLIED),
    ORA_XIN((byte) 0x01, ORA, X_INDEXED_INDIRECT),
    ORA_ZPG((byte) 0x05, ORA, ZEROPAGE),
    ASL_ZPG((byte) 0x06, ASL, ZEROPAGE),
    PHP_IMP((byte) 0x08, PHP, IMPLIED),
    ORA_IMM((byte) 0x09, ORA, IMMEDIATE),
    ASL_ACC((byte) 0x0a, ASL, ACCUMULATOR),
    ORA_ABS((byte) 0x0d, ORA, ABSOLUTE),
    ASL_ABS((byte) 0x0e, ASL, ABSOLUTE),
    BPL_REL((byte) 0x10, BPL, RELATIVE),
    ORA_INY((byte) 0x11, ORA, INDIRECT_Y_INDEXED),
    ORA_ZPX((byte) 0x15, ORA, ZEROPAGE_X_INDEXED),
    ASL_ZPX((byte) 0x16, ASL, ZEROPAGE_X_INDEXED),
    CLC_IMP((byte) 0x18, CLC, IMPLIED),
    ORA_ABY((byte) 0x19, ORA, ABSOLUTE_Y_INDEXED),
    ORA_ABX((byte) 0x1d, ORA, ABSOLUTE_X_INDEXED),
    ASL_ABX((byte) 0x1e, ASL, ABSOLUTE_X_INDEXED),
    JSR_ABS((byte) 0x20, JSR, ABSOLUTE),
    AND_XIN((byte) 0x21, AND, X_INDEXED_INDIRECT),
    BIT_ZPG((byte) 0x24, BIT, ZEROPAGE),
    AND_ZPG((byte) 0x25, AND, ZEROPAGE),
    ROL_ZPG((byte) 0x26, ROL, ZEROPAGE),
    PLP_IMP((byte) 0x28, PLP, IMPLIED),
    AND_IMM((byte) 0x29, AND, IMMEDIATE),
    ROL_ACC((byte) 0x2a, ROL, ACCUMULATOR),
    BIT_ABS((byte) 0x2c, BIT, ABSOLUTE),
    AND_ABS((byte) 0x2d, AND, ABSOLUTE),
    ROL_ABS((byte) 0x2e, ROL, ABSOLUTE),
    BMI_REL((byte) 0x30, BMI, RELATIVE),
    AND_INY((byte) 0x31, AND, INDIRECT_Y_INDEXED),
    AND_ZPX((byte) 0x35, AND, ZEROPAGE_X_INDEXED),
    ROL_ZPX((byte) 0x36, ROL, ZEROPAGE_X_INDEXED),
    SEC_IMP((byte) 0x38, SEC, IMPLIED),
    AND_ABY((byte) 0x39, AND, ABSOLUTE_Y_INDEXED),
    AND_ABX((byte) 0x3d, AND, ABSOLUTE_X_INDEXED),
    ROL_ABX((byte) 0x3e, ROL, ABSOLUTE_X_INDEXED),
    RTI_IMP((byte) 0x40, RTI, IMPLIED),
    EOR_XIN((byte) 0x41, EOR, X_INDEXED_INDIRECT),
    EOR_ZPG((byte) 0x45, EOR, ZEROPAGE),
    LSR_ZPG((byte) 0x46, LSR, ZEROPAGE),
    PHA_IMP((byte) 0x48, PHA, IMPLIED),
    EOR_IMM((byte) 0x49, EOR, IMMEDIATE),
    LSR_ACC((byte) 0x4a, LSR, ACCUMULATOR),
    JMP_ABS((byte) 0x4c, JMP, ABSOLUTE),
    EOR_ABS((byte) 0x4d, EOR, ABSOLUTE),
    LSR_ABS((byte) 0x4e, LSR, ABSOLUTE),
    BVC_REL((byte) 0x50, BVC, RELATIVE),
    EOR_INY((byte) 0x51, EOR, INDIRECT_Y_INDEXED),
    EOR_ZPX((byte) 0x55, EOR, ZEROPAGE_X_INDEXED),
    LSR_ZPX((byte) 0x56, LSR, ZEROPAGE_X_INDEXED),
    CLI_IMP((byte) 0x58, CLI, IMPLIED),
    EOR_ABY((byte) 0x59, EOR, ABSOLUTE_Y_INDEXED),
    EOR_ABX((byte) 0x5d, EOR, ABSOLUTE_X_INDEXED),
    LSR_ABX((byte) 0x5e, LSR, ABSOLUTE_X_INDEXED),
    RTS_IMP((byte) 0x60, RTS, IMPLIED),
    ADC_XIN((byte) 0x61, ADC, X_INDEXED_INDIRECT),
    ADC_ZPG((byte) 0x65, ADC, ZEROPAGE),
    ROR_ZPG((byte) 0x66, ROR, ZEROPAGE),
    PLA_IMP((byte) 0x68, PLA, IMPLIED),
    ADC_IMM((byte) 0x69, ADC, IMMEDIATE),
    ROR_ACC((byte) 0x6a, ROR, ACCUMULATOR),
    JMP_IND((byte) 0x6c, JMP, INDIRECT),
    ADC_ABS((byte) 0x6d, ADC, ABSOLUTE),
    ROR_ABS((byte) 0x6e, ROR, ABSOLUTE),
    BVS_REL((byte) 0x70, BVS, RELATIVE),
    ADC_INY((byte) 0x71, ADC, INDIRECT_Y_INDEXED),
    ADC_ZPX((byte) 0x75, ADC, ZEROPAGE_X_INDEXED),
    ROR_ZPX((byte) 0x76, ROR, ZEROPAGE_X_INDEXED),
    SEI_IMP((byte) 0x78, SEI, IMPLIED),
    ADC_ABY((byte) 0x79, ADC, ABSOLUTE_Y_INDEXED),
    ADC_ABX((byte) 0x7d, ADC, ABSOLUTE_X_INDEXED),
    ROR_ABX((byte) 0x7e, ROR, ABSOLUTE_X_INDEXED),
    STA_XIN((byte) 0x81, STA, X_INDEXED_INDIRECT),
    STY_ZPG((byte) 0x84, STY, ZEROPAGE),
    STA_ZPG((byte) 0x85, STA, ZEROPAGE),
    STX_ZPG((byte) 0x86, STX, ZEROPAGE),
    DEY_IMP((byte) 0x88, DEY, IMPLIED),
    TXA_IMP((byte) 0x8a, TXA, IMPLIED),
    STY_ABS((byte) 0x8c, STY, ABSOLUTE),
    STA_ABS((byte) 0x8d, STA, ABSOLUTE),
    STX_ABS((byte) 0x8e, STX, ABSOLUTE),
    BCC_REL((byte) 0x90, BCC, RELATIVE),
    STA_INY((byte) 0x91, STA, INDIRECT_Y_INDEXED),
    STY_ZPX((byte) 0x94, STY, ZEROPAGE_X_INDEXED),
    STA_ZPX((byte) 0x95, STA, ZEROPAGE_X_INDEXED),
    STX_ZPY((byte) 0x96, STX, ZEROPAGE_Y_INDEXED),
    TYA_IMP((byte) 0x98, TYA, IMPLIED),
    STA_ABY((byte) 0x99, STA, ABSOLUTE_Y_INDEXED),
    TXS_IMP((byte) 0x9a, TXS, IMPLIED),
    STA_ABX((byte) 0x9d, STA, ABSOLUTE_X_INDEXED),
    LDY_IMM((byte) 0xa0, LDY, IMMEDIATE),
    LDA_XIN((byte) 0xa1, LDA, X_INDEXED_INDIRECT),
    LDX_IMM((byte) 0xa2, LDX, IMMEDIATE),
    LDY_ZPG((byte) 0xa4, LDY, ZEROPAGE),
    LDA_ZPG((byte) 0xa5, LDA, ZEROPAGE),
    LDX_ZPG((byte) 0xa6, LDX, ZEROPAGE),
    TAY_IMP((byte) 0xa8, TAY, IMPLIED),
    LDA_IMM((byte) 0xa9, LDA, IMMEDIATE),
    TAX_IMP((byte) 0xaa, TAX, IMPLIED),
    LDY_ABS((byte) 0xac, LDY, ABSOLUTE),
    LDA_ABS((byte) 0xad, LDA, ABSOLUTE),
    LDX_ABS((byte) 0xae, LDX, ABSOLUTE),
    BCS_REL((byte) 0xb0, BCS, RELATIVE),
    LDA_INY((byte) 0xb1, LDA, INDIRECT_Y_INDEXED),
    LDY_ZPX((byte) 0xb4, LDY, ZEROPAGE_X_INDEXED),
    LDA_ZPX((byte) 0xb5, LDA, ZEROPAGE_X_INDEXED),
    LDX_ZPY((byte) 0xb6, LDX, ZEROPAGE_Y_INDEXED),
    CLV_IMP((byte) 0xb8, CLV, IMPLIED),
    LDA_ABY((byte) 0xb9, LDA, ABSOLUTE_Y_INDEXED),
    TSX_IMP((byte) 0xba, TSX, IMPLIED),
    LDY_ABX((byte) 0xbc, LDY, ABSOLUTE_X_INDEXED),
    LDA_ABX((byte) 0xbd, LDA, ABSOLUTE_X_INDEXED),
    LDX_ABY((byte) 0xbe, LDX, ABSOLUTE_Y_INDEXED),
    CPY_IMM((byte) 0xc0, CPY, IMMEDIATE),
    CMP_XIN((byte) 0xc1, CMP, X_INDEXED_INDIRECT),
    CPY_ZPG((byte) 0xc4, CPY, ZEROPAGE),
    CMP_ZPG((byte) 0xc5, CMP, ZEROPAGE),
    DEC_ZPG((byte) 0xc6, DEC, ZEROPAGE),
    INY_IMP((byte) 0xc8, INY, IMPLIED),
    CMP_IMM((byte) 0xc9, CMP, IMMEDIATE),
    DEX_IMP((byte) 0xca, DEX, IMPLIED),
    CPY_ABS((byte) 0xcc, CPY, ABSOLUTE),
    CMP_ABS((byte) 0xcd, CMP, ABSOLUTE),
    DEC_ABS((byte) 0xce, DEC, ABSOLUTE),
    BNE_REL((byte) 0xd0, BNE, RELATIVE),
    CMP_INY((byte) 0xd1, CMP, INDIRECT_Y_INDEXED),
    CMP_ZPX((byte) 0xd5, CMP, ZEROPAGE_X_INDEXED),
    DEC_ZPX((byte) 0xd6, DEC, ZEROPAGE_X_INDEXED),
    CLD_IMP((byte) 0xd8, CLD, IMPLIED),
    CMP_ABY((byte) 0xd9, CMP, ABSOLUTE_Y_INDEXED),
    CMP_ABX((byte) 0xdd, CMP, ABSOLUTE_X_INDEXED),
    DEC_ABX((byte) 0xde, DEC, ABSOLUTE_X_INDEXED),
    CPX_IMM((byte) 0xe0, CPX, IMMEDIATE),
    SBC_XIN((byte) 0xe1, SBC, X_INDEXED_INDIRECT),
    CPX_ZPG((byte) 0xe4, CPX, ZEROPAGE),
    SBC_ZPG((byte) 0xe5, SBC, ZEROPAGE),
    INC_ZPG((byte) 0xe6, INC, ZEROPAGE),
    INX_IMP((byte) 0xe8, INX, IMPLIED),
    SBC_IMM((byte) 0xe9, SBC, IMMEDIATE),
    NOP_IMP((byte) 0xea, NOP, IMPLIED),
    CPX_ABS((byte) 0xec, CPX, ABSOLUTE),
    SBC_ABS((byte) 0xed, SBC, ABSOLUTE),
    INC_ABS((byte) 0xee, INC, ABSOLUTE),
    BEQ_REL((byte) 0xf0, BEQ, RELATIVE),
    SBC_INY((byte) 0xf1, SBC, INDIRECT_Y_INDEXED),
    SBC_ZPX((byte) 0xf5, SBC, ZEROPAGE_X_INDEXED),
    INC_ZPX((byte) 0xf6, INC, ZEROPAGE_X_INDEXED),
    SED_IMP((byte) 0xf8, SED, IMPLIED),
    SBC_ABY((byte) 0xf9, SBC, ABSOLUTE_Y_INDEXED),
    SBC_ABX((byte) 0xfd, SBC, ABSOLUTE_X_INDEXED),
    INC_ABX((byte) 0xfe, INC, ABSOLUTE_X_INDEXED);

    final byte opCode;
    final Operation operation;
    final AddressMode addressMode;

    OpCode(byte opCode, Operation operation, AddressMode addressMode) {
        this.opCode = opCode;
        this.operation = operation;
        this.addressMode = addressMode;
    }

    private static final OpCode[] MAP = new OpCode[0x100];

    static {
        for (OpCode opCode : OpCode.values()) {
            MAP[Byte.toUnsignedInt(opCode.opCode)] = opCode;
        }
    }

    static OpCode fromOpCode(byte opCode) {
        return MAP[Byte.toUnsignedInt(opCode)];
    }
}
