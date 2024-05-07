package org.example.nes;

public class KlausInterruptController implements InterruptController {
    private static final short INTERRUPT_STATUS_ADDR = (short) 0xbffc;

    private static final byte IRQ_BITMASK = 0b1;
    private static final byte NMI_BITMASK = 0b10;

    private final Bus bus;

    KlausInterruptController(Bus bus) {
        this.bus = bus;
    }

    private boolean lastNmi = false;


    @Override
    public boolean isReset() {
        return false;
    }

    @Override
    public boolean isIrq() {
        return (~bus.read(INTERRUPT_STATUS_ADDR) & IRQ_BITMASK) == IRQ_BITMASK;
    }

    @Override
    public boolean isNmi() {
        if (lastNmi) {
            lastNmi = (~bus.read(INTERRUPT_STATUS_ADDR) & NMI_BITMASK) == NMI_BITMASK;
            return false;
        }
        return lastNmi = (~bus.read(INTERRUPT_STATUS_ADDR) & NMI_BITMASK) == NMI_BITMASK;
    }
}
