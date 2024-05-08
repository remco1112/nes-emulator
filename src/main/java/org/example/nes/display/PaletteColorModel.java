package org.example.nes.display;

import java.awt.image.IndexColorModel;

/*
    8  bit    0
    - ---- ----
    B GRpp pppp
    | |||| ||||
    | ||++ ++++- PPU output
    | |+-- ----- Emphasize red
    | +--- ----- Emphasize green
    + ---- ----- Emphasize blue
 */

public class PaletteColorModel extends IndexColorModel {
    public PaletteColorModel(byte[] palette) {
        super(9, palette.length / 3, palette, 0, false);
        if (palette.length != 512 * 3) {
            throw new IllegalArgumentException("Invalid palette!");
        }
    }
}
