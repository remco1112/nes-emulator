package org.example.nes.ppu;

import org.example.nes.bus.Bus;
import org.example.nes.bus.BusConfiguration;
import org.example.nes.ppu.bus.PPU2C02Bus;

import static org.example.nes.utils.UInt.toUint;

public class PPU2C02 {
    private static final int LINES = 262;
    private static final int PX_PER_LINE = 341;
    private static final int CYCLES_PER_FRAME = LINES * PX_PER_LINE;

    // y, tile, attribute, x
    private final byte[] secondaryOamBuffer = new byte[32];

    private final Bus bus;
    private final VBlankNotificationReceiver vBlankNotificationReceiver;
    private final SpriteEvaluator spriteEvaluator;
    public final OAM oam; // todo fix visibility

    private int currentSprite;
    private int spriteCount;
    private short spritePatternTableAddress;
    // pattern low, pattern high
    private byte[] spritePatternsWrite = new byte[16];
    private byte[] spritePatternsRead = new byte[16];

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
    private boolean spriteZeroHit;

    private byte emphasis; // BGR
    private short backgroundPatternTableAddress;

    private byte nt;
    private byte at;
    private byte patternLo;
    private byte patternHi;

    private short patternLoShifter;
    private short patternHiShifter;
    private short attributeLoShifter;
    private short attributeHiShifter;

    public PPU2C02(BusConfiguration mapperBusConfiguration, VBlankNotificationReceiver vBlankNotificationReceiver) {
        this(new PPU2C02Bus(mapperBusConfiguration), vBlankNotificationReceiver);
    }

    PPU2C02(Bus bus, VBlankNotificationReceiver vBlankNotificationReceiver) {
        this.bus = bus;
        this.vBlankNotificationReceiver = vBlankNotificationReceiver;
        this.oam = new OAM();
        this.spriteEvaluator = new SpriteEvaluator(oam);
    }

    PPU2C02(Bus bus) {
        this(bus, () -> {});
    }

