package org.example.nes.apu;

 abstract class WaveGenerator {

    protected abstract void tick();

    protected abstract int getCurrentValue();

    protected void reset() {

    }
 }
