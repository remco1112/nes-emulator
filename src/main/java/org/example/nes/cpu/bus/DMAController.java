package org.example.nes.cpu.bus;

import org.example.nes.bus.Bus;
import org.example.nes.bus.ReadListener;

import static org.example.nes.utils.UInt.toUint;

public class DMAController implements ReadListener {
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

    public boolean tick() {
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
            oamData = bus.read(oamAddr);
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

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    @Override
    public void onRead(short address, byte value) {
        if (!halted && haltCPU()) {
            throw new DMAHaltException();
        }
    }
}
