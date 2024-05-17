package org.example.nes.ppu;

import org.example.nes.Bus;
import org.example.nes.mapper.Mapper;

import static org.example.nes.UInt.toUint;

public class PPU2C02 {
    private static final int LINES = 262;
    private static final int PX_PER_LINE = 341;
    private static final int CYCLES_PER_FRAME = LINES * PX_PER_LINE;

    private final byte[] oam = new byte[256];

    private final Bus bus;
    private final VBlankNotificationReceiver vBlankNotificationReceiver;
    private final PixelConsumer pixelConsumer;

    private byte regOamAddr;
    private byte regOamData;

    private boolean w;
    private short t;
    private short v;
    private byte x;

    private int cycleInFrame;
    private byte ppuDataReadBuf;

    private boolean odd;
    private boolean vBlank;
    private boolean vRamPpuDataIncDown;
    private boolean vBlankNotify;
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
    private short attributeLoShifter;
    private short attributeHiShifter;

    public PPU2C02(Mapper mapper, VBlankNotificationReceiver vBlankNotificationReceiver, PixelConsumer pixelConsumer) {
        this(new PPU2C02Bus(mapper), vBlankNotificationReceiver, pixelConsumer);
    }

    PPU2C02(Bus bus, VBlankNotificationReceiver vBlankNotificationReceiver, PixelConsumer pixelConsumer) {
        this.bus = bus;
        this.vBlankNotificationReceiver = vBlankNotificationReceiver;
        this.pixelConsumer = pixelConsumer;
    }

    PPU2C02(Bus bus) {
        this(bus, () -> {}, (_) -> {});
    }

    public void tick() {
        final int line = getCurrentLine();
        final int cycle = getCycleInline();
        if (line < 240 || line == 261) {
            if (cycle > 0 && cycle <= 256) {
                handleFetchCycles(cycle);
                if (line != 261) {
                    producePixel();
                }
                if (cycle == 256) {
                    incrementVVertical();
                }
            } else if (cycle == 257) {
                resetVHorizontal();
            } else if (cycle > 320 && cycle <= 336) {
                handleFetchCycles(cycle);
            }
            if (line == 261) {
                if (cycle == 1) {
                    vBlank = false;
                } else if (cycle == 340 && !odd) {
                    incrementCycleCounter();
                } else if (cycle > 279 && cycle <= 304) {
                    resetVVertical();
                }
            }
        } else if (line == 241 && cycle == 1) {
            vBlank = true;
            if (vBlankNotify) {
                vBlankNotificationReceiver.onPpuVBlank();
            }
        }

        incrementCycleCounter();
    }

    // TODO x-scroll
    private void producePixel() {
        short paletteIndex = (short) (((patternLoShifter & 0x8000) >>> 15)
                        | (((patternHiShifter & 0x8000) >>> 15) << 1)
                        | (((attributeLoShifter & 0x8000) >>> 15) << 2)
                        | (((attributeHiShifter & 0x8000) >>> 15) << 3)
                        | (0 << 4));                                 // TODO background/sprite select

        if (greyScale) {
            paletteIndex &= 0x30;
        }

        short pixel = showBg ? bus.read((short) (0x3F00 | paletteIndex)) : 0;
        pixelConsumer.onPixel((short) (pixel | (emphasis << 5)));
    }

    private void incrementCycleCounter() {
        cycleInFrame = (cycleInFrame + 1) % (CYCLES_PER_FRAME);
        if (cycleInFrame == 0) {
            odd = !odd;
        }
    }

    private void handleFetchCycles(int cycle) {
        shiftRegisters();
        switch ((cycle - 1) % 8) {
            case 0 -> {
                reloadShifters();
                loadTile();
            }
            case 2 -> loadAttribute();
            case 4 -> loadPatternLow();
            case 6 -> loadPatternHigh();
            case 7 -> incrementVHorizontal();
        }
    }

    private void shiftRegisters() {
        patternHiShifter = (short) (toUint(patternHiShifter) << 1);
        patternLoShifter = (short) (toUint(patternLoShifter) << 1);
        attributeHiShifter = (short) (toUint(attributeHiShifter) << 1);
        attributeLoShifter = (short) (toUint(attributeLoShifter) << 1);
    }

    private void loadPatternHigh() {
        patternHi = bus.read((short) (toUint(patternTableAddress) + (toUint(nt) << 4) + ((toUint(v) >>> 12) & 0x7) + 8));
    }

    private void loadPatternLow() {
        patternLo = bus.read((short) (toUint(patternTableAddress) + (toUint(nt) << 4) + ((toUint(v) >>> 12) & 0x7)));
    }

