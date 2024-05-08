package org.example.nes.display;

import java.awt.*;
import java.awt.image.ImageProducer;

public class NESFrame extends Frame {
    final Image image;
    final int scale;

    public NESFrame(ImageProducer imageProducer, int scale) {
        setLocationByPlatform(true);
        setVisible(true);
        setResizable(false);
        setIgnoreRepaint(true);
        this.image = createImage(imageProducer);
        this.scale = scale;
        setSize(image.getWidth(this) * scale, image.getHeight(this) * scale);
    }

    public NESFrame(ImageProducer imageProducer) {
        this(imageProducer, 1);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
    }
}
