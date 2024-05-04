package org.example.nes;

import static org.example.nes.AddressMode.*;

public enum OpCode {
    CPY_IMM((byte) 0xC0, (short) 2, IMMEDIATE),
    CPY_ZPG((byte) 0xC4, (short) 2, ZEROPAGE),
    CPY_ABS((byte) 0xCC, (short) 3, ABSOLUTE),
    CPX_IMM((byte) 0xE0, (short) 2, IMMEDIATE),
    CPX_ZPG((byte) 0xE4, (short) 2, ZEROPAGE),
    CPX_ABS((byte) 0xEC, (short) 3, ABSOLUTE),
    CMP_IMM((byte) 0xC9, (short) 2, IMMEDIATE),
    CMP_ZPG((byte) 0xC5, (short) 2, ZEROPAGE),
    CMP_ABS((byte) 0xCD, (short) 3, ABSOLUTE),
    CMP_ZPX((byte) 0xD5, (short) 2, ZEROPAGE_X_INDEXED),
    CMP_ABX((byte) 0xDD, (short) 3, ABSOLUTE_X_INDEXED),
    CMP_ABY((byte) 0xD9, (short) 3, ABSOLUTE_Y_INDEXED),
    CMP_XIN((byte) 0xC1, (short) 2, X_INDEXED_INDIRECT),
    CMP_INY((byte) 0xD1, (short) 2, INDIRECT_Y_INDEXED);

    final byte opCode;
    final short size;
    final AddressMode addressMode;

    OpCode(byte opCode, short size, AddressMode addressMode) {
        this.opCode = opCode;
        this.size = size;
        this.addressMode = addressMode;
    }

    private static final OpCode[] MAP = new OpCode[0xff];

    static {
        for (OpCode opCode : OpCode.values()) {
            MAP[Byte.toUnsignedInt(opCode.opCode)] = opCode;
        }
    }

    static OpCode fromOpCode(byte opCode) {
        return MAP[Byte.toUnsignedInt(opCode)];
    }
}
