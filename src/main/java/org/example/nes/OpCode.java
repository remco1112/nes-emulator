package org.example.nes;

public enum OpCode {
    CPY_IMM((byte) 0xC0, (byte) 2, (byte) 2);

    final byte opCode;
    final byte cycles;
    final byte size;

    OpCode(byte opCode, byte cycles, byte size) {
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
