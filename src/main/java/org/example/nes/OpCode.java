package org.example.nes;

import static org.example.nes.AddressMode.*;

public enum OpCode {
    CPY_IMM((byte) 0xC0, IMMEDIATE),
    CPY_ZPG((byte) 0xC4, ZEROPAGE),
    CPY_ABS((byte) 0xCC, ABSOLUTE),
    CPX_IMM((byte) 0xE0, IMMEDIATE),
    CPX_ZPG((byte) 0xE4, ZEROPAGE),
    CPX_ABS((byte) 0xEC, ABSOLUTE),
    CMP_IMM((byte) 0xC9, IMMEDIATE),
    CMP_ZPG((byte) 0xC5, ZEROPAGE),
    CMP_ABS((byte) 0xCD, ABSOLUTE),
    CMP_ZPX((byte) 0xD5, ZEROPAGE_X_INDEXED),
    CMP_ABX((byte) 0xDD, ABSOLUTE_X_INDEXED),
    CMP_ABY((byte) 0xD9, ABSOLUTE_Y_INDEXED),
    CMP_XIN((byte) 0xC1, X_INDEXED_INDIRECT),
    CMP_INY((byte) 0xD1, INDIRECT_Y_INDEXED);

    final byte opCode;
    final AddressMode addressMode;

    OpCode(byte opCode, AddressMode addressMode) {
        this.opCode = opCode;
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
