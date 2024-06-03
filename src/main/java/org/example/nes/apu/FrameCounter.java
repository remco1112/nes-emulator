package org.example.nes.apu;

import org.example.nes.sequencer.SequenceEventCollection;
import org.example.nes.sequencer.Sequencer;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class FrameCounter {
    private static final SequenceEventCollection<FrameCounterSequencerEvents> FOUR_STEP_SEQUENCE;
    private static final SequenceEventCollection<FrameCounterSequencerEvents> FIVE_STEP_SEQUENCE;
    private static final Set<FrameClockResult> QUARTER = Collections.unmodifiableSet(EnumSet.of(FrameClockResult.QUARTER));
    private static final Set<FrameClockResult> QUARTER_HALF = Collections.unmodifiableSet(EnumSet.of(FrameClockResult.QUARTER, FrameClockResult.HALF));
    private static final Set<FrameClockResult> NONE = Collections.unmodifiableSet(EnumSet.noneOf(FrameClockResult.class));

    static {
        final Set<FrameCounterSequencerEvents> quarter = Collections.unmodifiableSet(EnumSet.of(FrameCounterSequencerEvents.QUARTER));
        final Set<FrameCounterSequencerEvents> half = Collections.unmodifiableSet(EnumSet.of(FrameCounterSequencerEvents.HALF));
        final Set<FrameCounterSequencerEvents> interrupt = Collections.unmodifiableSet(EnumSet.of(FrameCounterSequencerEvents.INTERRUPT));
        final Set<FrameCounterSequencerEvents> interruptHalf = Collections.unmodifiableSet(EnumSet.of(FrameCounterSequencerEvents.HALF, FrameCounterSequencerEvents.INTERRUPT));

        final Map<Integer, Set<FrameCounterSequencerEvents>> commonSequence = Map.of(
                3728 * 2 + 1, quarter,
                7456 * 2 + 1, half,
                11185 * 2 + 1, quarter
        );

        final Map<Integer, Set<FrameCounterSequencerEvents>> fourStepSequenceEnd = Map.of(
                0, interrupt,
                14914 * 2, interrupt,
                14914 * 2 + 1, interruptHalf
        );

        final Map<Integer, Set<FrameCounterSequencerEvents>> fiveStepSequenceEnd = Map.of(
                18640 * 2 + 1, half
        );

        FOUR_STEP_SEQUENCE = SequenceEventCollection.ofMap(
                Stream.of(commonSequence, fourStepSequenceEnd)
                        .flatMap(m -> m.entrySet().stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        FIVE_STEP_SEQUENCE = SequenceEventCollection.ofMap(
                Stream.of(commonSequence, fiveStepSequenceEnd)
                        .flatMap(m -> m.entrySet().stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    private final Map<FrameCounterSequencerEvents, Runnable> eventHandlingMap = new EnumMap<>(Map.of(
            FrameCounterSequencerEvents.HALF, this::handleHalf,
            FrameCounterSequencerEvents.QUARTER, this::handleQuarter,
            FrameCounterSequencerEvents.INTERRUPT, this::handleInterrupt

    ));

    private final Sequencer<FrameCounterSequencerEvents> fourStepSequencer = new Sequencer<>(eventHandlingMap, FOUR_STEP_SEQUENCE);
    private final Sequencer<FrameCounterSequencerEvents> fiveStepSequencer = new Sequencer<>(eventHandlingMap, FIVE_STEP_SEQUENCE);

    private Sequencer<FrameCounterSequencerEvents> activeSequencer = fourStepSequencer;

    private boolean irqEnabled;
    private boolean stepMode5;
    private boolean interrupt;
    private int resetCountDown;

    private Set<FrameClockResult> result = NONE;

    void configure(boolean irqEnabled, boolean stepMode5) {
        this.irqEnabled = irqEnabled;
        this.stepMode5 = stepMode5;
        if (!irqEnabled) {
            interrupt = false;
        }
        resetCountDown = activeSequencer.getSequenceIndex() % 2 == 0 ? 3 : 4;
    }

    Set<FrameClockResult> tick() {
        if (resetCountDown > 0 && --resetCountDown == 0) {
                return handleReset();
        }
        result = NONE;
        activeSequencer.tick();
        return result;
    }

    private Set<FrameClockResult> handleReset() {
        fourStepSequencer.reset();
        fiveStepSequencer.reset();
        if (stepMode5) {
            activeSequencer = fiveStepSequencer;
            return QUARTER_HALF;
        } else {
            activeSequencer = fourStepSequencer;
            return NONE;
        }
    }

    enum FrameClockResult {
        QUARTER,
        HALF,
    }

    void clearInterrupt() {
        interrupt = false;
    }

    public boolean isInterrupt() {
        return interrupt;
    }

    private enum FrameCounterSequencerEvents {
        QUARTER,
        HALF,
        INTERRUPT
    }

    private void handleHalf() {
        result = QUARTER_HALF;
    }

    private void handleQuarter() {
        result = QUARTER;
    }

    private void handleInterrupt() {
        if (irqEnabled) {
            interrupt = true;
        }
    }
}
