package org.example.nes.apu;

import org.example.nes.sequencer.SequenceEventCollection;
import org.example.nes.sequencer.Sequencer;

import java.util.*;

public class FrameCounter {
    private static final SequenceEventCollection<FrameCounterSequencerEvents> FOUR_STEP_SEQUENCE;
    private static final SequenceEventCollection<FrameCounterSequencerEvents> FIVE_STEP_SEQUENCE;
    private static final Set<ClockResult> QUARTER = Collections.unmodifiableSet(EnumSet.of(ClockResult.QUARTER));
    private static final Set<ClockResult> QUARTER_HALF = Collections.unmodifiableSet(EnumSet.of(ClockResult.QUARTER, ClockResult.HALF));
    private static final Set<ClockResult> APU = Collections.unmodifiableSet(EnumSet.of(ClockResult.APU));
    private static final Set<ClockResult> NONE = Collections.unmodifiableSet(EnumSet.noneOf(ClockResult.class));

    static {
        final Set<FrameCounterSequencerEvents> quarter = Collections.unmodifiableSet(EnumSet.of(FrameCounterSequencerEvents.QUARTER));
        final Set<FrameCounterSequencerEvents> half = Collections.unmodifiableSet(EnumSet.of(FrameCounterSequencerEvents.HALF));
        final Set<FrameCounterSequencerEvents> apu = Collections.unmodifiableSet(EnumSet.of(FrameCounterSequencerEvents.APU));
        final Set<FrameCounterSequencerEvents> interrupt = Collections.unmodifiableSet(EnumSet.of(FrameCounterSequencerEvents.APU, FrameCounterSequencerEvents.INTERRUPT));
        final Set<FrameCounterSequencerEvents> interruptHalf = Collections.unmodifiableSet(EnumSet.of(FrameCounterSequencerEvents.HALF, FrameCounterSequencerEvents.INTERRUPT));
        final Set<FrameCounterSequencerEvents> none = Collections.emptySet();


        final List<Set<FrameCounterSequencerEvents>> fourStepSequence = new ArrayList<>(14915 * 2);
        final List<Set<FrameCounterSequencerEvents>> fiveStepSequence = new ArrayList<>(18641 * 2);

        for (int i = 0; i < 14915 * 2; i++) {
            final Set<FrameCounterSequencerEvents> frameCounterSequencerEvents = i % 2 == 0 ? apu : none;
            fourStepSequence.add(frameCounterSequencerEvents);
            fiveStepSequence.add(frameCounterSequencerEvents);
        }

        for (int i = 14915 * 2; i < 18641 * 2; i++) {
            final Set<FrameCounterSequencerEvents> frameCounterSequencerEvents = i % 2 == 0 ? apu : none;
            fiveStepSequence.add(frameCounterSequencerEvents);
        }

        fourStepSequence.set(0, interrupt);
        fourStepSequence.set(3728 * 2 + 1, quarter);
        fourStepSequence.set(7456 * 2 + 1, half);
        fourStepSequence.set(11185 * 2 + 1, quarter);
        fourStepSequence.set(14914 * 2, interrupt);
        fourStepSequence.set(14914 * 2 + 1, interruptHalf);

        fiveStepSequence.set(3728 * 2 + 1, quarter);
        fiveStepSequence.set(7456 * 2 + 1, half);
        fiveStepSequence.set(11185 * 2 + 1, quarter);
        fiveStepSequence.set(18640 * 2 + 1, half);

        FOUR_STEP_SEQUENCE = SequenceEventCollection.ofList(fourStepSequence);
        FIVE_STEP_SEQUENCE = SequenceEventCollection.ofList(fiveStepSequence);
    }

    private final Map<FrameCounterSequencerEvents, Runnable> eventHandlingMap = new EnumMap<>(Map.of(
            FrameCounterSequencerEvents.HALF, this::handleHalf,
            FrameCounterSequencerEvents.QUARTER, this::handleQuarter,
            FrameCounterSequencerEvents.INTERRUPT, this::handleInterrupt,
            FrameCounterSequencerEvents.APU, this::handleApu

    ));

    private final Sequencer<FrameCounterSequencerEvents> fourStepSequencer = new Sequencer<>(eventHandlingMap, FOUR_STEP_SEQUENCE);
    private final Sequencer<FrameCounterSequencerEvents> fiveStepSequencer = new Sequencer<>(eventHandlingMap, FIVE_STEP_SEQUENCE);

    private Sequencer<FrameCounterSequencerEvents> activeSequencer = fourStepSequencer;

    private boolean irqEnabled;
    private boolean stepMode5;
    private boolean interrupt;
    private int resetCountDown;

    private Set<ClockResult> result = NONE;

    void configure(boolean irqEnabled, boolean stepMode5) {
        this.irqEnabled = irqEnabled;
        this.stepMode5 = stepMode5;
        if (!irqEnabled) {
            interrupt = false;
        }
        resetCountDown = activeSequencer.getSequenceIndex() % 2 == 0 ? 3 : 4;
    }

    Set<ClockResult> tick() {
        if (resetCountDown > 0 && --resetCountDown == 0) {
                return handleReset();
        }
        result = NONE;
        activeSequencer.tick();
        return result;
    }

    private Set<ClockResult> handleReset() {
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

    enum ClockResult {
        QUARTER,
        HALF,
        APU
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
        INTERRUPT,
        APU
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

    private void handleApu() {
        result = APU;
    }
}
