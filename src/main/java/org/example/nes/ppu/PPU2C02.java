package org.example.nes.ppu;

import static org.example.nes.UInt.toUint;

public class PPU2C02 {
    private static final int LINES = 262;
    private static final int PX_PER_LINE = 341;
    private static final int CYCLES_PER_FRAME = LINES * PX_PER_LINE;

    private final PPU2C02Bus bus;

    private byte regPpuMask;

    private byte regOamAddr;
    private byte regOamData;
    private byte regOamDma;

    private boolean w;
    private short t;
    private short v;
    private byte x;

    private int cycleInFrame;
    private byte ppuDataReadBuf;

    private boolean odd;
    private boolean vBlank;
    private boolean vRamPpuDataIncDown;
    private boolean vBlankNMI;
    private boolean greyScale;
    private boolean showBgLeft;
    private boolean showSpLeft;
    private boolean showBg;
    private boolean showSp;

    private byte emphasis; // BGR
    private short patternTableAddress;

    private byte nt;
    private byte at;
    private byte patternLo;
    private byte patternHi;

    private short patternLoShifter;
    private short patternHiShifter;
    private byte attributeLowShifter;
    private byte attributeHighShifter;

    PPU2C02(PPU2C02Bus bus) {
        this.bus = bus;
    }

    public void tick() {
        final int line = getCurrentLine();
        final int cycle = getCycleInline();
        final int vInt = toUint(v);
        if (line < 240 || line == 261) {
            if (cycle > 0 && cycle <= 256) {
                switch ((cycle - 1) % 8) {
                    case 0 -> {
                        patternLoShifter |= (short) (patternLo << 8);
                        patternHiShifter |= (short) (patternHi << 8);
                        // TODO Reset attribute shift registers here: https://forums.nesdev.org/viewtopic.php?t=10348

                        nt = bus.read((short) (0x2000 | (vInt & 0x0FFF)));
                    }
                    case 2 -> at = bus.read((short) (0x23C0 | (vInt & 0x0C00) | ((vInt >>> 4) & 0x38) | ((vInt >> 2) & 0x07)));
                    case 4 -> patternLo = bus.read((short) (toUint(patternTableAddress) + toUint(nt)));
                    case 6 -> patternHi = bus.read((short) (toUint(patternTableAddress) + toUint(nt) + 8));
                }
            }
        }

        cycleInFrame = (cycleInFrame + 1) % (CYCLES_PER_FRAME);
        if (cycleInFrame == 0) {
            odd = !odd;
        }
    }

    private void incrementVHorizontal() {
        final int intV = toUint(v);
        if ((intV & 0x001F) == 31) {                                 // if coarse X == 31
            v = (short) ((intV & ~0x001F) ^ 0x0400);                 // switch horizontal nametable
        } else {
            v = (short) (intV + 1);                                  // increment coarse X
        }
    }

    private int getCurrentLine() {
        return cycleInFrame / LINES;
    }

    private int getCycleInline() {
        return cycleInFrame % PX_PER_LINE;
    }

    private boolean shouldWritePixel() {
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

        vRamPpuDataIncDown = (ctrl & 0x4) == 0x4;
        patternTableAddress = (short) ((ctrl & 0x10) << 8);

                vBlankNMI = (ctrl & 0x80) == 0x80;
        // TODO remaining flags
    }

    public void setRegPpuMask(byte regPpuMask) {
        this.regPpuMask = regPpuMask;
    }

    // TODO: Race Condition Warning: Reading PPUSTATUS within two cycles of the start of vertical blank will return 0 in bit 7 but clear the latch anyway, causing NMI to not occur that frame.
    // TODO: Sprite 0 and overflow
    public byte readRegPpuStatus() {
        final byte currentStatus = (byte) (vBlank ? 0x80 : 0);
        clearWrite();
        clearVBlank();
        return currentStatus;
    }

    private void clearVBlank() {
        vBlank = false;
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

    public byte readRegPpuData() {
        if (PPU2C02Bus.isPaletteRamAddress(v)) {
            ppuDataReadBuf = bus.read(v); // TODO: storing palette ram value in buffer is not correct but good enough for now
            return ppuDataReadBuf;
        }
        final byte returnValue = ppuDataReadBuf;
        ppuDataReadBuf = bus.read(v);
        incrementVAfterPpuDataAccess();
        return returnValue;
    }

    public void writeRegPpuData(byte value) {
        bus.write(v, value);
        incrementVAfterPpuDataAccess();
    }

    private void incrementVAfterPpuDataAccess() {
        incrementV(vRamPpuDataIncDown ? 32 : 1);
    }

    private void incrementV(int i) {
        v = (short) ((toUint(v) + i) % 0x8000);
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

    void setX(byte x) {
        this.x = x;
    }
}
