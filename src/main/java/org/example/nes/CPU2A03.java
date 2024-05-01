package org.example.nes;

import java.util.ArrayList;
import java.util.List;

public class CPU2A03 {
    private static final byte IRQ_ADDR = (byte) 0xfffe;
    private static final byte RST_ADDR = (byte) 0xfffc;
    private static final byte NMI_ADDR = (byte) 0xfffa;

    private static final byte STACK_BASE = (byte) 0xff;

    private static final byte BIT_NEGATIVE   = 7;
    private static final byte BIT_OVERFLOW   = 6;
    private static final byte BIT_DECIMAL    = 3;
    private static final byte BIT_INT_DISABLE = 2;
    private static final byte BIT_ZERO        = 1;
    private static final byte BIT_CARRY       = 0;

    private static final byte BITMASK_NEGATIVE   = (byte) (1 << BIT_NEGATIVE);
    private static final byte BITMASK_OVERFLOW   = (byte) (1 << BIT_OVERFLOW);
    private static final byte BITMASK_DECIMAL    = (byte) (1 << BIT_DECIMAL);
    private static final byte BITMASK_INT_DISABLE = (byte) (1 << BIT_INT_DISABLE);
    private static final byte BITMASK_ZERO        = (byte) (1 << BIT_ZERO);
    private static final byte BITMASK_CARRY       = (byte) (1 << BIT_CARRY);

    private static final byte BITMASK_CMP = BITMASK_NEGATIVE | BITMASK_ZERO | BITMASK_CARRY;

    private short regPC;
    private byte regSP;
    private byte regA;
    private byte regX;
    private byte regY;
    private byte regP;

    private final MemoryMap memoryMap;

    private OpCode currentOp = OpCode.CPY_IMM;
    private byte cycleInOp = currentOp.cycles;

    CPU2A03(MemoryMap memoryMap) {
        this(
                memoryMap,
                (short) 0,
                STACK_BASE,
                (byte) 0,
                (byte) 0,
                (byte) 0,
                (byte) 0);
    }

    CPU2A03(MemoryMap memoryMap, short regPC, byte regSP, byte regA, byte regX, byte regY, byte regP) {
        this.memoryMap = memoryMap;
        this.regPC = regPC;
        this.regSP = regSP;
        this.regA = regA;
        this.regX = regX;
        this.regY = regY;
        this.regP = regP;
    }

    public void tick() {
        if (cycleInOp == currentOp.cycles) {
            cycleInOp = 1;
            currentOp = OpCode.fromOpCode(memoryMap.get(regPC));
            return;
        }
        cycleInOp++;
        switch (currentOp) {
            case OpCode.CPY_IMM -> handleCPY_IMM();
        }
    }

    public void tickUntilNextOp() {
        do {
            tick();
        } while (cycleInOp != currentOp.cycles);
    }

    private void handleCPY_IMM() {
        final byte operand = memoryMap.get((short) (regPC + 1));
        final int subtr = Byte.compareUnsigned(regY, operand);
        final byte flags =  (byte) (((subtr >>> 7) << BIT_NEGATIVE)
                | (subtr == 0 ? BITMASK_ZERO : 0)
                | (subtr >= 0 ? BITMASK_CARRY : 0));
        applyCMPFlags(flags);
        regPC += 2;
    }

    private void applyCMPFlags(byte flags) {
        applyFlags(BITMASK_CMP, flags);
    }

    private void applyFlags(byte bitmask, byte flags) {
        regP = (byte) ((regP & ~bitmask) | flags);
    }

    public short getRegPC() {
        return regPC;
    }

    public byte getRegSP() {
        return regSP;
    }

    public byte getRegA() {
        return regA;
    }

    public byte getRegX() {
        return regX;
    }

    public byte getRegY() {
        return regY;
    }

    public byte getRegP() {
        return regP;
    }

    public MemoryMap getMemoryMap() {
        return memoryMap;
    }
}
