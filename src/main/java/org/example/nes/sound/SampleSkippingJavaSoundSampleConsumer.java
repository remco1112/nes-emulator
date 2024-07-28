package org.example.nes.sound;

import org.example.nes.utils.UInt;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SampleSkippingJavaSoundSampleConsumer implements SoundSampleConsumer {
    private static final int CPU_CLOCK_SPEED = 1_789_773;
    private static final int TARGET_SPEED = 48000;
    private static final double TARGET_CLOCK_PER_CPU_CLOCK = (double)  TARGET_SPEED / CPU_CLOCK_SPEED;

    private static final int AUDIO_BUFFER_SIZE = Short.SIZE * 100;
    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(TARGET_SPEED, Short.SIZE, 1, true, false);

    private final byte[] sampleByteArray = new byte[Short.BYTES];
    private final SourceDataLine sourceDataLine;

    private double averageClockPerTarget = 0;
    private int sampleCount = 0;

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
        int sampled = 0;
        if (averageClockPerTarget <= TARGET_CLOCK_PER_CPU_CLOCK) {
            for (int i = 0; i < sampleByteArray.length; i++) {
                sampleByteArray[i] = (byte) ((UInt.toUint(sample) >>> (8 * i)) & 0xFF);
            }
            sourceDataLine.write(sampleByteArray, 0, sampleByteArray.length);
            sampled = 1;
        }
        averageClockPerTarget = ((averageClockPerTarget * sampleCount) + sampled) / (++sampleCount);
        if (sampleCount == Integer.MAX_VALUE) {
            sampleCount = 0;
            averageClockPerTarget = 0;
        }
    }
}
