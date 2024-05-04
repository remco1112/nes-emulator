package org.example.nes;

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
    private int cycleInInstruction = 0;

    private short operandAddress;

    private byte op0;
    private byte op1;
    private short indirectAddress;

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
        if (cycleInInstruction == 0) {
            fetchOperation();
        } else if (cycleInInstruction - 1 < currentOp.addressMode.cycles) {
            handleAddressing();
        } else {
            handleOperation();
        }
        cycleInInstruction++;
    }

    private void handleAddressing() {
        switch (currentOp.addressMode) {
            case ABSOLUTE -> handleAddressingAbsolute();
            case ABSOLUTE_X_INDEXED -> handleAddressingAbsoluteXIndexed();
            case ABSOLUTE_Y_INDEXED -> handleAddressingAbsoluteYIndexed();
            case X_INDEXED_INDIRECT -> handleAddressingXIndexedIndirect();
            case INDIRECT_Y_INDEXED -> handleAddressingIndirectYIndexed();
            case ZEROPAGE -> handleAddressingZeroPage();
            case ZEROPAGE_X_INDEXED -> handleAddressingZeroPageXIndexed();
            default -> throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    private void handleAddressingAbsolute() {
        handleAddressingAbsolute((byte) 0);
    }

    private void handleAddressingAbsoluteXIndexed() {
        handleAddressingAbsolute(regX);
    }

    private void handleAddressingAbsoluteYIndexed() {
        handleAddressingAbsolute(regY);
    }

    private void handleAddressingAbsolute(byte offset) {
        switch (getCycleInAddressing()) {
            case 0 -> fetchOperand0();
            case 1 -> {
                fetchOperand1();
                operandAddress = getAddressFromOperandsAndOffsetWithCarry(offset);
                if (addressInPage(operandAddress)) {
                    cycleInInstruction++;
                }
            }
            case 2 -> memoryMap.get(subtractPage(operandAddress));
        }
    }

    private void handleAddressingZeroPage() {
        fetchOperand0();
        operandAddress = (short) toUint(op0);
    }

    private void handleAddressingZeroPageXIndexed() {
        if (getCycleInAddressing() == 0) {
            fetchOperand0();
        } else {
            memoryMap.get((short) toUint(op0));
            operandAddress = getZeroPageAddress(regX);
        }
    }

    private void handleAddressingXIndexedIndirect() {
        switch (getCycleInAddressing()) {
            case 0 -> fetchOperand0();
            case 1 -> memoryMap.get((short) toUint(op0));
            case 2 -> operandAddress = (short) toUint(memoryMap.get(getZeroPageAddress(regX)));
            case 3 -> operandAddress = (short) (toUint(operandAddress) | (toUint(memoryMap.get((short) ((toUint(getZeroPageAddress(regX)) + 1) % 0x100))) << 8));
        }
    }

    private void handleAddressingIndirectYIndexed() {
        switch (getCycleInAddressing()) {
            case 0 -> fetchOperand0();
            case 1 -> operandAddress = (short) toUint(memoryMap.get(getZeroPageAddress()));
            case 2 -> {
                operandAddress = (short) (toUint(operandAddress) | (toUint(memoryMap.get((short) ((toUint(getZeroPageAddress()) + 1) % 0x100))) << 8));
                if (toUint(operandAddress) >>> 8 == (toUint(operandAddress) + toUint(regY)) >> 8) {
                    cycleInInstruction++;
                }
                operandAddress = (short) (toUint(operandAddress) + toUint(regY));
            }
            case 3 -> memoryMap.get(subtractPage(operandAddress));
        }
    }

    private int getCycleInAddressing() {
        return cycleInInstruction - 1;
    }

    private int getCycleInOperation() {
        return cycleInInstruction - 1 - currentOp.addressMode.cycles;
    }

    private void handleOperation() {
        switch (currentOp) {
            case OpCode.CPY_IMM -> handleCPY_IMM();
            case OpCode.CPY_ZPG -> handleCPY_ZPG();
            case OpCode.CPY_ABS -> handleCPY_ABS();
            case OpCode.CPX_IMM -> handleCPX_IMM();
            case OpCode.CPX_ZPG -> handleCPX_ZPG();
            case OpCode.CPX_ABS -> handleCPX_ABS();
            case OpCode.CMP_IMM -> handleCMP_IMM();
            case OpCode.CMP_ZPG -> handleCMP_ZPG();
            case OpCode.CMP_ABS -> handleCMP_ABS();
            case OpCode.CMP_ZPX -> handleCMP_ZPX();
            case OpCode.CMP_ABX -> handleCMP_ABX();
            case OpCode.CMP_ABY -> handleCMP_ABY();
            case OpCode.CMP_XIN -> handleCMP_XIN();
            case OpCode.CMP_INY -> handleCMP_YIN();
        }
    }

    private void fetchOperation() {
        currentOp = OpCode.fromOpCode(memoryMap.get(regPC));
        operandAddress = (short) (toUint(regPC) + 1);
    }

    public int tickUntilNextOp() {
        int cycles = 0;
        do {
            tick();
            cycles++;
        } while (cycleInInstruction != 0);
        return cycles;
    }

    private void handleCompare_ZPG(byte reg) {
        handleCompareFinalCycleAbsolute(reg, operandAddress);
    }

    private void handleCMP_ZPX() {
        handleCompareFinalCycleAbsolute(regA, operandAddress);
    }

    private void handleCMP_ABX() {
        handleCompareAbsolute(regA);
    }

    private void handleCMP_ABY() {
        handleCompareAbsolute(regA);
    }

    private void handleCMP_ABS() {
        handleCompareAbsolute(regA);
    }

    private void handleCMP_ZPG() {
        handleCompare_ZPG(regA);
    }

    private void handleCMP_IMM() {
        handleCompare(regA);
    }

    private void handleCPX_ABS() {
        handleCompareAbsolute(regX);
    }

    private void handleCPX_ZPG() {
        handleCompare_ZPG(regX);
    }

    private void handleCPX_IMM() {
        handleCompare(regX);
    }

    private void handleCPY_ABS() {
        handleCompareAbsolute(regY);
    }

    private void handleCPY_ZPG() {
        handleCompare_ZPG(regY);
    }

    private void handleCPY_IMM() {
        handleCompare(regY);
    }

    private void handleCMP_XIN() {
        handleCompareFinalCycleAbsolute(regA, operandAddress);
    }

    private void handleCMP_YIN() {
        handleCompareFinalCycleAbsolute(regA, operandAddress);
    }

    private void handleCompareAbsolute(byte comparisonTarget) {
        handleCompareFinalCycleAbsolute(comparisonTarget, operandAddress);
    }

    private short subtractPage(short address) {
        return (short) (toUint(address) - 0x100);
    }

    private short getAddressFromOperands() {
        return getAddressFromOperandsAndOffsetWithoutCarry((byte) 0);
    }

    private boolean addressInPage(short address) {
        return ((byte) (toUint(address) >>> 8)) == op1;
    }

    private short getAddressFromOperandsAndOffsetWithCarry(byte offset) {
        final int op0WithOffset = toUint(op0) + toUint(offset);
        return (short) (op0WithOffset + (toUint(op1) << 8));
    }

    private short getAddressFromOperandsAndOffsetWithoutCarry(byte offset) {
        return getAddressFromOperandsAndOffsetWithoutCarry(offset, op1);
    }

    private short getZeroPageAddress() {
        return getZeroPageAddress((byte) 0);
    }

    private short getZeroPageAddress(byte offset) {
        return getAddressFromOperandsAndOffsetWithoutCarry(offset, (byte) 0);
    }

    private short getAddressFromOperandsAndOffsetWithoutCarry(byte offset, byte operand1) {
        final int op0WithOffset = toUint(op0) + toUint(offset);
        return (short) ((op0WithOffset % 0x100) | (toUint(operand1) << 8));
    }

    private void fetchOperand0() {
        op0 = memoryMap.get((short) (regPC + 1));
    }

    private void fetchOperand1() {
        op1 = memoryMap.get((short) (regPC + 2));
    }

    private int toUint(byte byteValue) {
        return Byte.toUnsignedInt(byteValue);
    }

    private int toUint(short shortValue) {
        return Short.toUnsignedInt(shortValue);
    }
    
    private void handleCompareFinalCycleAbsolute(byte reg, short operandAddress) {
        this.operandAddress = operandAddress;
        handleCompare(reg);
    }

    private void handleCompare(byte reg) {
        final int subtr = Byte.compareUnsigned(reg, memoryMap.get(operandAddress));
        final byte flags =  (byte) (((subtr >>> 7) << BIT_NEGATIVE)
                | (subtr == 0 ? BITMASK_ZERO : 0)
                | (subtr >= 0 ? BITMASK_CARRY : 0));
        applyCompareFlags(flags);
        incrementPC();
        resetCycleInOp();
    }

    private void applyCompareFlags(byte flags) {
        applyFlags(BITMASK_CMP, flags);
    }

    private void applyFlags(byte bitmask, byte flags) {
        regP = (byte) ((regP & ~bitmask) | flags);
    }

    private void resetCycleInOp() {
        cycleInInstruction = -1;
    }

    private void incrementPC() {
        regPC += currentOp.size;
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
