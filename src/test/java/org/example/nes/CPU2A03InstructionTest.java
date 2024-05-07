package org.example.nes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CPU2A03InstructionTest {

    private static final String TEST_CASE_TEMP_DIR = "nes6502";
    private static final String TEST_CASE_BASE_URL = "https://raw.githubusercontent.com/TomHarte/ProcessorTests/main/nes6502/v1/";

    static void runTest(TestCase testCase) {
        final CPU2A03 cpu = new CPU2A03(
                new RecordingMemoryMap(testCase.initialState.ramAsByteArray()),
                testCase.initialState.pc,
                testCase.initialState.sp,
                testCase.initialState.a,
                testCase.initialState.x,
                testCase.initialState.y,
                testCase.initialState.p,
                new NoopInterruptController()
        );

        final int cycles = cpu.tickUntilNextOp();

        assertEquals(testCase.finalState.pc, cpu.getRegPC());
        assertEquals(testCase.finalState.sp, cpu.getRegSP());
        assertEquals(testCase.finalState.a, cpu.getRegA());
        assertEquals(testCase.finalState.x, cpu.getRegX());
        assertEquals(testCase.finalState.y, cpu.getRegY());
        assertEquals(testCase.finalState.p, cpu.getRegP());

        final List<Cycle> memoryMapLog = ((RecordingMemoryMap) cpu.getMemoryMap()).getLog();
        assertEquals(cycles, memoryMapLog.size(), "Mismatch between number of CPU cycles and memory access count");
        assertEquals(testCase.cycles, ((RecordingMemoryMap) cpu.getMemoryMap()).getLog());
    }

    static OpCodeTest[] prepareTests(String[] args) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Path testCaseDir = Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir"), TEST_CASE_TEMP_DIR));
        final OpCodeTest[] opCodeTests = new OpCodeTest[OpCode.values().length];
        for (OpCode opCode : OpCode.values()) {
            if (args.length > 0 && !Arrays.asList(args).contains(opCode.name()) && !Arrays.asList(args).contains(opCode.operation.name())) {
                opCodeTests[opCode.ordinal()] = new OpCodeTest(opCode, Collections.emptyList());
                continue;
            }
            print("Preparing", opCode.ordinal(), OpCode.values().length, opCode.name());
            final int opCodeUint = Byte.toUnsignedInt(opCode.opCode);
            final String fileName = (opCodeUint < 0x10 ? "0" : "") + Integer.toUnsignedString(opCodeUint, 16) + ".json";
            final Path filePath = testCaseDir.resolve(fileName);
            if (!Files.exists(filePath)) {
                try (InputStream in = URI.create(TEST_CASE_BASE_URL).resolve(fileName).toURL().openStream()) {
                    Files.copy(in, filePath);
                }
            }
            final List<TestCase> testCasesForOpCode = objectMapper.readValue(filePath.toFile(), new TypeReference<>() {});
            opCodeTests[opCode.ordinal()] = new OpCodeTest(opCode, testCasesForOpCode);
        }
        return opCodeTests;
    }

    private record OpCodeTest(OpCode opCode, List<TestCase> testCases) { }

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

    public static void main(String[] args) throws IOException {
        printNow("Preparing", 0, 0, "");
        OpCodeTest[] tests = prepareTests(args);
        final int total = Arrays.stream(tests).mapToInt(opCodeTest -> opCodeTest.testCases.size()).sum();
        int current = 0;
        for (OpCodeTest test : tests) {
            for (TestCase testCase : test.testCases) {
                print("Evaluating", current, total, test.opCode.name());
                try {
                    runTest(testCase);
                } catch (AssertionFailedError e) {
                    print("Test failed", current, total, test.opCode.name());
                    System.out.println();
                    System.out.println("Failed test: " + testCase.name);
                    e.printStackTrace(System.out);
                    System.exit(1);
                }
                current++;
            }
        }
        print("Completed successfully", current, total, "");
    }

    private static void print(String phase, int current, int total, String currentOperation) {
        clear();
        printNow(phase, current, total, currentOperation);
    }

    private static void clear() {
        System.out.print("\033[3F");
    }

    private static void printNow(String phase, int current, int total, String currentOperation) {
        final StringBuilder stringBuilder = new StringBuilder(100);
        stringBuilder.append("Status: ");
        stringBuilder.append(phase);
        stringBuilder.append('\n');
        stringBuilder.append("Current operation: ");
        stringBuilder.append(currentOperation);
        stringBuilder.append('\n');
        final int percentage = (int) (((double) current / (double) total) * 100.0);
        stringBuilder.append(percentage);
        stringBuilder.append("% ");
        final int percentChars = percentage / 2;
        stringBuilder.append("█".repeat(percentChars));
        stringBuilder.append("░".repeat(50 - percentChars));
        stringBuilder.append(" (");
        stringBuilder.append(current);
        stringBuilder.append('/');
        stringBuilder.append(total);
        stringBuilder.append(')');
        System.out.println(stringBuilder);
    }
}
