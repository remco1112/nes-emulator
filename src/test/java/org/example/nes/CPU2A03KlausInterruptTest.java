package org.example.nes;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CPU2A03KlausInterruptTest {

    @Test
    void test() throws IOException {
        final byte[] ram;
        try (var is = getClass().getResourceAsStream("6502_interrupt_test.bin")) {
            ram = is.readAllBytes();
        }
        final MemoryMap memoryMap = new RAMMemoryMap(ram);
        short previousRegPC;
        short regPC = 0x400;
        final CPU2A03 cpu2A03 = new CPU2A03(memoryMap, regPC, new KlausInterruptController(memoryMap));
        do {
            cpu2A03.tickUntilNextOp();
            previousRegPC = regPC;
            regPC = cpu2A03.getRegPC();
        } while (previousRegPC != regPC);

        assertEquals((short) 0x6e5, regPC);
    }
}
