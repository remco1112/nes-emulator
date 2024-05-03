package org.example.nes;

public enum OpCode {
    CPY_IMM((byte) 0xC0, (short) 2),
    CPY_ZPG((byte) 0xC4, (short) 2),
    CPY_ABS((byte) 0xCC, (short) 3),
    CPX_IMM((byte) 0xE0, (short) 2),
    CPX_ZPG((byte) 0xE4, (short) 2),
    CPX_ABS((byte) 0xEC, (short) 3),
    CMP_IMM((byte) 0xC9, (short) 2),
    CMP_ZPG((byte) 0xC5, (short) 2),
    CMP_ABS((byte) 0xCD, (short) 3),
    CMP_ZPX((byte) 0xD5, (short) 2),
    CMP_ABX((byte) 0xDD, (short) 3),
    CMP_ABY((byte) 0xD9, (short) 3),
    CMP_XIN((byte) 0xC1, (short) 2);

    final byte opCode;
    final short size;

    OpCode(byte opCode, short size) {
        this.opCode = opCode;
        this.size = size;
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
