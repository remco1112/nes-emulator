package org.example.nes;

import org.example.nes.mapper.INESLoader;
import org.example.nes.mapper.Mapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Main {
    private Main() {

    }

    public static void main(String[] args) throws IOException {
        final INESLoader inesLoader = new INESLoader();
        final Mapper mapper = inesLoader.loadRom(Files.newInputStream(Path.of(args[0])));
    }
}
