package com.sunflower.catchtherainbow.AudioClasses;

import java.io.Serializable;

/**
 * Created by SuperComputer on 3/21/2017.
 * Manages audio chunks.
 */


// passed from track
public class AudioInfo implements Serializable
{
    public AudioInfo(int sampleRate, int channels)
    {
        this.sampleRate = sampleRate;
        this.channels = channels;
    }

    protected int sampleRate = 44100;
    protected int channels = 2;
    protected SampleFormat format = SampleFormat.float_32Bit;

    public int getSampleRate()
    {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate)
    {
        this.sampleRate = sampleRate;
    }

    public int getChannels()
    {
        return channels;
    }

    public void setChannels(int channels)
    {
        this.channels = channels;
    }

    public SampleFormat getFormat()
    {
        return format;
    }

    public void setFormat(SampleFormat format)
    {
        this.format = format;
    }
}