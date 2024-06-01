package org.example.nes.cpu.bus;

import org.example.nes.apu.APUController;
import org.example.nes.bus.Memory;
import org.example.nes.input.InputController;

import static org.example.nes.utils.StringUtils.addressToString;
import static org.example.nes.utils.StringUtils.writeToString;

public class IORegisterBridge implements Memory {
    private static final int IO_REGISTER_FILE_SIZE = 0x18;

    private final DMAController dmaController;
    private final InputController inputController;
    private final APUController apuController = new APUController();

    IORegisterBridge(DMAController dmaController, InputController inputController) {
        this.dmaController = dmaController;
        this.inputController = inputController;
    }

    @Override
    public int getSize() {
        return IO_REGISTER_FILE_SIZE;
    }

    @Override
    public byte read(short address) {
        return switch (address) {
            case 0x15 -> apuController.readStatus();
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
            case 0x00 -> apuController.writeSQ1Vol(value);
            case 0x01 -> apuController.writeSQ1Sweep(value);
            case 0x02 -> apuController.writeSQ1Low(value);
            case 0x03 -> apuController.writeSQ1High(value);
            case 0x04 -> apuController.writeSQ2Vol(value);
            case 0x05 -> apuController.writeSQ2Sweep(value);
            case 0x06 -> apuController.writeSQ2Low(value);
            case 0x07 -> apuController.writeSQ2High(value);
            case 0x08 -> apuController.writeTRILinear(value);
            case 0x0A -> apuController.writeTRILow(value);
            case 0x0B -> apuController.writeTRIHigh(value);
            case 0x0C -> apuController.writeNoiseVol(value);
            case 0x0E -> apuController.writeNoiseLow(value);
            case 0x0F -> apuController.writeNoiseHigh(value);
            case 0x10 -> apuController.writeDMCFreq(value);
            case 0x11 -> apuController.writeDMCRaw(value);
            case 0x12 -> apuController.writeDMCStart(value);
            case 0x13 -> apuController.writeDMCLen(value);
            case 0x14 -> dmaController.requestOamDma(value);
            case 0x15 -> apuController.writeStatus(value);
            case 0x16 -> inputController.writeOutRegister(value);
            case 0x17 -> apuController.writeFrameCounter(value);
            default -> throw new IllegalArgumentException("Unsupported IO register write: " + writeToString(address, value));
        }
    }
}