    public short tick() {
        final int line = getCurrentLine();
        final int cycle = getCycleInline();
        short pixel = -1;
        if (line < 240 || line == 261) {
            if (cycle > 0) {
                if (cycle < 257) {
                    handleBackgroundFetchCycles(cycle);
                    if (line != 261) {
                        spriteEvaluator.tick();
                        pixel = producePixel();
                    }
                    if (cycle == 256) {
                        incrementVVertical();
                    }
                } else if (cycle < 321) {
                    if (cycle == 257) {
                        resetVHorizontal();
                    }
                    handleSpriteFetchCycles(cycle);
                    if (cycle == 320) {
                        final byte[] spritePatternsRead = this.spritePatternsRead;
                        this.spritePatternsRead = spritePatternsWrite;
                        spritePatternsWrite = spritePatternsRead;
                    }
                } else if (cycle < 337) {
                    handleBackgroundFetchCycles(cycle);
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
            }
        } else if (line == 241 && cycle == 1) {
            vBlank = true;
            spriteZeroHit = false;
            if (vBlankNotify) {
                vBlankNotificationReceiver.onPpuVBlank();
            }
        }

        incrementCycleCounter();
        return pixel;
    }

    private short producePixel() {
        short backgroundPaletteIndex = (short) (showBg ? ((((patternLoShifter << toUint(x)) & 0x8000) >>> 15)
                        | ((((patternHiShifter << toUint(x)) & 0x8000) >>> 15) << 1)) : 0);

        if (backgroundPaletteIndex != 0) {
            backgroundPaletteIndex = (short) (toUint(backgroundPaletteIndex) | (((((attributeLoShifter << toUint(x)) & 0x8000) >>> 15) << 2)
                                | ((((attributeHiShifter << toUint(x)) & 0x8000) >>> 15) << 3)));
        }

        if (greyScale) {
            backgroundPaletteIndex &= 0x30;
        }

        short pixel = bus.read((short) (0x3F00 | backgroundPaletteIndex));

        short spritePaletteIndex = 0;
        int spriteIndex = -1;

        for (int i = spriteCount - 1; i >= 0; i--) {
            final int spriteX = toUint(secondaryOamBuffer[4 * i + 3]);
            final int offsetX = getCycleInline() - 1 - spriteX;
            if (offsetX >= 0 && offsetX < 8) {
                final int offsetAfterFlip = ((toUint(secondaryOamBuffer[4 * i + 2]) >>> 6) & 0x1) == 0 ? offsetX : 7 - offsetX;
                final short newSpritePaletteIndex = (short) ((((toUint(spritePatternsRead[2 * i]) << offsetAfterFlip) & 0x80) >>> 7)
                                        | ((((toUint(spritePatternsRead[2 * i + 1]) << offsetAfterFlip) & 0x80) >>> 7) << 1)
                                        | ((toUint(secondaryOamBuffer[4 * i + 2]) & 0x3) << 2)
                                        | 0x10);
                if ((newSpritePaletteIndex & 0x3) != 0) {
                    spritePaletteIndex = newSpritePaletteIndex;
                    spriteIndex = i;
                }
            }
        }

        if (((spritePaletteIndex & 0x3) != 0) && showSp) {
            assert spriteIndex != -1;
            final boolean spriteInFrontOfBackground = ((secondaryOamBuffer[4 * spriteIndex + 2] >>> 5) & 0x1) == 0;
            if (spriteInFrontOfBackground || (backgroundPaletteIndex & 0x3) == 0) {
                pixel = bus.read((short) (0x3F00 | spritePaletteIndex));
            }
            if (spriteIndex == 0 && (backgroundPaletteIndex & 0x3) != 0) {
                spriteZeroHit = true;
            }
        }

        return (short) (pixel | (emphasis << 5));
    }

    private void incrementCycleCounter() {
        cycleInFrame = (cycleInFrame + 1) % (CYCLES_PER_FRAME);
        if (cycleInFrame == 0) {
            odd = !odd;
        }
    }

    private void handleBackgroundFetchCycles(int cycle) {
        shiftRegisters();
        switch ((cycle - 1) % 8) {
            case 0 -> {
                reloadShifters();
                loadTile();
            }
            case 2 -> loadAttribute();
            case 4 -> loadBackgroundPatternLow();
            case 6 -> loadBackgroundPatternHigh();
            case 7 -> incrementVHorizontal();
        }
    }

    private void handleSpriteFetchCycles(int cycle) {
        oam.writeRegOamAddr((byte) 0);
        spriteCount = spriteEvaluator.getNumberOfSpritesInSecondaryOam();
        final int relativeCycle = (cycle - 1) % 8;

        if (relativeCycle >= 0 && relativeCycle < 4) {
            final int secondaryOamIndex = 4 * currentSprite + relativeCycle;
            secondaryOamBuffer[secondaryOamIndex] = spriteEvaluator.readSecondaryOam(secondaryOamIndex);
        }

        switch (relativeCycle) {
            case 0, 2 -> loadTile(); // garbage
            case 4 -> loadSpritePatternLow();
            case 6 -> {
                loadSpritePatternHigh();
                currentSprite = (currentSprite + 1) % 8;
            }
        }
    }

    private void shiftRegisters() {
        patternHiShifter = (short) (toUint(patternHiShifter) << 1);
        patternLoShifter = (short) (toUint(patternLoShifter) << 1);
        attributeHiShifter = (short) (toUint(attributeHiShifter) << 1);
        attributeLoShifter = (short) (toUint(attributeLoShifter) << 1);
    }

    private void loadBackgroundPatternHigh() {
        patternHi = bus.read((short) (toUint(backgroundPatternTableAddress) + (toUint(nt) << 4) + ((toUint(v) >>> 12) & 0x7) + 8));
    }

    private void loadBackgroundPatternLow() {
        patternLo = bus.read((short) (toUint(backgroundPatternTableAddress) + (toUint(nt) << 4) + ((toUint(v) >>> 12) & 0x7)));
    }

    private void loadSpritePatternLow() {
        spritePatternsWrite[2 * currentSprite] = bus.read((short) (
                toUint(spritePatternTableAddress)
                        + (toUint(secondaryOamBuffer[4 * currentSprite + 1]) << 4)
                        + (((toUint(secondaryOamBuffer[4 * currentSprite + 2]) >>> 7) & 0x1) == 0
                            ? (getCurrentLine() - toUint(secondaryOamBuffer[4 * currentSprite]))
                            : 7 - (getCurrentLine() - toUint(secondaryOamBuffer[4 * currentSprite])))));
    }

    private void loadSpritePatternHigh() {
        spritePatternsWrite[2 * currentSprite + 1] = bus.read((short) (
                toUint(spritePatternTableAddress)
                        + (toUint(secondaryOamBuffer[4 * currentSprite + 1]) << 4)
                        + 8
                        + (((toUint(secondaryOamBuffer[4 * currentSprite + 2]) >>> 7) & 0x1) == 0
                            ? (getCurrentLine() - toUint(secondaryOamBuffer[4 * currentSprite]))
                            : 7 - (getCurrentLine() - toUint(secondaryOamBuffer[4 * currentSprite])))));
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
        backgroundPatternTableAddress = (short) ((ctrl & 0x10) << 8);
        spritePatternTableAddress = (short) ((ctrl & 0x8) << 9);

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

    public byte readRegOamAddr() {
        return oam.readRegOamAddr();
    }

    public void writeRegOamAddr(byte regOamAddr) {
        oam.writeRegOamAddr(regOamAddr);
    }

    public byte readRegOamData() {
        return oam.readRegOamData();
    }

    public void writeRegOamData(byte data) {
        oam.writeRegOamData(data);
    }

    // TODO: Race Condition Warning: Reading PPUSTATUS within two cycles of the start of vertical blank will return 0 in bit 7 but clear the latch anyway, causing NMI to not occur that frame.
    // TODO: overflow
    public byte readRegPpuStatus() {
        final byte currentStatus = (byte) ((vBlank ? 0x80 : 0) | (spriteZeroHit ? 0x40 : 0));
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
        if (toUint(v) >= 0x3F00 && toUint(v) <= 0x3FFF) {
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
