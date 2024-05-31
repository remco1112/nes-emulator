package org.example.nes.cpu.bus;

import org.example.nes.bus.Memory;
import org.example.nes.input.InputController;

import static org.example.nes.utils.StringUtils.addressToString;
import static org.example.nes.utils.StringUtils.writeToString;

public class IORegisterBridge implements Memory {
    private static final int IO_REGISTER_FILE_SIZE = 0x18;

    private final DMAController dmaController;
    private final InputController inputController;

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
            case 0x16 -> inputController.readDevice1();
            case 0x17 -> inputController.readDevice2();
            default -> {
                System.out.println("Warning: unsupported IO register read: " + addressToString(address));
                yield 0;
            }
        };
    }

    @Override
    public void write(short address, byte value) {
        switch (address) {
            case 0x14 -> dmaController.requestOamDma(value);
            case 0x16 -> inputController.writeOutRegister(value);
            default -> System.out.println("Warning: unsupported IO register write: " + writeToString(address, value));
        }
    }
}
