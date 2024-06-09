package org.example.nes.apu;

import static org.example.nes.utils.UInt.toUint;

class TimerWaveChannel<W extends WaveGenerator> extends WaveChannel<W> {
    private final Timer timer;
    private final boolean tickWaveGeneratorOnCpu;

    TimerWaveChannel(Timer timer, W waveGenerator, boolean tickWaveGeneratorOnCpu) {
        super(waveGenerator);
        this.timer = timer;
        this.tickWaveGeneratorOnCpu = tickWaveGeneratorOnCpu;
    }

    @Override
    public void writeRegister2(byte value) {
        timer.setPeriod((timer.getPeriod() & 0x700) | toUint(value));
    }

    @Override
    void writeRegister3(byte value) {
        super.writeRegister3(value);
        timer.setPeriod((timer.getPeriod() & 0xFF) | ((value & 0x7) << 8));
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
