package org.example.nes.sound;

import org.example.nes.Emulator;
import org.example.nes.mapper.Mapper;
import org.example.nes.mapper.NROM;

import java.io.IOException;

public class TriangleTestRunner {
    private static final byte[] TRIANGLE_TEST_PROGRAM = new byte[] {
            (byte) 0xA9, // LDA_IMM
            (byte) 0xC0, // 11000000 (control/lc halt, linear counter)
            (byte) 0x8D, // STA_ABS
            (byte) 0x08, // register lb
            (byte) 0x40, // register hb
            (byte) 0xA9, // LDA_IMM
            (byte) 0x8B, // timer low
            (byte) 0x8D, // STA_ABS
            (byte) 0x0A, // register lb
            (byte) 0x40, // register hb
            (byte) 0xA9, // LDA_IMM
            (byte) 0x03, // 0000_0100 (enable triangle)
            (byte) 0x8D, // STA_ABS
            (byte) 0x15, // register lb
            (byte) 0x40, // register hb
            (byte) 0xA9, // LDA_IMM
            (byte) 0x08, // 0000_1000 (length counter 1, timer high)
            (byte) 0x8D, // STA_ABS
            (byte) 0x0B, // register lb
            (byte) 0x40, // register hb
            (byte) 0xD0, // BNE_REL
            (byte) 0xFE  // -2 (infinite loop)
    };

    public static void main(String[] args) throws IOException {
        final byte[] prgRom = new byte[0x8000];
        System.arraycopy(TRIANGLE_TEST_PROGRAM, 0, prgRom, 0, TRIANGLE_TEST_PROGRAM.length);
        prgRom[0x7FFD] = (byte) 0x80; // initialize reset vector to start of prgRom
        final Mapper mapper = new NROM(prgRom, new byte[0x2000]);

        new Emulator().start(mapper);
    }
}
