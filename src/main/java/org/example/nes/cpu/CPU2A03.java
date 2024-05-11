package org.example.nes.cpu;

import org.example.nes.Bus;
import org.example.nes.mapper.Mapper;
import org.example.nes.ppu.PPU2C02;

import static org.example.nes.UInt.toUint;

public class CPU2A03 {
    private static final short IRQ_ADDR = (short) 0xfffe;
    private static final short RST_ADDR = (short) 0xfffc;
    private static final short NMI_ADDR = (short) 0xfffa;

    private static final byte STACK_BASE = (byte) 0xff;

    private static final byte BIT_NEGATIVE    = 7;
    private static final byte BIT_OVERFLOW    = 6;
    private static final byte BIT_UNUSED      = 5;
    private static final byte BIT_BREAK       = 4;
    private static final byte BIT_DECIMAL     = 3;
    private static final byte BIT_INT_DISABLE = 2;
    private static final byte BIT_ZERO        = 1;
    private static final byte BIT_CARRY       = 0;

    private static final byte BITMASK_NEGATIVE    = (byte) (1 << BIT_NEGATIVE);
    private static final byte BITMASK_OVERFLOW    = (byte) (1 << BIT_OVERFLOW);
    private static final byte BITMASK_UNUSED      = (byte) (1 << BIT_UNUSED);
    private static final byte BITMASK_BREAK       = (byte) (1 << BIT_BREAK);
    private static final byte BITMASK_DECIMAL     = (byte) (1 << BIT_DECIMAL);
    private static final byte BITMASK_INT_DISABLE = (byte) (1 << BIT_INT_DISABLE);
    private static final byte BITMASK_ZERO        = (byte) (1 << BIT_ZERO);
    private static final byte BITMASK_CARRY       = (byte) (1 << BIT_CARRY);

    private static final byte BITMASK_ZN = BITMASK_ZERO | BITMASK_NEGATIVE;
    private static final byte BITMASK_ZNC = BITMASK_ZN | BITMASK_CARRY;
    private static final byte BITMASK_ZNCV = BITMASK_ZNC | BITMASK_OVERFLOW;
    private static final byte BITMASK_NV = BITMASK_NEGATIVE | BITMASK_OVERFLOW;
    private static final byte BITMASK_ZNV = BITMASK_ZERO | BITMASK_NV;
    private static final byte BITMASK_BU = BITMASK_BREAK | BITMASK_UNUSED;

    private short regPC;
    private byte regSP;
    private byte regA;
    private byte regX;
    private byte regY;
    private byte regP;

    private final Bus bus;
    private final InterruptController interruptionController;

    private boolean interrupt_irq;
    private boolean interrupt_nmi;
    private boolean interrupt_reset;

    private Interrupt currentInterrupt;

    private OpCode currentOp = OpCode.CPY_IMM;
    private int cycleInInstruction = 0;

    private short operandAddress;

    private byte op0;
    private byte op1;

    CPU2A03(Bus bus) {
        this(
                bus,
                (short) 0,
                STACK_BASE,
                (byte) 0,
                (byte) 0,
                (byte) 0,
                (byte) 0,
                new NoopInterruptController()
        );
    }

    CPU2A03(Bus bus, short regPC) {
        this(
                bus,
                regPC,
                STACK_BASE,
                (byte) 0,
                (byte) 0,
                (byte) 0,
                (byte) 0,
                new NoopInterruptController()
        );
    }

    CPU2A03(Bus bus, short regPC, InterruptController interruptController) {
        this(
                bus,
                regPC,
                STACK_BASE,
                (byte) 0,
                (byte) 0,
                (byte) 0,
                (byte) 0,
                interruptController
        );
    }

    public CPU2A03(Mapper mapper, PPU2C02 ppu, InterruptController interruptController) {
        this(
                new CPU2A03Bus(mapper, ppu),
                (short) 0,
                STACK_BASE,
                (byte) 0,
                (byte) 0,
                (byte) 0,
                (byte) 0,
                interruptController);
        interrupt_reset = true;
    }

    CPU2A03(Bus bus, short regPC, byte regSP, byte regA, byte regX, byte regY, byte regP, InterruptController interruptController) {
        this.bus = bus;
        this.regPC = regPC;
        this.regSP = regSP;
        this.regA = regA;
        this.regX = regX;
        this.regY = regY;
        this.regP = regP;
        this.interruptionController = interruptController;
    }

