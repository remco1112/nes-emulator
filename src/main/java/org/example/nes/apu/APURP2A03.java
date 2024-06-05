package org.example.nes.apu;

import java.util.Set;

public class APURP2A03 {
    private final FrameCounter frameCounter = new FrameCounter();
    private final Pulse1Channel pulse1Channel = new Pulse1Channel();
    private final Pulse2Channel pulse2Channel = new Pulse2Channel();
    private final Mixer mixer = new Mixer();

    public void writeSQ1Vol(byte value) {
        pulse1Channel.writeVolumeRegister(value);
    }

    public void writeSQ1Sweep(byte value) {
        pulse1Channel.writeSweepRegister(value);
    }

    public void writeSQ1Low(byte value) {
        pulse1Channel.writeTimerLowRegister(value);
    }

    public void writeSQ1High(byte value) {
        pulse1Channel.writeLengthCounterRegister(value);
    }

    public void writeSQ2Vol(byte value) {
        pulse2Channel.writeVolumeRegister(value);
    }

    public void writeSQ2Sweep(byte value) {
        pulse2Channel.writeSweepRegister(value);
    }

    public void writeSQ2Low(byte value) {
        pulse2Channel.writeTimerLowRegister(value);
    }

    public void writeSQ2High(byte value) {
        pulse2Channel.writeLengthCounterRegister(value);
    }

    public void writeTRILinear(byte value) {

    }

    public void writeTRILow(byte value) {

    }

    public void writeTRIHigh(byte value) {

    }

    public void writeNoiseVol(byte value) {

    }

    public void writeNoiseLow(byte value) {

    }

    public void writeNoiseHigh(byte value) {

    }

    public void writeDMCFreq(byte value) {

    }

    public void writeDMCRaw(byte value) {

    }

    public void writeDMCStart(byte value) {

    }

    public void writeDMCLen(byte value) {

    }

    public void writeStatus(byte value) {
        pulse1Channel.setEnabled((value & 0x1) != 0);
        pulse2Channel.setEnabled((value & 0x2) != 0);
    }

    public byte readStatus() {
        return 0;
    }

    public void writeFrameCounter(byte value) {
        final boolean stepMode5 = (value & 0x80) != 0;
        final boolean irqEnabled = (value & 0x40) == 0;

        frameCounter.configure(irqEnabled, stepMode5);
    }

    public short tick() {
        final Set<FrameCounter.ClockResult> frameCounterClockResults = frameCounter.tick();

        final int pulse1 = pulse1Channel.tick(frameCounterClockResults);
        final int pulse2 = pulse2Channel.tick(frameCounterClockResults);

        return mixer.mix(pulse1, pulse2, 0, 0, 0);
    }
}
