package org.example.nes.display;

import java.awt.image.MemoryImageSource;

public class NESImageProducer extends MemoryImageSource {
    private static final int SCREEN_WIDTH = 256;
    private static final int SCREEN_HEIGHT = 240;

    private final int[] pixels;
    private int nextPixel = 0;

    private NESImageProducer(int[] pixels) {
        super(SCREEN_WIDTH, SCREEN_HEIGHT, pixels, 0, SCREEN_WIDTH);
        setAnimated(true);
        this.pixels = pixels;
    }

    public NESImageProducer() {
        this(new int[SCREEN_WIDTH * SCREEN_HEIGHT]);
    }

    public void write(int color) {
        pixels[nextPixel] = color;
        newPixels(nextPixel % SCREEN_WIDTH, nextPixel / SCREEN_HEIGHT,1,1, nextPixel == SCREEN_WIDTH * SCREEN_HEIGHT - 1);
        nextPixel = (nextPixel + 1) % (SCREEN_WIDTH * SCREEN_HEIGHT);
    }
}
