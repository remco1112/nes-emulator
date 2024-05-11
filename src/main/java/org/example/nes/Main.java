package org.example.nes;

import org.example.nes.bridge.MasterClock;
import org.example.nes.display.NESFrame;
import org.example.nes.display.NESImageProducer;
import org.example.nes.display.PaletteColorModel;
import org.example.nes.mapper.INESLoader;
import org.example.nes.mapper.Mapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Main {
    private Main() {

    }

    public static void main(String[] args) throws IOException {
        final byte[] palette;
        try(var paletteIs = PaletteColorModel.class.getResourceAsStream("default.pal")) {
            palette = paletteIs.readAllBytes();
        }

        final NESImageProducer nesImageProducer = new NESImageProducer(new PaletteColorModel(palette));
        final NESFrame nesFrame = new NESFrame(nesImageProducer, 3);

        final INESLoader inesLoader = new INESLoader();
        final Mapper mapper = inesLoader.loadRom(Files.newInputStream(Path.of(args[0])));
        final MasterClock masterClock = new MasterClock(mapper, nesImageProducer);
        masterClock.start();
    }
}
