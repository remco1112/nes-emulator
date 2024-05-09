package org.example.nes.ppu;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class PPU2C02RegisterTest {

    @ParameterizedTest
    @CsvSource({
            "00000000_00000000, 00000000, 00000000_00000000",
            "11111111_11111111, 11111111, 11111111_11111111",
            "00000000_00000000, 11111111, 00001100_00000000",
            "00000000_00000000, 00000011, 00001100_00000000",
            "11000000_00011000, 00000010, 11001000_00011000"
    })
    void testPpuCtrl(String initialT, String input, String expectedT) {
        PPU2C02 ppu2C02 = new PPU2C02(new PPU2C02Bus());
        ppu2C02.setT(toShort(initialT));

        ppu2C02.writeRegPpuCtrl(toByte(input));

        assertEquals(toShort(expectedT), ppu2C02.getT());
    }

    @ParameterizedTest
    @CsvSource({
            "00000000, false, 00000000",
            "00000000, true,  00000000",
            "11111111, false, 01111111",
            "10101010, false, 00101010"
    })
    void testRegPpuStatus(String initialStatusString, boolean initialW, String expectedStatus) {
        final byte initialStatus = toByte(initialStatusString);

        PPU2C02 ppu2C02 = new PPU2C02(new PPU2C02Bus());
        ppu2C02.setW(initialW);
        ppu2C02.setRegPpuStatus(initialStatus);

        assertEquals(initialStatus, ppu2C02.readRegPpuStatus());
        assertEquals(toByte(expectedStatus), ppu2C02.getRegPpuStatus());
        assertFalse(ppu2C02.getW());
    }


    @ParameterizedTest
    @CsvSource(value = {
            "in. T,             input,    in. W, in. X,    ex. T,             ex. W, ex. X   ",
            "00000000_00000000, 00000000, false, 00000000, 00000000_00000000, true,  00000000",
            "11111111_11111111, 00000000, false, 11111111, 11111111_11100000, true,  11111000",
            "00000000_00000000, 11111111, false, 00000000, 00000000_00011111, true,  00000111",
            "01010101_01010101, 01010101, false, 01010101, 01010101_01001010, true,  01010101",
            "00000000_00000000, 00000000, true,  00000000, 00000000_00000000, false, 00000000",
            "11111111_11111111, 11111111, true,  11111111, 11111111_11111111, false, 11111111",
            "00000000_00000000, 11111111, true,  00000000, 01110011_11100000, false, 00000000",
            "01010101_01010101, 01010101, true,  01010101, 01010101_01010101, false, 01010101"
    }, useHeadersInDisplayName = true)
    void testRegPpuScroll(String initialT, String input, boolean initialW, String initialX, String expectedT, boolean expectedW, String expectedX) {
        PPU2C02 ppu2C02 = new PPU2C02(new PPU2C02Bus());
        ppu2C02.setT(toShort(initialT));
        ppu2C02.setW(initialW);
        ppu2C02.setX(toByte(initialX));

        ppu2C02.writeRegPpuScroll(toByte(input));

        assertEquals(toShort(expectedT), ppu2C02.getT());
        assertEquals(expectedW, ppu2C02.getW());
        assertEquals(toByte(expectedX), ppu2C02.getX());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "in. T,             input,    in. W, ex. T,             ex. W",
            "00000000_00000000, 00000000, false, 00000000_00000000, true ",
            "11111111_11111111, 00000000, false, 10000000_11111111, true ",
            "00000000_00000000, 11111111, false, 00111111_00000000, true ",
            "01010101_01010101, 01010101, false, 00010101_01010101, true ",
            "00000000_00000000, 00000000, true,  00000000_00000000, false",
            "11111111_11111111, 00000000, true,  11111111_00000000, false",
            "00000000_00000000, 11111111, true,  00000000_11111111, false",
            "01010101_01010101, 01010101, true,  01010101_01010101, false",
    }, useHeadersInDisplayName = true)
    void testRegPpuAddr(String initialT, String input, boolean initialW, String expectedT, boolean expectedW) {
        PPU2C02 ppu2C02 = new PPU2C02(new PPU2C02Bus());
        ppu2C02.setT(toShort(initialT));
        ppu2C02.setW(initialW);

        ppu2C02.writeRegPpuAddr(toByte(input));

        final short tShort = toShort(expectedT);
        assertEquals(tShort, ppu2C02.getT());
        assertEquals(expectedW, ppu2C02.getW());
        if (initialW) {
            assertEquals(tShort,ppu2C02.getV());
        };
    }

    short toShort(String value) {
        return (short) toInt(value);
    }

    byte toByte(String value) {
        return (byte) toInt(value);
    }

    int toInt(String value) {
        return Integer.parseInt(value.replaceAll("_", ""), 2);
    }
}
