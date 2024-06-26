package org.example.nes.display;


public interface PixelConsumer {

    /*
        8  bit    0
        - ---- ----
        B GRpp pppp
        | |||| ||||
        | ||++ ++++- Palette index
        | |+-- ----- Emphasize red
        | +--- ----- Emphasize green
        + ---- ----- Emphasize blue
    */
    void onPixel(short pixel);
}
