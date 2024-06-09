package org.example.nes.apu;

class WaveChannel<W extends WaveGenerator> extends Channel {
    protected final W waveGenerator;
    private final Timer timer;
    private final boolean tickWaveGeneratorOnCpu;
    private final LengthCounter lengthCounter = new LengthCounter();

    WaveChannel(Timer timer, W waveGenerator, boolean tickWaveGeneratorOnCpu) {
        this.waveGenerator = waveGenerator;
        this.timer = timer;
        this.tickWaveGeneratorOnCpu = tickWaveGeneratorOnCpu;
    }

    @Override
    void onApuTick() {
        super.onApuTick();
        if (!tickWaveGeneratorOnCpu) tick();
    }

    @Override
    void onCpuTick() {
        super.onCpuTick();
        if(tickWaveGeneratorOnCpu) tick();
    }

    @Override
    boolean isSilenced() {
        return super.isSilenced() || lengthCounter.isZero() || !timer.isActive();
    }

    protected int getTimerPeriod() {
        return timer.getPeriod();
    }

    protected void setTimerPeriod(int period) {
        timer.setPeriod(period);
    }

    private void tick() {
        if (timer.tick() && shouldTickWaveGenerator()) waveGenerator.tick();
    }

    protected boolean shouldTickWaveGenerator() {
        return true;
    }

    @Override
    void writeRegister0(byte value) {
        super.writeRegister0(value);
        if (waveGenerator instanceof EnvelopeWaveGenerator) {
            ((EnvelopeWaveGenerator) waveGenerator).configureVolume(value);
            setLengthCounterHalted((value & 0x20) != 0);
        }
    }

    @Override
    void writeRegister3(byte value) {
        super.writeRegister3(value);
        lengthCounter.set((value & 0xF8) >>> 3);
        waveGenerator.reset();
    }

    void setEnabled(boolean enabled) {
        lengthCounter.setEnabled(enabled);
    }

    void setLengthCounterHalted(boolean halted) {
        lengthCounter.setHalted(halted);
    }

    @Override
    void onHalfFrameTick() {
        super.onHalfFrameTick();
        lengthCounter.tick();
    }

    @Override
    void onQuarterFrameTick() {
        super.onQuarterFrameTick();
        if (waveGenerator instanceof EnvelopeWaveGenerator) {
            ((EnvelopeWaveGenerator) waveGenerator).tickEnvelope();
        }
    }

    @Override
    final int getVolume() {
        return waveGenerator.getCurrentValue();
    }
}
