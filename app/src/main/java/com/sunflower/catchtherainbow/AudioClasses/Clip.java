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

    public boolean setSamples(ByteBuffer buffer, int start, int len, AudioInfo format)
    {
        return sequence.set(buffer, start, len, format);
    }

    public int getSamples(ByteBuffer buffer, int start, int len)
    {
        return sequence.get(buffer, start, len);
    }

    public boolean append(ByteBuffer buffer, long len) throws IOException
    {
        return sequence.append(buffer, len);
    }

    // wave data
    public boolean getWaveData(long startSample, long len, long samplesPerFrame, WaveData waveData)
    {
        return sequence.getWaveData(startSample, len, samplesPerFrame, waveData);
    }

    boolean getMinMax(double startTime, double endTime, Float outMin, Float outMax)
    {
        outMin = 0.0f;
        outMax = 0.0f;

        if (startTime > endTime)
            return false;

        if (startTime == endTime)
            return true;

        Integer s0 = 0, s1 = 0;

        timeToSamplesClip(startTime, s0);
        timeToSamplesClip(endTime, s1);

        return sequence.getMinMax(s0, s1-s0, outMin, outMax);
    }

    void timeToSamplesClip(double t0, Integer s0)
    {
        if (t0 < offset)
            s0 = 0;
        else if (t0 > offset + sequence.samplesCount/info.sampleRate)
            s0 = sequence.samplesCount;
        else
            s0 = (int) Math.floor(((t0 - offset) * info.sampleRate) + 0.5);
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

    public  int getMinSamples()
    {
        return sequence.getMinSamples();
    }

    public int getMaxSamples()
    {
        return sequence.getMaxSamples();
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
