package org.example.nes.utils;

import static org.example.nes.utils.UInt.toUint;

public class StringUtils {
    public static String addressToString(int address) {
        return Integer.toUnsignedString(address, 16);
    }

    public static String writeToString(int address, byte value) {
        return addressToString(address) + " <- " + Integer.toUnsignedString(toUint(value), 16);
    }
}
