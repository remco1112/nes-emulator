package org.example.nes.bridge;

import org.example.nes.mapper.Mapper;
import org.example.nes.mapper.NROM;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class DMATest {
    private static final byte[] DMA_TEST_PROGRAM = new byte[] {
            (byte) 0xA2, // LDX_IMM
            (byte) 0x90, // data address in rom + rom offset
            (byte) 0xAD, // LDA_ABS
            (byte) 0x02, // PPU status register lb
            (byte) 0x20, // PPU status register hb
            (byte) 0x29, // AND_IMM
            (byte) 0x80, // PPU status vblank flag
            (byte) 0xf0, // BEQ_REL
            (byte) 0xF9, // -7 (repeat ppu status check)
            (byte) 0x8e, // STX_ABS
            (byte) 0x14, // DMA register lb
            (byte) 0x40, // DMA register hb
            (byte) 0xD0, // BNE_REL
            (byte) 0xFE  // -2 (infinite loop)
    };

    private static final byte[] OAM_CONTENT = new byte[0x100];

    static {
        for (int i = 0; i < OAM_CONTENT.length; i++) {
            OAM_CONTENT[i] = (byte) i;
        }
    }

    @Test
    void test() {
        final byte[] prgRom = new byte[0x8000];
        System.arraycopy(DMA_TEST_PROGRAM, 0, prgRom, 0, DMA_TEST_PROGRAM.length);
        System.arraycopy(OAM_CONTENT, 0, prgRom, 0x1000, OAM_CONTENT.length);
        prgRom[0x7FFD] = (byte) 0x80; // initialize reset vector to start of prgRom

        final Mapper mapper = new NROM(prgRom, new byte[0x2000]);

        final MasterClock clock = new MasterClock(mapper, (_) -> {
        });

        for (int i = 0; i < 85000; i++) {
            clock.tick();
        }

        assertArrayEquals(OAM_CONTENT, clock.oam.createSnapshot());
    }
}
