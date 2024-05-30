package org.example.nes.cpu.bus;

import org.example.nes.bus.Memory;

public class IORegisterBridge implements Memory {
    private static final int IO_REGISTER_FILE_SIZE = 0x18;

    private final DMAController dmaController;

    IORegisterBridge(DMAController dmaController) {
        this.dmaController = dmaController;
    }

    @Override
    public int getSize() {
        return IO_REGISTER_FILE_SIZE;
    }

    @Override
    public byte read(short address) {
        return 0; // TODO
    }

    @Override
    public void write(short address, byte value) {
        switch (address) {
            case 0x14 -> dmaController.requestOamDma(value);
            default -> {
                // throw new IllegalStateException("Illegal IO register write: " + writeToString(addr, value));
            }
        }
    }
}
