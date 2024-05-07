package org.example.nes.mapper;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class INESLoader {
    private static final byte[] INES_MARK = "NES\u001a".getBytes(Charset.defaultCharset());
    private static final byte TRAINER_BITMASK = 0b0000_0100;

    public Mapper loadRom(InputStream stream) throws IOException {
        for (byte inesMark : INES_MARK) {
            if (readByte(stream) != inesMark) {
                throw new IOException("Unsupported file");
            }
        }
        int prgRomSize = Byte.toUnsignedInt(readByte(stream)) * 16384;
        int chrRomSize = Byte.toUnsignedInt(readByte(stream)) * 8192;
        byte flags6 = readByte(stream);
        byte flags7 = readByte(stream);
        byte mapper = (byte) (Byte.toUnsignedInt(flags6) >>> 4 | (Byte.toUnsignedInt(flags7) & 0xf0));
        skipBytes(8, stream);
        if ((flags6 & TRAINER_BITMASK) == TRAINER_BITMASK) {
            skipBytes(512, stream);
        }
        byte[] prgRom = readBytes(stream, prgRomSize);
        byte[] chrRom = readBytes(stream, chrRomSize);
        return switch (mapper) {
            case 0 -> new NROM(prgRom, chrRom);
            default -> throw new UnsupportedOperationException("Unrecognized mapper: " + mapper);
        };
    }

    private void skipBytes(int count, InputStream stream) throws IOException {
        for (int i = 0; i < count; i++) {
            readByte(stream);
        }
    }

    private byte readByte(InputStream stream) throws IOException {
        int nextByte = stream.read();
        if (nextByte == -1) {
            throw new EOFException();
        }
        return (byte) nextByte;
    }

    private byte[] readBytes(InputStream stream, int count) throws IOException {
        final byte[] bytes = new byte[count];
        for (int i = 0; i < count; i++) {
            bytes[i] = readByte(stream);
        }
        return bytes;
    }
}
