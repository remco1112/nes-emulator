package org.example.nes.ppu;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.When;
import com.pholser.junit.quickcheck.generator.Size;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(JUnitQuickcheck.class)
public class SpriteEvaluatorTest {

    @Property
    public void secondaryOamContentsCorrectForEachLine(@When(seed = -4308380896438925083L) @Size(min = 256, max = 256) ArrayList<Byte> oamList) {
        final Byte[] boxedOam = oamList.toArray(new Byte[256]);
        final byte[] oam = new byte[256];
        for (int i = 0; i < 256; i++) {
            oam[i] = boxedOam[i];
        }

        if (Arrays.hashCode(oam) == -1111066093) {
            System.out.println();
        }

        SpriteEvaluator spriteEvaluator = new SpriteEvaluator(new OAM(oam));
        int spriteCount;
        boolean spriteOverflow;
        for (int line = 0; line < 240; line++) {
            spriteCount = 0;
            spriteOverflow = false;

            for (int i = 0; i < 256; i++) {
                spriteEvaluator.tick();
            }

            for(int i = 0; i < 64; i += 4) {
               if (line >= oam[i] && line < oam[i] + 8) {
                   if (spriteCount < 8) {
                       for (int j = 0; j < 4; j++) {
                           assertEquals(oam[i + j], spriteEvaluator.readSecondaryOam(4 * spriteCount + j));
                       }
                       spriteCount++;
                   } else {
                       spriteOverflow = true;
                       break;
                   }
               }
            }

            if (spriteCount < 8) {
                assertEquals(oam[63 * 4], spriteEvaluator.readSecondaryOam(4 * spriteCount));

                for (int i = 4 * spriteCount + 1; i < 32; i++) {
                    assertEquals(-1, spriteEvaluator.readSecondaryOam(i));
                }
            }
        }
    }
}
