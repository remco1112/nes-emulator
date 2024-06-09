package org.example.nes.apu;

class TimerWaveChannel<W extends WaveGenerator> extends WaveChannel<W> {
    private final Timer timer;
    private final boolean tickWaveGeneratorOnCpu;

    TimerWaveChannel(Timer timer, W waveGenerator, boolean tickWaveGeneratorOnCpu) {
        super(waveGenerator);
        this.timer = timer;
        this.tickWaveGeneratorOnCpu = tickWaveGeneratorOnCpu;
    }

    @Override
    void onApuTick() {
        if (!tickWaveGeneratorOnCpu) tick();
    }

    @Override
    void onCpuTick() {
        if(tickWaveGeneratorOnCpu) tick();
    }

    @Override
    boolean isSilenced() {
        return super.isSilenced() || !timer.isActive();
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
}
