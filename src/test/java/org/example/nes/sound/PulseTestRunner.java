package org.example.nes.sound;

import org.example.nes.Emulator;
import org.example.nes.mapper.Mapper;
import org.example.nes.mapper.NROM;

import java.io.IOException;

public class PulseTestRunner {
    private static final byte[] PULSE_TEST_PROGRAM = new byte[] {
            (byte) 0xA9, // LDA_IMM
            (byte) 0xBF, // 1011_1111 (duty 2, lc halt, constant volume, volume 15)
            (byte) 0x8D, // STA_ABS
            (byte) 0x00, // register lb
            (byte) 0x40, // register hb
            (byte) 0xA9, // LDA_IMM
            (byte) 0x00, // 0000_0000 (sweep disabled)
            (byte) 0x8D, // STA_ABS
            (byte) 0x01, // register lb
            (byte) 0x40, // register hb
            (byte) 0xA9, // LDA_IMM
            (byte) 0x17, // timer low
            (byte) 0x8D, // STA_ABS
            (byte) 0x02, // register lb
            (byte) 0x40, // register hb
            (byte) 0xA9, // LDA_IMM
            (byte) 0x01, // 0000_0001 (enable pulse 1)
            (byte) 0x8D, // STA_ABS
            (byte) 0x15, // register lb
            (byte) 0x40, // register hb
            (byte) 0xA9, // LDA_IMM
            (byte) 0x09, // 0000_1001 (length counter 1, timer high)
            (byte) 0x8D, // STA_ABS
            (byte) 0x03, // register lb
            (byte) 0x40, // register hb
            (byte) 0xD0, // BNE_REL
            (byte) 0xFE  // -2 (infinite loop)
    };

    public static void main(String[] args) throws IOException {
        final byte[] prgRom = new byte[0x8000];
        System.arraycopy(PULSE_TEST_PROGRAM, 0, prgRom, 0, PULSE_TEST_PROGRAM.length);
        prgRom[0x7FFD] = (byte) 0x80; // initialize reset vector to start of prgRom
        final Mapper mapper = new NROM(prgRom, new byte[0x2000]);

        new Emulator().start(mapper);
    }
}
