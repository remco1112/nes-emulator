package org.example.nes.ppu;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.Size;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.example.nes.UInt.toUint;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(JUnitQuickcheck.class)
public class SpriteEvaluatorTest {

    @Property
    public void secondaryOamContentsCorrectForEachLine(@Size(min = 256, max = 256) ArrayList<Byte> oamList) {
        final Byte[] boxedOam = oamList.toArray(new Byte[256]);
        final byte[] oamdata = new byte[256];
        for (int i = 0; i < 256; i++) {
            oamdata[i] = boxedOam[i];
        }

        final OAM oam = new OAM(oamdata);

        SpriteEvaluator spriteEvaluator = new SpriteEvaluator(oam);
        int spriteCount;
        boolean spriteOverflow;
        for (int line = 0; line < 240; line++) {
            spriteCount = 0;
            spriteOverflow = false;

            oam.writeRegOamAddr((byte) 0);

            for (int i = 0; i < 256; i++) {
                spriteEvaluator.tick();
            }

            for(int i = 0; i < 256; i += 4) {
               if (line >= toUint(oamdata[i]) && line < toUint(oamdata[i]) + 8) {
                   if (spriteCount < 8) {
                       for (int j = 0; j < 4; j++) {
                           assertEquals(oamdata[i + j], spriteEvaluator.readSecondaryOam(4 * spriteCount + j));
                       }
                       spriteCount++;
                   } else {
                       spriteOverflow = true;
                       break;
                   }
               }
            }

            if (spriteCount < 8) {
                if (spriteCount > 0 && spriteEvaluator.readSecondaryOam(4 * (spriteCount - 1)) != oamdata[63 * 4]) {
                    assertEquals(oamdata[63 * 4], spriteEvaluator.readSecondaryOam(4 * spriteCount));
                }

                for (int i = 4 * spriteCount + 1; i < 32; i++) {
                    assertEquals(-1, spriteEvaluator.readSecondaryOam(i));
                }
            }
        }
    }
}
