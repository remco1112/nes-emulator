package org.example.nes.cpu;

class NoopInterruptController implements InterruptController {
    @Override
    public boolean isReset() {
        return false;
    }

    @Override
    public boolean isIrq() {
        return false;
    }

    @Override
    public boolean isNmi() {
        return false;
    }
}
