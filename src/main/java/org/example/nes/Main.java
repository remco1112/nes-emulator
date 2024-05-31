package org.example.nes;

import org.example.nes.bridge.MasterClock;
import org.example.nes.display.AWTStandardControllerAdapter;
import org.example.nes.display.NESFrame;
import org.example.nes.display.NESImageProducer;
import org.example.nes.display.PaletteColorModel;
import org.example.nes.input.InputController;
import org.example.nes.input.NullInputDevice;
import org.example.nes.input.StandardControllerInputDevice;
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
        final AWTStandardControllerAdapter awtStandardControllerAdapter = new AWTStandardControllerAdapter();
        nesFrame.addKeyListener(awtStandardControllerAdapter);

        final INESLoader inesLoader = new INESLoader();
        final Mapper mapper = inesLoader.loadRom(Files.newInputStream(Path.of(args[0])));
        final InputController inputController = new InputController(new StandardControllerInputDevice(awtStandardControllerAdapter), new NullInputDevice());

        final MasterClock masterClock = new MasterClock(mapper, nesImageProducer, inputController);
        masterClock.start();
    }
}
