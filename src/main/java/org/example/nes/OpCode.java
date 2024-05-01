package org.example.nes;

public enum OpCode {
    CPY_IMM((byte) 0xC0, 2, (short) 2),
    CPY_ZPG((byte) 0xC4, 3, (short) 2),
    CPY_ABS((byte) 0xCC, 4, (short) 3),
    CPX_IMM((byte) 0xE0, 2, (short) 2),
    CPX_ZPG((byte) 0xE4, 3, (short) 2),
    CPX_ABS((byte) 0xEC, 4, (short) 3),
    CMP_IMM((byte) 0xC9, 2, (short) 2),
    CMP_ZPG((byte) 0xC5, 3, (short) 2),
    CMP_ABS((byte) 0xCD, 4, (short) 3);

    final byte opCode;
    final int cycles;
    final short size;

    OpCode(byte opCode, int cycles, short size) {
        this.opCode = opCode;
        this.cycles = cycles;
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
