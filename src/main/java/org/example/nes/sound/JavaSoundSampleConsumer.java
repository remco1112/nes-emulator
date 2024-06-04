package org.example.nes.sound;

import org.example.nes.utils.UInt;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class JavaSoundSampleConsumer implements SoundSampleConsumer {
    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(893400f, Short.SIZE, 1, true, false);
    private final byte[] sampleByteArray = new byte[Short.BYTES];

    private final SourceDataLine sourceDataLine;

    {

        try {
            sourceDataLine = AudioSystem.getSourceDataLine(AUDIO_FORMAT);
            sourceDataLine.open(AUDIO_FORMAT, Short.SIZE * 10000);
            sourceDataLine.start();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onSample(short sample) {
        for (int i = 0; i < sampleByteArray.length; i++) {
            sampleByteArray[i] = (byte) ((UInt.toUint(sample) >>> (8 * i)) & 0xFF);
        }
        sourceDataLine.write(sampleByteArray, 0, sampleByteArray.length);
    }
}
