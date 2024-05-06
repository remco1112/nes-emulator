package org.example.nes;

public class CPU2A03 {
    private static final short IRQ_LO_ADDR = (short) 0xfffe;
    private static final short IRQ_HI_ADDR = (short) 0xffff;
    private static final short RST_ADDR = (short) 0xfffc;
    private static final short NMI_ADDR = (short) 0xfffa;

    private static final byte STACK_BASE = (byte) 0xff;

    private static final byte BIT_NEGATIVE    = 7;
    private static final byte BIT_OVERFLOW    = 6;
    private static final byte BIT_DECIMAL     = 3;
    private static final byte BIT_BREAK       = 4;
    private static final byte BIT_INT_DISABLE = 2;
    private static final byte BIT_ZERO        = 1;
    private static final byte BIT_CARRY       = 0;

    private static final byte BITMASK_NEGATIVE    = (byte) (1 << BIT_NEGATIVE);
    private static final byte BITMASK_OVERFLOW    = (byte) (1 << BIT_OVERFLOW);
    private static final byte BITMASK_DECIMAL     = (byte) (1 << BIT_DECIMAL);
    private static final byte BITMASK_BREAK       = (byte) (1 << BIT_BREAK);
    private static final byte BITMASK_INT_DISABLE = (byte) (1 << BIT_INT_DISABLE);
    private static final byte BITMASK_ZERO        = (byte) (1 << BIT_ZERO);
    private static final byte BITMASK_CARRY       = (byte) (1 << BIT_CARRY);

