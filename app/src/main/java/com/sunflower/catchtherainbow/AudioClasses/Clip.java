package com.sunflower.catchtherainbow.AudioClasses;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Created by SuperComputer on 3/16/2017.
 * Manages audio chunks.
 */

public class Clip implements Serializable
{
    // time offset in seconds
    private double offset = 0;
    private AudioSequence sequence;
    private AudioInfo info = new AudioInfo(44100, 2);
    private FileManager fileManager;

    public Clip(FileManager manager, AudioInfo info)
    {
        this.fileManager = manager;
        this.info = info;
        this.sequence = new AudioSequence(fileManager, info);
    }

    int getSamples(ByteBuffer buffer, int start, int len)
    {
        return sequence.get(buffer, start, len);
    }

    public boolean append(ByteBuffer buffer, long len) throws IOException
    {
        return sequence.append(buffer, len);
    }

    double getStartTime()
    {
        // offset is the minimum value and it is returned; no clipping to 0
        return offset;
    }

    double getEndTime()
    {
        int numSamples = sequence.samplesCount;

        double maxLen = offset + (numSamples)/info.sampleRate/info.channels;
        // calculated value is not the length;
        // it is a maximum value and can be negative; no clipping to 0

        return maxLen;
    }

    public int getStartSample()
    {
        return (int)Math.floor(offset * info.sampleRate * info.channels + 0.5);
    }

    public int getEndSample()
    {
        return getStartSample() + sequence.samplesCount;
    }

    public int getNumSamples()
    {
        return sequence.samplesCount;
    }


    public double getOffset()
    {
        return offset;
    }

    public AudioSequence getSequence()
    {
        return sequence;
    }

    public AudioInfo getInfo()
    {
        return info;
    }
}
