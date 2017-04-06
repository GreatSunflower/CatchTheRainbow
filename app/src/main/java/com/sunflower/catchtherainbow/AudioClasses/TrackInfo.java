package com.sunflower.catchtherainbow.AudioClasses;

/**
 * Created by SuperComputer on 4/5/2017.
 */

// used to determine track samples during playback
public class TrackInfo
{
    protected WaveTrack track;

    protected Integer channel;

    protected long currentSample = 0;

    public TrackInfo(int channel, WaveTrack track, int currentSample)
    {
        this.channel = channel;
        this.track = track;
        this.currentSample = currentSample;
    }

    public void setCurrentSample(long currentSample)
    {
        this.currentSample = currentSample;
    }

    public int getChannel()
    {
        return channel;
    }

    public void setChannel(int channel)
    {
        this.channel = channel;
    }

    public long getCurrentSample()
    {
        return currentSample;
    }

    public WaveTrack getTrack()
    {
        return track;
    }
}