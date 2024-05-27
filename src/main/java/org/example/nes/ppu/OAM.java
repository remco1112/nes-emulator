package org.example.nes.ppu;

import static org.example.nes.UInt.toUint;

public class OAM {
    private final byte[] oam = new byte[256];
    private byte regOamAddr;
    private boolean pullReadsHigh;

    public byte readRegOamData() {
        if (pullReadsHigh) {
            return (byte) 0xFF;
        }
        return oam[toUint(regOamAddr)];
    }

    public void writeRegOamData(byte data) {
        oam[toUint(regOamAddr)] = data;
        regOamAddr++;
    }

    public byte readRegOamAddr() {
        return regOamAddr;
    }

    public void writeRegOamAddr(byte regOamAddr) {
        this.regOamAddr = regOamAddr;
    }

    void setPullReadsHigh(boolean pullReadsHigh) {
        this.pullReadsHigh = pullReadsHigh;
    }

    public byte[] createSnapshot() {
        final byte[] snapshot = new byte[256];
        System.arraycopy(oam, 0, snapshot, 0, 256);
        return snapshot;
    }
}