    private void loadAttribute() {
        final int vInt = toUint(v);
        final byte attributes = bus.read((short) (0x23C0 | (vInt & 0x0C00) | ((vInt >>> 4) & 0x38) | ((vInt >>> 2) & 0x07)));
        at = (byte) (toUint(attributes) >>> ((vInt & 2) + ((vInt & 64) == 64 ? 4 : 0)));
    }

    private void loadTile() {
        nt = bus.read((short) (0x2000 | (toUint(v) & 0x0FFF)));
    }

    private void reloadShifters() {
        patternLoShifter |= (short) toUint(patternLo);
        patternHiShifter |= (short) toUint(patternHi);

        attributeLoShifter = (short) (toUint(attributeLoShifter) | ((at & 0b01) == 0b01 ? 0x00ff : 0));
        attributeHiShifter = (short) (toUint(attributeHiShifter) | ((at & 0b10) == 0b10 ? 0x00ff : 0));
    }

    private void incrementVHorizontal() {
        if (renderingEnabled()) {
            final int intV = toUint(v);
            if ((intV & 0x001F) == 31) {                             // if coarse X == 31
                v = (short) ((intV & ~0x001F) ^ 0x0400);             // switch horizontal nametable
            } else {
                v = (short) (intV + 1);                              // increment coarse X
            }
        }
    }

    private void incrementVVertical() {
        if (renderingEnabled()) {
            int intV = toUint(v);
            if ((intV & 0x7000) != 0x7000) {                         // if fine Y < 7
                intV += 0x1000;                                      // increment fine Y
            } else {
                intV &= ~0x7000;                                     // fine Y = 0
                int y = (intV & 0x03E0) >> 5;                        // let y = coarse Y
                if (y == 29) {
                    y = 0;                                           // coarse Y = 0
                    intV ^= 0x0800;                                  // switch vertical nametable
                } else if (y == 31) {
                    y = 0;                                           // coarse Y = 0, nametable not switched
                } else {
                    y += 1;                                          // increment coarse Y
                }
                intV = (short) ((intV & ~0x03E0) | (y << 5));        // put coarse Y back into
            }
            v = (short) intV;
        }
    }

    private void resetVHorizontal() {
        if (renderingEnabled()) {
            int vInt = toUint(v);
            final int tInt = toUint(t);

            // set nametable x
            final int ntxmask = 1 << 10;
            vInt &= ~ntxmask;
            vInt |= tInt & ntxmask;

            // set coarse x
            final int coarsexmask = 0x1F;
            vInt &= ~coarsexmask;
            vInt |= tInt & coarsexmask;

            v = (short) vInt;
        }
    }

    private void resetVVertical() {
        if (renderingEnabled()) {
            int vInt = toUint(v);
            final int tInt = toUint(t);

            // set fine y
            final int fineymask = 0x7 << 12;
            vInt &= ~fineymask;
            vInt |= tInt & fineymask;

            // set nametable y
            final int ntymask = 0x1 << 11;
            vInt &= ~ntymask;
            vInt |= tInt & ntymask;

            // set coarse y
            final int coarseymask = 0x1F << 5;
            vInt &= ~coarseymask;
            vInt |= tInt & coarseymask;

            v = (short) vInt;
        }
    }

    private int getCurrentLine() {
        return cycleInFrame / PX_PER_LINE;
    }

    private int getCycleInline() {
        return cycleInFrame % PX_PER_LINE;
    }

    private boolean renderingEnabled() {
        return showBg || showSp;
    }

    public void writeRegPpuCtrl(byte ctrl) {
        /*
            t: ...GH.. ........ <- d: ......GH
         */
        final short gh = (short) (toUint(((byte) (ctrl << 6))) << 4);// 0000GH00 00000000
        t |= gh;
        t &= (short) (gh | 0b11110011_11111111);

        vRamPpuDataIncDown = (ctrl & 0x4) == 0x4;
        patternTableAddress = (short) ((ctrl & 0x10) << 8);

        vBlankNotify = (ctrl & 0x80) == 0x80;
        if (vBlank && vBlankNotify) {
            vBlankNotificationReceiver.onPpuVBlank();
        }

        // TODO remaining flags
    }

    public void writeRegPpuMask(byte regPpuMask) {
        greyScale = (regPpuMask & 1) == 1;
        showBgLeft = (regPpuMask & 2) == 2;
        showSpLeft = (regPpuMask & 4) == 4;
        showBg = (regPpuMask & 8) == 8;
        showSp = (regPpuMask & 0x10) == 0x10;
        emphasis = (byte) ((regPpuMask & 0xE0) >>> 5);
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

    public void writeRegOamAddr(byte regOamAddr) {
        this.regOamAddr = regOamAddr;
    }

    public byte readRegOamData() {
        return oam[toUint(regOamAddr)];
    }

    public void writeRegOamData(byte data) {
        oam[toUint(regOamAddr)] = data;
        regOamAddr++;
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

    public byte[] getOam() {
        return oam;
    }
}
