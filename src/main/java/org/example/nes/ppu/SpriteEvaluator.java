package org.example.nes.ppu;

import static org.example.nes.UInt.toUint;

class SpriteEvaluator {
    // y, tile, attribute, x
    private final byte[] secondaryOam = new byte[32];
    private final OAM oam;

    private byte oamReadBuf;
    private byte spritesEvaluated;
    private byte spritesFound;
    private byte secondaryOamCounter;
    private byte foundSpriteStartAddr;
    private boolean incrementSecondaryOamIndex;

    private int cycle;

    SpriteEvaluator(OAM oam) {
        this.oam = oam;
    }

    // TODO 8x16 sprites
    // TODO overflow when oam addr does not start at 0
    // TODO Sprite overflow flag
    void tick() {
        if (cycle % 2 == 0) {
            if (cycle % 256 == 0) {
                foundSpriteStartAddr = -1;
                spritesEvaluated = -1;
                spritesFound = 0;
                secondaryOamCounter = 0;
            }
            oam.setPullReadsHigh(cycle % 256 < 64);
            incrementSecondaryOamIndex = true;
            oamReadBuf = oam.readRegOamData();
            if (cycle % 256 >= 64) {
                byte oamAddress = oam.readRegOamAddr();
                final int offsetFromFoundSprite = toUint(oamAddress) - toUint(foundSpriteStartAddr);
                if (offsetFromFoundSprite > 0 && offsetFromFoundSprite < 4 && foundSpriteStartAddr != -1) {
                    oam.writeRegOamAddr((byte) (oamAddress + 1));
                } else {
                    spritesEvaluated++;
                    if (yInRange(cycle)) {
                        assert toUint(oamAddress) % 4 == 0;
                        foundSpriteStartAddr = oamAddress;
                        if (spritesFound < 8 && spritesEvaluated < 64) {
                            spritesFound++;
                        }
                        oam.writeRegOamAddr((byte) (oamAddress + 1));
                    } else {
                        oam.writeRegOamAddr((byte) (oamAddress + 4));
                        incrementSecondaryOamIndex = false;
                    }
                }
            }
        } else {
            if (secondaryOamCounter < 64 && spritesEvaluated < 64) {
                secondaryOam[secondaryOamCounter % 32] = oamReadBuf;
            }
            if (incrementSecondaryOamIndex) {
                secondaryOamCounter++;
                incrementSecondaryOamIndex = false;
            }
        }
        cycle = (cycle + 1) % (256 * 240);
    }

    private boolean yInRange(int cycle) {
        final int y = toUint(oamReadBuf);
        final int line = cycle / 256;
        return line >= y && line < y + 8;
    }

    byte readSecondaryOam(int index) {
        return secondaryOam[index];
    }

    int getNumberOfSpritesInSecondaryOam() {
        return spritesFound;
    }
}
