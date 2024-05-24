package org.example.nes.ppu;

import static org.example.nes.UInt.toUint;

class SpriteEvaluator {
    // y, tile, attribute, x
    private final byte[] secondaryOam = new byte[32];
    private final OAMAccesor oamAccesor;

    private byte oamReadBuf;
    private byte n;
    private byte secondaryOamIndex;
    private byte foundSpriteStartAddr;
    private byte spriteByteFetchCount;
    private boolean incrementSecondaryOamIndex;

    SpriteEvaluator(OAMAccesor oamAccesor) {
        this.oamAccesor = oamAccesor;
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
            incrementSecondaryOamIndex = true;
            oamReadBuf = oamAccesor.readRegOamData();
            if (cycle > 64) {
                byte oamAddress = oamAccesor.readRegOamAddr();
                if (toUint(oamAddress) - toUint(foundSpriteStartAddr) < 4 && foundSpriteStartAddr != -1) {
                    spriteByteFetchCount++;
                    oamAccesor.writeRegOamAddr((byte) (oamAddress + 1));
                } else {
                    if (yInRange(line)) {
                        assert toUint(oamAddress) % 4 == 0;
                        foundSpriteStartAddr = oamAddress;
                        spriteByteFetchCount++;
                        oamAccesor.writeRegOamAddr((byte) (oamAddress + 1));
                    } else {
                        oamAccesor.writeRegOamAddr((byte) (oamAddress + 4));
                        incrementSecondaryOamIndex = false;
                    }
                    n++;
                }
            }
        } else {
            if (spriteByteFetchCount <= 8 * 4 && n <= 64) {
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
        return Integer.min(spriteByteFetchCount / 4, 8);
    }
}
