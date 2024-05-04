package org.example.nes;

import static org.example.nes.AddressMode.*;
import static org.example.nes.Operation.*;

public enum OpCode {
    CPY_IMM((byte) 0xC0, CPY, IMMEDIATE),
    CPY_ZPG((byte) 0xC4, CPY, ZEROPAGE),
    CPY_ABS((byte) 0xCC, CPY, ABSOLUTE),
    CPX_IMM((byte) 0xE0, CPX, IMMEDIATE),
    CPX_ZPG((byte) 0xE4, CPX, ZEROPAGE),
    CPX_ABS((byte) 0xEC, CPX, ABSOLUTE),
    CMP_IMM((byte) 0xC9, CMP, IMMEDIATE),
    CMP_ZPG((byte) 0xC5, CMP, ZEROPAGE),
    CMP_ABS((byte) 0xCD, CMP, ABSOLUTE),
    CMP_ZPX((byte) 0xD5, CMP, ZEROPAGE_X_INDEXED),
    CMP_ABX((byte) 0xDD, CMP, ABSOLUTE_X_INDEXED),
    CMP_ABY((byte) 0xD9, CMP, ABSOLUTE_Y_INDEXED),
    CMP_XIN((byte) 0xC1, CMP, X_INDEXED_INDIRECT),
    CMP_INY((byte) 0xD1, CMP, INDIRECT_Y_INDEXED);

    final byte opCode;
    final Operation operation;
    final AddressMode addressMode;

    OpCode(byte opCode, Operation operation, AddressMode addressMode) {
        this.opCode = opCode;
        this.operation = operation;
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