    private static final byte BITMASK_ZN = BITMASK_ZERO | BITMASK_NEGATIVE;
    private static final byte BITMASK_ZNC = BITMASK_ZN | BITMASK_CARRY;
    private static final byte BITMASK_ZNCV = BITMASK_ZNC | BITMASK_OVERFLOW;
    private static final byte BITMASK_NV = BITMASK_NEGATIVE | BITMASK_OVERFLOW;
    private static final byte BITMASK_ZNV = BITMASK_ZERO | BITMASK_NV;

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
            case INDIRECT -> handleAddressingIndirect();
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
                if (currentOp.operation == Operation.JSR) { // Hack: JSR uses absolute addressing but different cycles
                    memoryMap.get(getStackAddress());
                    cycleInInstruction++;
                    return;
                }
                fetchOperand1();
                operandAddress = getAddressFromOperandsAndOffsetWithCarry(offset);
                if (currentOp.operation == Operation.JMP) { // Hack: Handling jump during addressing since JMP has no operation cycles
                    regPC = operandAddress;
                    resetCycleInOp();
                    return;
                }
                if (shouldSkipCycleAbsolute()) {
                    cycleInInstruction++;
                }
            }
            case 2 -> memoryMap.get(addressInPage(operandAddress) ? operandAddress : subtractPage(operandAddress));
        }
    }

    private boolean shouldSkipCycleAbsolute() {
        return currentOp.addressMode == AddressMode.ABSOLUTE ||
                (addressInPage(operandAddress) && !currentOp.operation.writesToMemory);
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
            case 3 -> operandAddress = getAddressFromOperands((byte) operandAddress, memoryMap.get((short) (toUint(getZeroPageAddress((byte) (toUint(regX) + 1))))));
        }
    }

    private void handleAddressingIndirectYIndexed() {
        switch (getCycleInAddressing()) {
            case 0 -> fetchOperand0();
            case 1 -> operandAddress = (short) toUint(memoryMap.get(getZeroPageAddress((byte) 0)));
            case 2 -> {
                operandAddress = getAddressFromOperands((byte) operandAddress, memoryMap.get((short) (toUint(getZeroPageAddress((byte) 1)))));
                if (samePage(operandAddress, (short) (toUint(operandAddress) + toUint(regY)))) {
                    cycleInInstruction++;
                }
                operandAddress = (short) (toUint(operandAddress) + toUint(regY));
            }
            case 3 -> memoryMap.get(subtractPage(operandAddress));
        }
    }

    private void handleAddressingIndirect() { // Hack: immediately handles JMP operation since JMP has no op cycles
        switch (getCycleInAddressing()) {
            case 0 -> fetchOperand0();
            case 1 -> {
                fetchOperand1();
                operandAddress = getAddressFromOperands();
            }
            case 2 -> op0 = memoryMap.get(operandAddress);
            case 3 -> {
                op1 = memoryMap.get(getAddressFromAddressAndOffsetWithoutCarry((byte) 1, operandAddress));
                regPC = getAddressFromOperands();
                resetCycleInOp();
            }
        }
    }


    private int getCycleInAddressing() {
        return cycleInInstruction - 1;
    }

    private int getCycleInOperation() {
        return cycleInInstruction - 1 - currentOp.addressMode.cycles;
    }

    private void handleOperation() {
        switch (currentOp.operation) {
            case ADC -> handleADC();
            case AND -> handleAND();
            case ASL -> handleASL();
            case BCC -> handleBCC();
            case BCS -> handleBCS();
            case BEQ -> handleBEQ();
            case BIT -> handleBIT();
            case BMI -> handleBMI();
            case BNE -> handleBNE();
            case BPL -> handleBPL();
            case BRK -> handleBRK();
            case BVC -> handleBVC();
            case BVS -> handleBVS();
            case CLC -> handleCLC();
            case CLD -> handleCLD();
            case CLI -> handleCLI();
            case CLV -> handleCLV();
            case CMP -> handleCMP();
            case CPX -> handleCPX();
            case CPY -> handleCPY();
            case DEC -> handleDEC();
            case DEX -> handleDEX();
            case DEY -> handleDEY();
            case EOR -> handleEOR();
            case INC -> handleINC();
            case INX -> handleINX();
            case INY -> handleINY();
            case JMP -> handleJMP();
            case JSR -> handleJSR();
            default -> {
                nextOp();
            }
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

    private void handleADC() {
        final int operand = toUint(memoryMap.get(operandAddress));
        int res = toUint(regA) + operand + (isCarrySet() ? 1 : 0);
        final byte flags =  (byte) (getFlagsZNC(res)
                | ((toUint(regA) >>> 7 == operand >>> 7) && (operand >>> 7 != toUint((byte) res) >>> 7) ? BITMASK_OVERFLOW : 0)
        );
        regA = (byte) res;
        applyFlags(BITMASK_ZNCV, flags);
        nextOp();
    }

    private void handleAND() {
        regA = (byte) (toUint(regA) & toUint(memoryMap.get(operandAddress)));
        applyFlagsZN(regA);
        nextOp();
    }

    private void handleASL() {
        switch (getCycleInOperation()) {
            case 0 -> {
                op0 = memoryMap.get(operandAddress);
                if (currentOp.addressMode == AddressMode.ACCUMULATOR) {
                    final int res = toUint(regA) << 1;
                    applyFlagsZNC(res);
                    regA = (byte) res;
                    nextOp();
                }
            }
            case 1 -> memoryMap.set(operandAddress, op0);
            case 2 -> {
                final int res = toUint(op0) << 1;
                memoryMap.set(operandAddress, (byte) res);
                applyFlagsZNC(res);
                nextOp();
            }
        }
    }

    private void applyFlagsZNC(int res) {
        applyFlags(BITMASK_ZNC, getFlagsZNC(res));
    }

    private void handleBCC() {
        handleBranch(BITMASK_CARRY, false);
    }

    private void handleBCS() {
        handleBranch(BITMASK_CARRY, true);
    }

    private void handleBEQ() {
        handleBranch(BITMASK_ZERO, true);
    }

    private void handleBMI() {
        handleBranch(BITMASK_NEGATIVE, true);
    }

    private void handleBNE() {
        handleBranch(BITMASK_ZERO, false);
    }

    private void handleBPL() {
        handleBranch(BITMASK_NEGATIVE, false);
    }

    private void handleBVC() {
        handleBranch(BITMASK_OVERFLOW, false);
    }

    private void handleBVS() {
        handleBranch(BITMASK_OVERFLOW, true);
    }

    private void handleBranch(byte flagBitmask, boolean flagSet) {
        switch (getCycleInOperation()) {
            case 0 -> {
                fetchOperand0();
                if (isFlagSet(flagBitmask) != flagSet) {
                    nextOp();
                }
            }
            case 1 -> {
                fetchOperand1();
                final short normalNewAddress = getNextPC();
                final short branchNewAddress = (short) (toUint(normalNewAddress) + op0);
                if (samePage(normalNewAddress, branchNewAddress)) {
                    regPC = branchNewAddress;
                    resetCycleInOp();
                }
            }
            case 2 -> {
                final short normalNewAddress = getNextPC();
                final short branchNewAddress = (short) (toUint(normalNewAddress) + op0);
                memoryMap.get(getAddressFromOperands((byte) branchNewAddress, getPage(normalNewAddress)));
                regPC = branchNewAddress;
                resetCycleInOp();
            }
        }
    }

    private void handleBIT() {
        final byte operand = memoryMap.get(operandAddress);
        final byte flags = (byte) (getFlagsZ((byte) (toUint(operand) & toUint(regA)))
                | (toUint(operand) & BITMASK_NV));
        applyFlags(BITMASK_ZNV, flags);
        nextOp();
    }

    private void handleBRK() {
        switch (getCycleInOperation()) {
            case 0 -> fetchOperand0();
            case 1 -> pushPCH();
            case 2 -> pushPCL();
            case 3 -> push((byte) (toUint(regP) | BITMASK_BREAK));
            case 4 -> regPC = (short) toUint(memoryMap.get(IRQ_LO_ADDR));
            case 5 -> {
                regPC = getAddressFromOperands((byte) regPC, memoryMap.get(IRQ_HI_ADDR));
                applyFlags(BITMASK_INT_DISABLE, BITMASK_INT_DISABLE);
                resetCycleInOp();
            }
        }
    }

    private void handleCLC() {
        handleClear(BITMASK_CARRY);
    }

    private void handleCLD() {
        handleClear(BITMASK_DECIMAL);
    }

    private void handleCLI() {
        handleClear(BITMASK_INT_DISABLE);
    }

    private void handleCLV() {
        handleClear(BITMASK_OVERFLOW);
    }

    private void handleClear(byte bitmask) {
        fetchOperand0();
        applyFlags(bitmask, (byte) 0);
        nextOp();
    }

    private void handleCMP() {
        handleCompare(regA);
    }

    private void handleCPX() {
        handleCompare(regX);
    }

    private void handleCPY() {
        handleCompare(regY);
    }

    private void handleCompare(byte reg) {
        final int subtr = Byte.compareUnsigned(reg, memoryMap.get(operandAddress));
        final byte flags =  (byte) (getFlagsZN((byte) subtr)
                | (subtr >= 0 ? BITMASK_CARRY : 0));
        applyFlags(BITMASK_ZNC, flags);
        nextOp();
    }

    private void handleDEC() {
        handleLiteralMemoryAddition((byte) -1);
    }

    private void handleDEX() {
        regX = handleDecrement(regX);
    }

    private void handleDEY() {
        regY = handleDecrement(regY);
    }

    private byte handleDecrement(byte register) {
        return handleLiteralAddition(register, (byte) -1);
    }

    private void handleEOR() {
        byte operand = memoryMap.get(operandAddress);
        regA = (byte) (toUint(regA) ^ toUint(operand));
        applyFlagsZN(regA);
        nextOp();
    }

    private void handleINC() {
        handleLiteralMemoryAddition((byte) 1);
    }

    private void handleINX() {
        regX = handleIncrement(regX);
    }

    private void handleINY() {
        regY = handleIncrement(regY);
    }

    private byte handleIncrement(byte register) {
        return handleLiteralAddition(register, (byte) 1);
    }

    private byte handleLiteralAddition(byte register, byte value) {
        fetchOperand0();
        byte newRegisterValue = (byte) (toUint(register) + value);
        applyFlagsZN(newRegisterValue);
        nextOp();
        return newRegisterValue;
    }

    private void handleLiteralMemoryAddition(byte value) {
        switch (getCycleInOperation()) {
            case 0 -> op0 = memoryMap.get(operandAddress);
            case 1 -> memoryMap.set(operandAddress, op0);
            case 2 -> {
                final byte dec = (byte) (toUint(op0) + toUint(value));
                memoryMap.set(operandAddress, dec);
                applyFlagsZN(dec);
                nextOp();
            }
        }
    }

    private void handleJMP() {
        throw new IllegalStateException("JMP should have been handled during addressing");
    }

    private void handleJSR() {
        switch (getCycleInOperation()) {
            case 0 -> pushPCH();
            case 1 -> pushPCL();
            case 2 -> {
                fetchOperand1();
                regPC = getAddressFromOperands();
                resetCycleInOp();
            }
        }
    }

    private void pushPCH() {
        push(getPage((short) (toUint(regPC) + 2)));
    }

    private void pushPCL() {
        push((byte) ((toUint(regPC) + 2)));
    }

    private void push(byte value) {
        memoryMap.set(getStackAddress(), value);
        regSP = (byte) (toUint(regSP) - 1);
    }

    private short getStackAddress() {
        return (short) (toUint(regSP) | 0x100);
    }

    private void nextOp() {
        incrementPC();
        resetCycleInOp();
    }

    private boolean isCarrySet() {
        return isFlagSet(BITMASK_CARRY);
    }

    private boolean isFlagSet(byte flagBitmask) {
        return (regP & flagBitmask) == flagBitmask;
    }

    private byte getFlagsZNC(int res) {
        return (byte) (getFlagsZN((byte) res)
                | (res > 0xff ? BITMASK_CARRY : 0));
    }

    private void applyFlagsZN(byte res) {
        applyFlags(BITMASK_ZN, getFlagsZN(res));
    }

    private byte getFlagsZN(byte res) {
        return (byte) (getFlagsZ(res)
                | ((res >>> 7) << BIT_NEGATIVE));
    }

    private byte getFlagsZ(byte res) {
        return res == 0 ? BITMASK_ZERO : 0;
    }

    private short subtractPage(short address) {
        return (short) (toUint(address) - 0x100);
    }

    private boolean addressInPage(short address) {
        return getPage(address) == op1;
    }

    private boolean samePage(short address1, short address2) {
        return getPage(address1) == getPage(address2);
    }

    private byte getPage(short address) {
        return ((byte) (toUint(address) >>> 8));
    }

    private short getAddressFromOperandsAndOffsetWithCarry(byte offset) {
        final int op0WithOffset = toUint(op0) + toUint(offset);
        return (short) (op0WithOffset + (toUint(op1) << 8));
    }

    private short getZeroPageAddress(byte offset) {
        return getAddressFromOperandsAndOffsetWithoutCarry(offset, op0, (byte) 0);
    }

    private short getAddressFromAddressAndOffsetWithoutCarry(byte offset, short address) {
        return getAddressFromOperandsAndOffsetWithoutCarry(offset, (byte) address, getPage(address));
    }

    private short getAddressFromOperands() {
        return getAddressFromOperands(op0, op1);
    }

    private short getAddressFromOperandsAndOffsetWithoutCarry(byte offset, byte operand0, byte operand1) {
        final int op0WithOffset = toUint(operand0) + toUint(offset);
        return getAddressFromOperands((byte) op0WithOffset, operand1);
    }

    private short getAddressFromOperands(byte operand0, byte operand1) {
        return (short) (toUint(operand0) | (toUint(operand1) << 8));
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

    private void applyFlags(byte bitmask, byte flags) {
        regP = (byte) ((regP & ~bitmask) | flags);
    }

    private void resetCycleInOp() {
        cycleInInstruction = -1;
    }

    private void incrementPC() {
        regPC = getNextPC();
    }

    private short getNextPC() {
        return (short) (toUint(regPC) + currentOp.addressMode.instructionSize);
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