    public void tick() {
        if (cycleInInstruction == 0) {
            if ((currentInterrupt = getInterrupt()) != null) {
                handleInterrupt();
            } else {
                fetchOperation();
            }
        } else if (currentInterrupt != null) {
            handleInterrupt();
        } else if (cycleInInstruction - 1 < currentOp.addressMode.cycles) {
            handleAddressing();
        } else {
            handleOperation();
        }
        cycleInInstruction++;
    }

    private void handleInterrupt() {
        switch (currentInterrupt) {
            case RESET -> handleReset();
            case NMI -> handleNMI();
            case IRQ -> handleIRQ();
        }
    }

    private void handleReset() {
//        if (interrupt_reset) {
//            checkInterrupts();
//            resetCycleInOp();
//            return;
//        }
        handleInterrupt(false, RST_ADDR);
    }

    private void handleNMI() {
        handleInterrupt(false, NMI_ADDR);
    }

    private void handleIRQ() {
        handleInterrupt(false, IRQ_ADDR);
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
            case ZEROPAGE_Y_INDEXED -> handleAddressingZeroPageYIndexed();
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
                    bus.read(getStackAddress());
                    cycleInInstruction++;
                    return;
                }
                fetchOperand1();
                operandAddress = getAddressFromOperandsAndOffsetWithCarry(offset);
                if (currentOp.operation == Operation.JMP) { // Hack: Handling jump during addressing since JMP has no operation cycles
                    regPC = operandAddress;
                    checkInterrupts();
                    resetCycleInOp();
                    return;
                }
                if (shouldSkipCycleAbsolute()) {
                    cycleInInstruction++;
                }
            }
            case 2 -> bus.read(addressInPage(operandAddress) ? operandAddress : subtractPage(operandAddress));
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
        handleAddressingZeroPageIndexed(regX);
    }

    private void handleAddressingZeroPageYIndexed() {
        handleAddressingZeroPageIndexed(regY);
    }

    private void handleAddressingZeroPageIndexed(byte register) {
        if (getCycleInAddressing() == 0) {
            fetchOperand0();
        } else {
            bus.read((short) toUint(op0));
            operandAddress = getZeroPageAddress(register);
        }
    }

    private void handleAddressingXIndexedIndirect() {
        switch (getCycleInAddressing()) {
            case 0 -> fetchOperand0();
            case 1 -> bus.read((short) toUint(op0));
            case 2 -> operandAddress = (short) toUint(bus.read(getZeroPageAddress(regX)));
            case 3 -> operandAddress = getAddressFromOperands((byte) operandAddress, bus.read((short) (toUint(getZeroPageAddress((byte) (toUint(regX) + 1))))));
        }
    }

    private void handleAddressingIndirectYIndexed() {
        switch (getCycleInAddressing()) {
            case 0 -> fetchOperand0();
            case 1 -> operandAddress = (short) toUint(bus.read(getZeroPageAddress((byte) 0)));
            case 2 -> {
                operandAddress = getAddressFromOperands((byte) operandAddress, bus.read((short) (toUint(getZeroPageAddress((byte) 1)))));
                if (samePage(operandAddress, (short) (toUint(operandAddress) + toUint(regY))) && !currentOp.operation.writesToMemory) {
                    cycleInInstruction++;
                }
                operandAddress = (short) (toUint(operandAddress) + toUint(regY));
            }
            case 3 -> bus.read(samePage(operandAddress, (short) (toUint(operandAddress) - toUint(regY))) ? operandAddress : subtractPage(operandAddress));
        }
    }

    private void handleAddressingIndirect() { // Hack: immediately handles JMP operation since JMP has no op cycles
        switch (getCycleInAddressing()) {
            case 0 -> fetchOperand0();
            case 1 -> {
                fetchOperand1();
                operandAddress = getAddressFromOperands();
            }
            case 2 -> op0 = bus.read(operandAddress);
            case 3 -> {
                op1 = bus.read(getAddressFromAddressAndOffsetWithoutCarry((byte) 1, operandAddress));
                regPC = getAddressFromOperands();
                checkInterrupts();
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
            case LDA -> handleLDA();
            case LDX -> handleLDX();
            case LDY -> handleLDY();
            case LSR -> handleLSR();
            case NOP -> handleNOP();
            case ORA -> handleORA();
            case PHA -> handlePHA();
            case PHP -> handlePHP();
            case PLA -> handlePLA();
            case PLP -> handlePLP();
            case ROL -> handleROL();
            case ROR -> handleROR();
            case RTI -> handleRTI();
            case RTS -> handleRTS();
            case SBC -> handleSBC();
            case SEC -> handleSEC();
            case SED -> handleSED();
            case SEI -> handleSEI();
            case STA -> handleSTA();
            case STX -> handleSTX();
            case STY -> handleSTY();
            case TAX -> handleTAX();
            case TAY -> handleTAY();
            case TSX -> handleTSX();
            case TXA -> handleTXA();
            case TXS -> handleTXS();
            case TYA -> handleTYA();
        }
    }

    private void fetchOperation() {
        currentOp = OpCode.fromOpCode(bus.read(regPC));
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
        handleAddition(bus.read(operandAddress));
    }

    private void handleSBC() {
        handleAddition((byte) ~bus.read(operandAddress));
    }

    private void handleAddition(byte op) {
        final int operand = toUint(op);
        int res = toUint(regA) + operand + (isCarrySet() ? 1 : 0);
        final byte flags = (byte) (getFlagsZNC(res)
                | ((toUint(regA) >>> 7 == operand >>> 7) && (operand >>> 7 != toUint((byte) res) >>> 7) ? BITMASK_OVERFLOW : 0)
        );
        regA = (byte) res;
        applyFlags(BITMASK_ZNCV, flags);
        nextOp();
    }

    private void handleAND() {
        regA = (byte) (toUint(regA) & toUint(bus.read(operandAddress)));
        applyFlagsZN(regA);
        nextOp();
    }

    private void handleASL() {
        switch (getCycleInOperation()) {
            case 0 -> {
                op0 = bus.read(operandAddress);
                if (currentOp.addressMode == AddressMode.ACCUMULATOR) {
                    final int res = toUint(regA) << 1;
                    applyFlagsZNC(res);
                    regA = (byte) res;
                    nextOp();
                }
            }
            case 1 -> bus.write(operandAddress, op0);
            case 2 -> {
                final int res = toUint(op0) << 1;
                bus.write(operandAddress, (byte) res);
                applyFlagsZNC(res);
                nextOp();
            }
        }
    }

    private void handleLSR() {
        switch (getCycleInOperation()) {
            case 0 -> {
                op0 = bus.read(operandAddress);
                if (currentOp.addressMode == AddressMode.ACCUMULATOR) {
                    final byte res = (byte) (toUint(regA) >>> 1);
                    applyFlags(BITMASK_ZNC, (byte) (toUint(getFlagsZN(res)) | ((toUint(regA) & 1) << BIT_CARRY)));
                    regA = res;
                    nextOp();
                }
            }
            case 1 -> bus.write(operandAddress, op0);
            case 2 -> {
                final byte res = (byte) (toUint(op0) >>> 1);
                bus.write(operandAddress, res);
                applyFlags(BITMASK_ZNC, (byte) (toUint(getFlagsZN(res)) | ((toUint(op0) & 1) << BIT_CARRY)));
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
                checkInterrupts();
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
                bus.read(getAddressFromOperands((byte) branchNewAddress, getPage(normalNewAddress)));
                regPC = branchNewAddress;
                resetCycleInOp();
            }
        }
    }

    private void handleBIT() {
        final byte operand = bus.read(operandAddress);
        final byte flags = (byte) (getFlagsZ((byte) (toUint(operand) & toUint(regA)))
                | (toUint(operand) & BITMASK_NV));
        applyFlags(BITMASK_ZNV, flags);
        nextOp();
    }

    private void handleBRK() {
        handleInterrupt(true, IRQ_ADDR);
    }

    private void handleInterrupt(boolean soft, short addr) {
        switch (getCycleInOperation()) {
            case 0 -> fetchOperand0();
            case 1 -> {
                if (addr != RST_ADDR) pushPCH(soft ? 2 : 0);
            }
            case 2 -> {
                if (addr != RST_ADDR) pushPCL(soft ? 2 : 0);
            }
            case 3 -> {
                if (addr != RST_ADDR) push((byte) (toUint(regP) | (soft ? BITMASK_BREAK : 0)));
            }
            case 4 -> regPC = (short) toUint(bus.read(addr));
            case 5 -> {
                regPC = getAddressFromOperands((byte) regPC, bus.read((short) (toUint(addr) + 1)));
                applyFlags(BITMASK_INT_DISABLE, BITMASK_INT_DISABLE);
                checkInterrupts(); // TODO interrupt check probably not cycle-accurate
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
        checkInterrupts();
        applyFlags(bitmask, (byte) 0);
        incrementPC();
        resetCycleInOp();
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
        final int subtr = Byte.compareUnsigned(reg, bus.read(operandAddress));
        final byte flags = (byte) (getFlagsZN((byte) subtr)
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
        byte operand = bus.read(operandAddress);
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
            case 0 -> op0 = bus.read(operandAddress);
            case 1 -> bus.write(operandAddress, op0);
            case 2 -> {
                final byte dec = (byte) (toUint(op0) + toUint(value));
                bus.write(operandAddress, dec);
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
            case 0 -> pushPCH(2);
            case 1 -> pushPCL(2);
            case 2 -> {
                fetchOperand1();
                regPC = getAddressFromOperands();
                checkInterrupts();
                resetCycleInOp();
            }
        }
    }

    private void handleLDA() {
        regA = handleLoad();
    }

    private void handleLDX() {
        regX = handleLoad();
    }

    private void handleLDY() {
        regY = handleLoad();
    }

    private byte handleLoad() {
        byte register = bus.read(operandAddress);
        applyFlagsZN(register);
        nextOp();
        return register;
    }

    private void handleNOP() {
        fetchOperand0();
        nextOp();
    }

    private void handleORA() {
        regA = (byte) (toUint(regA) | toUint(bus.read(operandAddress)));
        applyFlagsZN(regA);
        nextOp();
    }

    private void handlePHA() {
        handlePush(regA);
    }

    private void handlePHP() {
        handlePush((byte) (toUint(regP) | BITMASK_BU));
    }

    private void handlePush(byte value) {
        switch (getCycleInOperation()) {
            case 0 -> fetchOperand0();
            case 1 -> {
                push(value);
                nextOp();
            }
        }
    }

    private void handlePLA() {
        switch (getCycleInOperation()) {
            case 0 -> fetchOperand0();
            case 1 -> bus.read(getStackAddress());
            case 2 -> {
                regA = pull();
                applyFlagsZN(regA);
                nextOp();
            }
        }
    }

    private void handlePLP() {
        switch (getCycleInOperation()) {
            case 0 -> fetchOperand0();
            case 1 -> bus.read(getStackAddress());
            case 2 -> {
                pullP();
                nextOp();
            }
        }
    }

    private void handleROL() {
        switch (getCycleInOperation()) {
            case 0 -> {
                op0 = bus.read(operandAddress);
                if (currentOp.addressMode == AddressMode.ACCUMULATOR) {
                    final int res = (toUint(regA) << 1) | (isCarrySet() ? 1 : 0);
                    applyFlagsZNC(res);
                    regA = (byte) res;
                    nextOp();
                }
            }
            case 1 -> bus.write(operandAddress, op0);
            case 2 -> {
                final int res = (toUint(op0) << 1) | (isCarrySet() ? 1 : 0);
                bus.write(operandAddress, (byte) res);
                applyFlagsZNC(res);
                nextOp();
            }
        }
    }

    private void handleROR() {
        switch (getCycleInOperation()) {
            case 0 -> {
                op0 = bus.read(operandAddress);
                if (currentOp.addressMode == AddressMode.ACCUMULATOR) {
                    final byte res = (byte) ((toUint(regA) >>> 1) | (isCarrySet() ? 0b10000000 : 0));
                    applyFlags(BITMASK_ZNC, (byte) (toUint(getFlagsZN(res)) | ((toUint(regA) & 1) << BIT_CARRY)));
                    regA = res;
                    nextOp();
                }
            }
            case 1 -> bus.write(operandAddress, op0);
            case 2 -> {
                final byte res = (byte) ((toUint(op0) >>> 1) | (isCarrySet() ? 0b10000000 : 0));
                bus.write(operandAddress, res);
                applyFlags(BITMASK_ZNC, (byte) (toUint(getFlagsZN(res)) | ((toUint(op0) & 1) << BIT_CARRY)));
                nextOp();
            }
        }
    }

    private void handleRTI() {
        switch (getCycleInOperation()) {
            case 0 -> fetchOperand0();
            case 1 -> bus.read(getStackAddress());
            case 2 -> pullP();
            case 3 -> regPC = pull();
            case 4 -> {
                regPC = getAddressFromOperands((byte) regPC, pull());
                checkInterrupts();
                resetCycleInOp();
            }
        }
    }

    private void handleRTS() {
        switch (getCycleInOperation()) {
            case 0 -> fetchOperand0();
            case 1 -> bus.read(getStackAddress());
            case 2 -> regPC = pull();
            case 3 -> regPC = getAddressFromOperands((byte) regPC, pull());
            case 4 -> {
                bus.read(regPC);
                nextOp();
            }
        }
    }

    private void handleSEC() {
        handleSet(BITMASK_CARRY);
    }

    private void handleSED() {
        handleSet(BITMASK_DECIMAL);
    }

    private void handleSEI() {
        handleSet(BITMASK_INT_DISABLE);
    }

    private void handleSet(byte bitmask) {
        fetchOperand0();
        checkInterrupts();
        applyFlags(bitmask, bitmask);
        incrementPC();
        resetCycleInOp();
    }

    private void handleSTA() {
        handleStore(regA);
    }

    private void handleSTX() {
        handleStore(regX);
    }

    private void handleSTY() {
        handleStore(regY);
    }

    private void handleStore(byte reg) {
        bus.write(operandAddress, reg);
        nextOp();
    }

    private void handleTAX() {
        fetchOperand0();
        regX = regA;
        applyFlagsZN(regX);
        nextOp();
    }

    private void handleTAY() {
        fetchOperand0();
        regY = regA;
        applyFlagsZN(regY);
        nextOp();
    }

    private void handleTSX() {
        fetchOperand0();
        regX = regSP;
        applyFlagsZN(regX);
        nextOp();
    }

    private void handleTXA() {
        fetchOperand0();
        regA = regX;
        applyFlagsZN(regA);
        nextOp();
    }

    private void handleTXS() {
        fetchOperand0();
        regSP = regX;
        nextOp();
    }

    private void handleTYA() {
        fetchOperand0();
        regA = regY;
        applyFlagsZN(regA);
        nextOp();
    }

    private void pullP() {
        regP = (byte) ((pull() | BITMASK_UNUSED) & ~BITMASK_BREAK);
    }

    private void pushPCH(int offset) {
        push(getPage((short) (toUint(regPC) + offset)));
    }

    private void pushPCL(int offset) {
        push((byte) ((toUint(regPC) + offset)));
    }

    private void push(byte value) {
        bus.write(getStackAddress(), value);
        regSP = (byte) (toUint(regSP) - 1);
    }

    private byte pull() {
        regSP = (byte) (toUint(regSP) + 1);
        return bus.read(getStackAddress());
    }

    private short getStackAddress() {
        return (short) (toUint(regSP) | 0x100);
    }

    private void nextOp() {
        incrementPC();
        resetCycleInOp();
        checkInterrupts();
    }

    private void checkInterrupts() {
        interrupt_reset = interruptionController.isReset();
        interrupt_irq = interruptionController.isIrq() && !isFlagSet(BITMASK_INT_DISABLE);
        interrupt_nmi = interruptionController.isNmi();
    }

    private Interrupt getInterrupt() {
        if (interrupt_reset) {
            return Interrupt.RESET;
        }
        if (interrupt_nmi) {
            return Interrupt.NMI;
        }
        if (interrupt_irq) {
            return Interrupt.IRQ;
        }
        return null;
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
        op0 = bus.read((short) (regPC + 1));
    }

    private void fetchOperand1() {
        op1 = bus.read((short) (regPC + 2));
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

    public Bus getMemoryMap() {
        return bus;
    }
}
