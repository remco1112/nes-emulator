package org.example.nes.display;

import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;

public class NESImageProducer extends MemoryImageSource implements PixelConsumer {
    private static final int SCREEN_WIDTH = 256;
    private static final int SCREEN_HEIGHT = 240;
    private static final int RASTER_COLOR = 0;

    private final int[] pixels;
    private final boolean drawRaster;
    private int nextPixel = 0;

    private NESImageProducer(int[] pixels, ColorModel colorModel, boolean drawRaster) {
        super(SCREEN_WIDTH, SCREEN_HEIGHT, colorModel, pixels, 0, SCREEN_WIDTH);
        setAnimated(true);
        this.pixels = pixels;
        this.drawRaster = drawRaster;
    }

    public NESImageProducer(ColorModel colorModel, boolean drawRaster) {
        this(new int[SCREEN_WIDTH * SCREEN_HEIGHT], colorModel, drawRaster);
    }

    public NESImageProducer(ColorModel colorModel) {
        this(colorModel, false);
    }

    public void write(int color) {
        pixels[nextPixel] = drawRaster && isRasterPixel(nextPixel) ? RASTER_COLOR : color;
        newPixels(nextPixel % SCREEN_WIDTH, nextPixel / SCREEN_WIDTH,1,1, nextPixel == SCREEN_WIDTH * SCREEN_HEIGHT - 1);
        nextPixel = (nextPixel + 1) % (SCREEN_WIDTH * SCREEN_HEIGHT);
    }

    @Override
    public void onPixel(short pixel) {
        write(pixel);
    }

    private boolean isRasterPixel(int pixel) {
        return ((pixel % 8 == 7) && ((pixel / SCREEN_WIDTH) % 2 == 0)) || ((pixel / SCREEN_WIDTH) % 8 == 7) && (pixel % 2 == 0);
    }
}
