package org.example.nes.display;

import java.io.IOException;

public class PaletteTestRunner {
    public static void main(String[] args) throws IOException {
        final byte[] palette;
        try(var paletteIs = PaletteColorModel.class.getResourceAsStream("default.pal")) {
            palette = paletteIs.readAllBytes();
        }

        final NESImageProducer nesImageProducer = new NESImageProducer(new PaletteColorModel(palette));
        final NESFrame nesFrame = new NESFrame(nesImageProducer, 3);

        int i = 0;
        while(true) {
            nesImageProducer.write(i % 512);
            i = (i + 1) % 512;
        }
    }
}
