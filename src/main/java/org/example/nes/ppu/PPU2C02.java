package org.example.nes.ppu;

import static org.example.nes.UInt.toUint;

public class PPU2C02 {
    private static final int LINES = 262;
    private static final int PX_PER_LINE = 341;
    private static final int CYCLES_PER_FRAME = LINES * PX_PER_LINE;

    private static final byte VBLANK_BITMASK = (byte) 0b1000_0000;

    private final PPU2C02Bus bus;

    private byte regPpuCtrl;
    private byte regPpuMask;
    private byte regPpuStatus;
    private byte regPpuScroll;
    private byte regPpuAddr;
    private byte regPpuData;

    private byte regOamAddr;
    private byte regOamData;
    private byte regOamDma;

    private boolean w;
    private short t;
    private short v;
    private byte x;

    private int cycleInFrame;
    private boolean odd;

    PPU2C02(PPU2C02Bus bus) {
        this.bus = bus;
    }

    public void tick() {


        cycleInFrame = (cycleInFrame + 1) % (CYCLES_PER_FRAME);
    }

    private int getCurrentLine() {
        return cycleInFrame / LINES;
    }

    private int getCycleInline() {
        return cycleInFrame % PX_PER_LINE;
    }

    private boolean writePixel() {
        final int currentLine = getCurrentLine();
        return currentLine >= 0 && currentLine < 240;
    }

    // TODO: If the PPU is currently in vertical blank, and the PPUSTATUS ($2002) vblank flag is still set (1), changing the NMI flag in bit 7 of $2000 from 0 to 1 will immediately generate an NMI.
    public void writeRegPpuCtrl(byte ctrl) {
        /*
            t: ...GH.. ........ <- d: ......GH
         */
        final short gh = (short) (toUint(((byte) (ctrl << 6))) << 4);// 0000GH00 00000000
        t |= gh;
        t &= (short) (gh | 0b11110011_11111111);
    }

    public void setRegPpuMask(byte regPpuMask) {
        this.regPpuMask = regPpuMask;
    }

    // TODO: Race Condition Warning: Reading PPUSTATUS within two cycles of the start of vertical blank will return 0 in bit 7 but clear the latch anyway, causing NMI to not occur that frame.
    public byte readRegPpuStatus() {
        final byte currentStatus = regPpuStatus;
        clearWrite();
        clearVBlankStatus();
        return currentStatus;
    }

    private void clearVBlankStatus() {
        regPpuStatus &= ~VBLANK_BITMASK;
    }

    private void clearWrite() {
        w = false;
    }

    public void writeRegPpuScroll(byte scroll) {
        final int scrollInt = toUint(scroll);
        final short addr = (short) (scrollInt >>> 3);                // 00000000 000ABCDE
        if (w) {
            /*
                t: FGH..AB CDE..... <- d: ABCDEFGH
                w:                  <- 0
             */
            final short abcde = (short) (addr << 5);                 // 000000AB CDE00000
            final short fgh = (short) ((scrollInt & 0b111) << 12);   // 0FGH0000 00000000
            final short abcdefgh = (short) (abcde | fgh);            // 0FGH00AB CDE00000
            t |= abcdefgh;
            t &= (short) (abcdefgh | 0b10001100_00011111);           // 1FGH11AB CDE11111
        } else {
            /*
                t: ....... ...ABCDE <- d: ABCDE...
                x:              FGH <- d: .....FGH
                w:                  <- 1
             */
            t |= addr;
            t &= (short) (addr | 0b11111111_11100000);               // 11111111 111ABCDE

            final byte xScroll = (byte) (scroll & 0b00000111);       // 00000FGH
            this.x |= xScroll;
            this.x &= (byte) (xScroll | 0b11111000);                 // 11111FGH
        }
        w = !w;
    }

    public byte getRegPpuAddr() {
        return regPpuAddr;
    }

    public void writeRegPpuAddr(byte addr) {
        final int addrInt = toUint(addr);
        if (w) {
            /*
                t: ....... ABCDEFGH <- d: ABCDEFGH
                v: <...all bits...> <- t: <...all bits...>
                w:                  <- 0
             */
            final short addrLow = (short) addrInt;
            t |= addrLow;
            t &= (short) (addrLow | 0xff00);
            v = t;
        } else {
            /*
                t: .CDEFGH ........ <- d: ..CDEFGH
                       <unused>     <- d: AB......
                t: Z...... ........ <- 0 (bit Z is cleared)
                w:                  <- 1
            */
            final short addrHigh = (short) (toUint(((short) (addrInt << 10))) >>> 2);  // 00CDEFGH 00000000
            t |= addrHigh;
            t &= (short) (addrHigh | 0b10000000_11111111);           // 10CDEFGH 11111111
        }
        w = !w;
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

    short getV() {
        return v;
    }

    short getT() {
        return t;
    }

    byte getX() {
        return x;
    }

    boolean getW() {
        return w;
    }

    void setW(boolean w) {
        this.w = w;
    }

    void setT(short t) {
        this.t = t;
    }

    void setV(short v) {
        this.v = v;
    }

    void setX(byte x) {
        this.x = x;
    }

    void setRegPpuStatus(byte regPpuStatus) {
        this.regPpuStatus = regPpuStatus;
    }

    byte getRegPpuStatus() {
        return regPpuStatus;
    }
}
