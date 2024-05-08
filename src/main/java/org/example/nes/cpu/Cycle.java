package org.example.nes.cpu;

public record Cycle(boolean isRead, short address, byte value) {
}
