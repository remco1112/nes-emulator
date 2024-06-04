package org.example.nes.cpu.bus;

import org.example.nes.apu.APURP2A03;
import org.example.nes.bus.Memory;
import org.example.nes.input.InputController;

import static org.example.nes.utils.StringUtils.addressToString;
import static org.example.nes.utils.StringUtils.writeToString;

public class IORegisterBridge implements Memory {
    private static final int IO_REGISTER_FILE_SIZE = 0x18;

    private final DMAController dmaController;
    private final InputController inputController;
    private final APURP2A03 apu;

    IORegisterBridge(DMAController dmaController, InputController inputController, APURP2A03 apu) {
        this.dmaController = dmaController;
        this.inputController = inputController;
        this.apu = apu;
    }

    @Override
    public int getSize() {
        return IO_REGISTER_FILE_SIZE;
    }

    @Override
    public byte read(short address) {
        return switch (address) {
            case 0x15 -> apu.readStatus();
            case 0x16 -> inputController.readDevice1();
            case 0x17 -> inputController.readDevice2();
            default -> {
                System.out.println("Warning: open bus IO register read: " + addressToString(address));
                yield 0;
            }
        };
    }

    @Override
    public void write(short address, byte value) {
        switch (address) {
            case 0x00 -> apu.writeSQ1Vol(value);
            case 0x01 -> apu.writeSQ1Sweep(value);
            case 0x02 -> apu.writeSQ1Low(value);
            case 0x03 -> apu.writeSQ1High(value);
            case 0x04 -> apu.writeSQ2Vol(value);
            case 0x05 -> apu.writeSQ2Sweep(value);
            case 0x06 -> apu.writeSQ2Low(value);
            case 0x07 -> apu.writeSQ2High(value);
            case 0x08 -> apu.writeTRILinear(value);
            case 0x0A -> apu.writeTRILow(value);
            case 0x0B -> apu.writeTRIHigh(value);
            case 0x0C -> apu.writeNoiseVol(value);
            case 0x0E -> apu.writeNoiseLow(value);
            case 0x0F -> apu.writeNoiseHigh(value);
            case 0x10 -> apu.writeDMCFreq(value);
            case 0x11 -> apu.writeDMCRaw(value);
            case 0x12 -> apu.writeDMCStart(value);
            case 0x13 -> apu.writeDMCLen(value);
            case 0x14 -> dmaController.requestOamDma(value);
            case 0x15 -> apu.writeStatus(value);
            case 0x16 -> inputController.writeOutRegister(value);
            case 0x17 -> apu.writeFrameCounter(value);
            default -> throw new IllegalArgumentException("Unsupported IO register write: " + writeToString(address, value));
        }
    }
}
