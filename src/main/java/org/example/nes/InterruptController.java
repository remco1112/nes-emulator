package org.example.nes;

public interface InterruptController {

    boolean isReset();

    boolean isIrq();

    boolean isNmi();
}
