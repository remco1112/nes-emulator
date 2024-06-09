package org.example.nes.apu;

import org.example.nes.sequencer.data.DataSequencer;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static org.example.nes.utils.UInt.toUint;

public class PulseWaveGenerator extends EnvelopeWaveGenerator {
    static final int SEQUENCE_LENGTH = 8;
    private static final List<List<Boolean>> SEQUENCERS_DATA = IntStream.of(0b01000000, 0b01100000, 0b01111000, 0b10011111)
            .mapToObj(PulseWaveGenerator::intToBooleanList)
            .toList();

    private final List<DataSequencer<Boolean>> sequencers = SEQUENCERS_DATA.stream()
            .map(DataSequencer::new)
            .toList();

    private DataSequencer<Boolean> activeSequencer = sequencers.getFirst();

    @Override
    public void configureVolume(byte value) {
        super.configureVolume(value);
        setDuty(toUint(value) >>> 6);
    }

    private void setDuty(int duty) {
        assert Objects.checkIndex(duty, sequencers.size()) == duty;
        activeSequencer = sequencers.get(duty);
    }

    @Override
    public void tick() {
        for (var sequencer : sequencers) sequencer.tick();
    }

    void reset() {
        resetEnvelope();
        for (var sequencer : sequencers) sequencer.reset();
    }

    @Override
    protected boolean isEnvelopeMuted() {
        return !activeSequencer.getValue();
    }

    private static List<Boolean> intToBooleanList(int value) {
        return IntStream.range(0, SEQUENCE_LENGTH)
                .mapToObj(i -> ((value >>> (SEQUENCE_LENGTH - i)) & 1) == 1)
                .toList();
    }
}
