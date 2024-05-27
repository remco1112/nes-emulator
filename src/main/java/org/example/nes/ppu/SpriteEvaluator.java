package org.example.nes.ppu;

import static org.example.nes.UInt.toUint;

class SpriteEvaluator {
    // y, tile, attribute, x
    private final byte[] secondaryOam = new byte[32];
    private final OAM oam;

    private byte oamReadBuf;
    private byte n;
    private byte secondaryOamIndex;
    private byte foundSpriteStartAddr;
    private byte spriteByteFetchCount;
    private boolean incrementSecondaryOamIndex;

    SpriteEvaluator(OAM oam) {
        this.oam = oam;
    }

    // TODO 8x16 sprites
    // TODO overflow when oam addr does not start at 0
    // TODO Sprite overflow flag
    void tick(int cycle, int line) {
        if (cycle % 2 == 1) {
            if (cycle == 1) {
                foundSpriteStartAddr = -1;
                n = 0;
                spriteByteFetchCount = 0;
                secondaryOamIndex = 0;
            }
            oam.setPullReadsHigh(cycle <= 64);
            incrementSecondaryOamIndex = true;
            oamReadBuf = oam.readRegOamData();
            if (cycle > 64) {
                byte oamAddress = oam.readRegOamAddr();
                if (toUint(oamAddress) - toUint(foundSpriteStartAddr) < 4 && foundSpriteStartAddr != -1) {
                    if (spriteByteFetchCount < 8 * 4 && n < 64) {
                        spriteByteFetchCount++;
                    }
                    oam.writeRegOamAddr((byte) (oamAddress + 1));
                } else {
                    if (yInRange(line)) {
                        assert toUint(oamAddress) % 4 == 0;
                        foundSpriteStartAddr = oamAddress;
                        if (spriteByteFetchCount < 8 * 4 && n < 64) {
                            spriteByteFetchCount++;
                        }
                        oam.writeRegOamAddr((byte) (oamAddress + 1));
                    } else {
                        oam.writeRegOamAddr((byte) (oamAddress + 4));
                        incrementSecondaryOamIndex = false;
                    }
                    n++;
                }
            }
        } else {
            if (spriteByteFetchCount < 8 * 4 && n < 64) { // TODO this condition is broken (see DK peach sprite)
                secondaryOam[secondaryOamIndex] = oamReadBuf;
            }
            if (incrementSecondaryOamIndex) {
                secondaryOamIndex = (byte) ((secondaryOamIndex + 1) % 32);
                incrementSecondaryOamIndex = false;
            }
        }
    }

    private boolean yInRange(int line) {
        final int y = toUint(oamReadBuf);
        return line >= y && line < y + 8;
    }

    byte readSecondaryOam(int index) {
        return secondaryOam[index];
    }

    int getNumberOfSpritesInSecondaryOam() {
        return spriteByteFetchCount / 4;
    }
}
