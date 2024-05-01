package org.example.nes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CPU2A03Test {

    @ParameterizedTest
    @MethodSource
    void test(TestCase testCase) {
        final CPU2A03 cpu = new CPU2A03(
                new RecordingMemoryMap(testCase.initialState.ram),
                testCase.initialState.pc,
                testCase.initialState.sp,
                testCase.initialState.a,
                testCase.initialState.x,
                testCase.initialState.y,
                testCase.initialState.p
        );

        cpu.tickUntilNextOp();

        assertEquals(testCase.finalState.pc, cpu.getRegPC());
        assertEquals(testCase.finalState.sp, cpu.getRegSP());
        assertEquals(testCase.finalState.a, cpu.getRegA());
        assertEquals(testCase.finalState.x, cpu.getRegX());
        assertEquals(testCase.finalState.y, cpu.getRegY());
        assertEquals(testCase.finalState.p, cpu.getRegP());
        assertArrayEquals(testCase.finalState.ram, cpu.getMemoryMap().asByteArray());
        assertEquals(testCase.cycles, ((RecordingMemoryMap) cpu.getMemoryMap()).getLog());
    }

    static List<TestCase> test() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(URI.create("https://raw.githubusercontent.com/TomHarte/ProcessorTests/main/nes6502/v1/c0.json").toURL(), new TypeReference<>() {
        });
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
            private byte[] ram;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
