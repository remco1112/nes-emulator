package org.example.nes.ppu;

import static org.example.nes.utils.UInt.toUint;

class SpriteEvaluator {
    private static final int EVALUATION_START_CYCLE = 64;
    private static final int LINE_LENGTH = 256;
    private static final int LINES = 240;
    private static final int MAX_SPRITES_IN_OAM = 64;
    private static final int MAX_SPRITES_IN_LINE = 8;
    private static final int SPRITE_SIZE = 4;
    private static final int SECONDARY_OAM_SIZE = 32;
    private static final int SPRITE_HEIGHT = 8;
    private static final int NO_SPRITE_FOUND_ADDRESS = -1;

    // y, tile, attribute, x
    private final byte[] secondaryOam = new byte[SECONDARY_OAM_SIZE];
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
        if (isReadCycle()) {
            handleRead();
        } else {
            handleWrite();
        }
        incrementCycleCounter();
    }

    private boolean isReadCycle() {
        return cycle % 2 == 0;
    }

    private void handleRead() {
        if (isFirstCycleInClear()) {
            initializeLineState();
            prepareOamForClear();
        } else if (isFirstCycleInEvaluation()) {
            prepareOamForEvaluation();
        }

        readOam();

        if (isEvaluationCycle()) {
            handleEvaluation();
        }
    }

    private boolean isFirstCycleInClear() {
        return getCycleInLine() == 0;
    }

    private int getCycleInLine() {
        return cycle % LINE_LENGTH;
    }

    private void initializeLineState() {
        foundSpriteStartAddr = NO_SPRITE_FOUND_ADDRESS;
        spritesEvaluated = -1;
        spritesFound = 0;
        secondaryOamCounter = 0;
        enableSecondaryOamAddressIncrements();
    }

    private void enableSecondaryOamAddressIncrements() {
        incrementSecondaryOamIndex = true;
    }

    private void prepareOamForClear() {
        oam.setPullReadsHigh(true);
    }

    private boolean isFirstCycleInEvaluation() {
        return getCycleInLine() == EVALUATION_START_CYCLE;
    }

    private void prepareOamForEvaluation() {
        oam.setPullReadsHigh(false);
    }

    private void readOam() {
        oamReadBuf = oam.readRegOamData();
    }

    private boolean isEvaluationCycle() {
        return getCycleInLine() >= EVALUATION_START_CYCLE;
    }

    private void handleEvaluation() {
        enableSecondaryOamAddressIncrements();
        if (isFetchingRemainingSpriteData()) {
            setOamAddressToNextByte();
        } else {
            evaluateNextSprite();
        }
    }

    private boolean isFetchingRemainingSpriteData() {
        byte oamAddress = oam.readRegOamAddr();
        final int offsetFromFoundSprite = toUint(oamAddress) - toUint(foundSpriteStartAddr);
        return offsetFromFoundSprite > 0
                && offsetFromFoundSprite < SPRITE_SIZE
                && foundSpriteStartAddr != NO_SPRITE_FOUND_ADDRESS;
    }

    private void evaluateNextSprite() {
        spritesEvaluated++;
        if (spriteInRange()) {
            foundSpriteStartAddr = oam.readRegOamAddr();
            if (spritesFound < MAX_SPRITES_IN_LINE && spritesEvaluated < MAX_SPRITES_IN_OAM) {
                spritesFound++;
            }
            setOamAddressToNextByte();
        } else {
            setOamAddressToNextSprite();
            disableSecondaryOamAddressIncrements();
        }
    }

    private boolean spriteInRange() {
        final int y = toUint(oamReadBuf);
        final int line = cycle / LINE_LENGTH;
        return line >= y && line < y + SPRITE_HEIGHT;
    }

    private void setOamAddressToNextByte() {
        incrementOamAddress(1);
    }

    private void setOamAddressToNextSprite() {
        incrementOamAddress(4);
    }

    private void incrementOamAddress(int inc) {
        oam.writeRegOamAddr((byte) (oam.readRegOamAddr() + inc));
    }

    private void disableSecondaryOamAddressIncrements() {
        incrementSecondaryOamIndex = false;
    }

    private void handleWrite() {
        if (writesEnabled()) {
            secondaryOam[secondaryOamCounter % SECONDARY_OAM_SIZE] = oamReadBuf;
        }
        if (incrementSecondaryOamIndex) {
            secondaryOamCounter++;
        }
    }

    private boolean writesEnabled() {
        return secondaryOamCounter < 2 * SECONDARY_OAM_SIZE && spritesEvaluated < MAX_SPRITES_IN_OAM;
    }

    private void incrementCycleCounter() {
        cycle = (cycle + 1) % (LINES * LINE_LENGTH);
    }

    byte readSecondaryOam(int index) {
        return secondaryOam[index];
    }

    int getNumberOfSpritesInSecondaryOam() {
        return spritesFound;
    }
}
