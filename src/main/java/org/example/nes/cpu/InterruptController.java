package org.example.nes.cpu;

public interface InterruptController {

    boolean isReset();

    boolean isIrq();

    boolean isNmi();
}
