package org.example.nes.ppu;

@FunctionalInterface
public interface VBlankNotificationReceiver {
    void onPpuVBlank();
}
