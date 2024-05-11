package org.example.nes.bridge;

import org.example.nes.cpu.InterruptController;
import org.example.nes.ppu.VBlankNotificationReceiver;

class NESInterruptController implements InterruptController, VBlankNotificationReceiver {
    private boolean nmi;

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
        final boolean currentNmi = nmi;
        nmi = false;
        return currentNmi;
    }

    @Override
    public void onPpuVBlank() {
        nmi = true;
    }
}
