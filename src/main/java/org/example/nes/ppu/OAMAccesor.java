package org.example.nes.ppu;

interface OAMAccesor {

    byte readRegOamData();

    byte readRegOamAddr();

    void writeRegOamAddr(byte address);
}
