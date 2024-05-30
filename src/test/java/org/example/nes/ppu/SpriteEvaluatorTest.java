package org.example.nes.ppu;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.Size;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.example.nes.utils.UInt.toUint;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(JUnitQuickcheck.class)
public class SpriteEvaluatorTest {
    private static final int OAM_SIZE = 256;
    private byte[] oamData;
    private OAM oam;
    private SpriteEvaluator spriteEvaluator;

    @Property
    public void secondaryOamContentsCorrectForEachLine(@Size(min = OAM_SIZE, max = OAM_SIZE) ArrayList<Byte> oamList) {
        initializeFields(oamList);

        for (int line = 0; line < 240; line++) {
            testSecondaryOamContentsCorrectForLine(line);
        }
    }

    private void initializeFields(ArrayList<Byte> oamList) {
        oamData = toByteArray(oamList);
        oam = new OAM(oamData);
        spriteEvaluator = new SpriteEvaluator(oam);
    }

    private void testSecondaryOamContentsCorrectForLine(int line) {
        oam.writeRegOamAddr((byte) 0);

        runSpriteEvaluatorForOneLine();

        int spriteCount = 0;
        for(int i = 0; i < OAM_SIZE; i += 4) {
           if (spriteInRange(line, oamData[i])) {
               if (spriteCount < 8) {
                   assertSecondaryOamContainsSprite(i, 4 * spriteCount++);
               } else {
                   // TODO Sprite overflow
                   break;
               }
           }
        }

        if (spriteCount < 8) {
            assertYOfSprite63InSecondaryOam(spriteCount);
            assertRemainderOfSecondaryOamClear(spriteCount);
        }
    }

    private void assertYOfSprite63InSecondaryOam(int spriteCount) {
        if (sprite63NotLastSpriteInSecondaryOam(spriteCount)) {
            assertEquals(oamData[63 * 4], spriteEvaluator.readSecondaryOam(4 * spriteCount));
        }
    }

    private boolean sprite63NotLastSpriteInSecondaryOam(int spriteCount) {
        return spriteCount > 0 && spriteEvaluator.readSecondaryOam(4 * (spriteCount - 1)) != oamData[63 * 4];
    }

    private void assertRemainderOfSecondaryOamClear(int spriteCount) {
        for (int i = 4 * spriteCount + 1; i < 32; i++) {
            assertEquals(-1, spriteEvaluator.readSecondaryOam(i));
        }
    }

    private void assertSecondaryOamContainsSprite(int spriteIndexInOam, int spriteIndexInSecondaryOam) {
        for (int j = 0; j < 4; j++) {
            assertEquals(oamData[spriteIndexInOam + j], spriteEvaluator.readSecondaryOam(spriteIndexInSecondaryOam + j));
        }
    }

    private boolean spriteInRange(int line, byte spriteY) {
        return line >= toUint(spriteY) && line < toUint(spriteY) + 8;
    }

    private void runSpriteEvaluatorForOneLine() {
        for (int i = 0; i < OAM_SIZE; i++) {
            spriteEvaluator.tick();
        }
    }

    private static byte[] toByteArray(ArrayList<Byte> oamList) {
        final byte[] oamData = new byte[OAM_SIZE];
        for (int i = 0; i < OAM_SIZE; i++) {
            oamData[i] = oamList.get(i);
        }
        return oamData;
    }
}
