package org.example.nes.ppu;

public class PPU2C02 {
    private byte regPpuCtrl;
    private byte regPpuMask;
    private byte regPpuStatus;
    private byte regPpuScroll;
    private byte regPpuAddr;
    private byte regPpuData;

    private byte regOamAddr;
    private byte regOamData;
    private byte regOamDma;

    public void setRegPpuCtrl(byte regPpuCtrl) {
        this.regPpuCtrl = regPpuCtrl;
    }

    public void setRegPpuMask(byte regPpuMask) {
        this.regPpuMask = regPpuMask;
    }

    public byte getRegPpuStatus() {
        return regPpuStatus;
    }

    public void setRegPpuScroll(byte regPpuScroll) {
        this.regPpuScroll = regPpuScroll;
    }

    public byte getRegPpuAddr() {
        return regPpuAddr;
    }

    public void setRegPpuAddr(byte regPpuAddr) {
        this.regPpuAddr = regPpuAddr;
    }

    public byte getRegPpuData() {
        return regPpuData;
    }

    public void setRegPpuData(byte regPpuData) {
        this.regPpuData = regPpuData;
    }

    public byte getRegOamAddr() {
        return regOamAddr;
    }

    public void setRegOamAddr(byte regOamAddr) {
        this.regOamAddr = regOamAddr;
    }

    public byte getRegOamData() {
        return regOamData;
    }

    public void setRegOamData(byte regOamData) {
        this.regOamData = regOamData;
    }

    public void setRegOamDma(byte regOamDma) {
        this.regOamDma = regOamDma;
    }
}
