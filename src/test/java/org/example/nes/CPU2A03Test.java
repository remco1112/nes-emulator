package org.example.nes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CPU2A03Test {

    private static final String TEST_CASE_TEMP_DIR = "nes6502";
    private static final String TEST_CASE_BASE_URL = "https://raw.githubusercontent.com/TomHarte/ProcessorTests/main/nes6502/v1/";

    @ParameterizedTest
    @MethodSource
    void test(TestCase testCase) {
        final CPU2A03 cpu = new CPU2A03(
                new RecordingMemoryMap(testCase.initialState.ramAsByteArray()),
                testCase.initialState.pc,
                testCase.initialState.sp,
                testCase.initialState.a,
                testCase.initialState.x,
                testCase.initialState.y,
                testCase.initialState.p
        );

        final int cycles = cpu.tickUntilNextOp();

        assertEquals(testCase.finalState.pc, cpu.getRegPC());
        assertEquals(testCase.finalState.sp, cpu.getRegSP());
        assertEquals(testCase.finalState.a, cpu.getRegA());
        assertEquals(testCase.finalState.x, cpu.getRegX());
        assertEquals(testCase.finalState.y, cpu.getRegY());
        assertEquals(testCase.finalState.p, cpu.getRegP());
        assertArrayEquals(testCase.finalState.ramAsByteArray(), cpu.getMemoryMap().asByteArray());

        final List<Cycle> memoryMapLog = ((RecordingMemoryMap) cpu.getMemoryMap()).getLog();
        assertEquals(cycles, memoryMapLog.size(), "Mismatch between number of CPU cycles and memory access count");
        assertEquals(testCase.cycles, ((RecordingMemoryMap) cpu.getMemoryMap()).getLog());
    }

    static List<TestCase> test() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        List<TestCase> testCases = new ArrayList<>(OpCode.values().length * 10000);
        final Path testCaseDir = Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir"), TEST_CASE_TEMP_DIR));
        for (OpCode opCode : OpCode.values()) {
            final String fileName = Integer.toUnsignedString(Byte.toUnsignedInt(opCode.opCode), 16) + ".json";
            final Path filePath = testCaseDir.resolve(fileName);
            if (!Files.exists(filePath)) {
                try (InputStream in = URI.create(TEST_CASE_BASE_URL).resolve(fileName).toURL().openStream()) {
                    System.out.println("Downloading: " + fileName);
                    Files.copy(in, filePath);
                }
            }
            final List<TestCase> testCasesForOpCode = objectMapper.readValue(filePath.toFile(), new TypeReference<>() {});
            testCases.addAll(testCasesForOpCode);
        }
        return testCases;
    }

    private static class TestCase {
        @JsonProperty("name")
        private String name;

        @JsonProperty("initial")
        private State initialState;

        @JsonProperty("final")
        private State finalState;

        @JsonDeserialize(contentUsing = CycleDeserializer.class)
        private List<Cycle> cycles;

        private static class State {
            @JsonProperty("pc")
            @JsonDeserialize(using = ShortDeserializer.class)
            private short pc;

            @JsonProperty("s")
            @JsonDeserialize(using = ByteDeserializer.class)
            private byte sp;

            @JsonProperty("a")
            @JsonDeserialize(using = ByteDeserializer.class)
            private byte a;

            @JsonProperty("x")
            @JsonDeserialize(using = ByteDeserializer.class)
            private byte x;

            @JsonProperty("y")
            @JsonDeserialize(using = ByteDeserializer.class)
            private byte y;

            @JsonProperty("p")
            @JsonDeserialize(using = ByteDeserializer.class)
            private byte p;

            @JsonDeserialize(using = RamDeserializer.class)
            private Map<Integer, Byte> ram;

            private byte[] ramAsByteArray() {
                byte[] ramBytes = new byte[0x10000];
                for (var entry : ram.entrySet()) {
                    ramBytes[entry.getKey()] = entry.getValue();
                }
                return ramBytes;
            }
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
