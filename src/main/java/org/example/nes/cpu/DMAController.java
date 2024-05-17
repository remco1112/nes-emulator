package org.example.nes.cpu;

import org.example.nes.Bus;

import static org.example.nes.UInt.toUint;

class DMAController {
    private static final short OAM_DATA = 0x2004;

    private Bus bus;

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

    void tick() {
        if (halted) {
            handleOam();
        }
        canPut = !canPut;
    }

    private void handleOam() {
        if (wantsPut && canPut) {
            bus.write(OAM_DATA, oamData);
            if ((oamAddr & 0xFF) == 0xFF) {
                oamDma = false;
                halted = false;
            }
            wantsPut = false;
        } else if (!wantsPut && !canPut) {
            oamData = bus.dmaRead(oamAddr);
            if ((oamAddr & 0xFF) != 0xFF) {
                oamAddr++;
                wantsPut = true;
            }
        }
    }

    boolean haltCPU() {
        if (oamDma) {
            halted = true;
            return true;
        }
        return false;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }
}
