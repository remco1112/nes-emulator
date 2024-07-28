package org.example.nes.sound;

import org.example.nes.utils.UInt;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class NativeDownsamplingJavaSoundSampleConsumer implements SoundSampleConsumer {
    private static final float CPU_CLOCK_SPEED = 1.789773f * 1_000_000f;
    private static final int AUDIO_BUFFER_SIZE = Short.SIZE * 3000;
    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(CPU_CLOCK_SPEED, Short.SIZE, 1, true, false);

    private final byte[] sampleByteArray = new byte[Short.BYTES];
    private final SourceDataLine sourceDataLine;

    {

        try {
            sourceDataLine = AudioSystem.getSourceDataLine(AUDIO_FORMAT);
            sourceDataLine.open(AUDIO_FORMAT, AUDIO_BUFFER_SIZE);
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
