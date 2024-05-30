package org.example.nes.cpu;

import static org.example.nes.UInt.toUint;

class DMAController {
    private static final short OAM_DATA = 0x2004;

    private CPUBus bus;

    private boolean canPut;
    private boolean wantsPut;
    private boolean oamDma;
    private boolean halted;

    private short oamAddr;
    private byte oamData;

    void requestOamDma(byte page) {
        oamAddr = (short) (toUint(page) << 8);
        oamDma = true;
    }

    boolean tick() {
        final boolean result = handleTick();
        canPut = !canPut;
        return result;
    }

    private boolean handleTick() {
        if(halted) {
            return handleOam();
        }
        return false;
    }

    private boolean handleOam() {
        if (wantsPut && canPut) {
            bus.write(OAM_DATA, oamData);
            if ((oamAddr & 0xFF) == 0) {
                oamDma = false;
                halted = false;
            }
            wantsPut = false;
            return true;
        } else if (!wantsPut && !canPut) {
            oamData = bus.dmaRead(oamAddr);
            oamAddr++;
            wantsPut = true;
            return true;
        }
        return false;
    }

    boolean haltCPU() {
        if (oamDma) {
            halted = true;
            return true;
        }
        return false;
    }

    public void setBus(CPUBus bus) {
        this.bus = bus;
    }
}
