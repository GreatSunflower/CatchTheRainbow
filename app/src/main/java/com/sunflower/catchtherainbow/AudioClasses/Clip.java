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

    public int getStartSample()
    {
        return (int)Math.floor(offset * info.sampleRate + 0.5);
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
