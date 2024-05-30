package org.example.nes.utils;

public final class UInt {

    private UInt() {
    }

    public static int toUint(byte byteValue) {
        return Byte.toUnsignedInt(byteValue);
    }

    public static int toUint(short shortValue) {
        return Short.toUnsignedInt(shortValue);
    }
}
