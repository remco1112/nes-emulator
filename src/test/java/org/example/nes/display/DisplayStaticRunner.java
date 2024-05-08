package org.example.nes.display;

import java.awt.*;
import java.awt.image.ColorModel;

public class DisplayStaticRunner {

    public static void main(String[] args) {
        final NESImageProducer nesImageProducer = new NESImageProducer(ColorModel.getRGBdefault());
        final NESFrame nesFrame = new NESFrame(nesImageProducer, 3);

        while (true) {
            for (int x = 0; x < 256; x++) {
                for (int y = 0; y < 240; y++) {
                    boolean rand = Math.random() > 0.5;
                    nesImageProducer.write(rand ? Color.black.getRGB() : Color.white.getRGB());
                }
            }
        }
    }
}
