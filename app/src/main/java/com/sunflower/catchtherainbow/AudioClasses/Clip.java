package com.sunflower.catchtherainbow.AudioClasses;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by SuperComputer on 3/16/2017.
 * Manages audio chunks.
 */

public class Clip implements Serializable
{
    // time offset in seconds
    protected double offset = 0;
    protected AudioSequence sequence;
    protected AudioInfo info = new AudioInfo(44100, 2);
    protected FileManager fileManager;

    protected boolean isPlaceholder = false;

    public Clip(FileManager manager, AudioInfo info)
    {
        this.fileManager = manager;
        this.info = info;
        this.sequence = new AudioSequence(fileManager, info);
    }

    public Clip(Clip other, FileManager manager)
    {
        this.fileManager = manager;
        this.info = other.info;
        this.sequence = new AudioSequence(manager, other.getSequence());
    }

    public boolean setSamples(ByteBuffer buffer, long start, long len, AudioInfo format)
    {
        return sequence.set(buffer, start, len, format);
    }

    public int getSamples(ByteBuffer buffer, long start, int len)
    {
        return sequence.get(buffer, start, len);
    }

    public boolean append(ByteBuffer buffer, long len) throws IOException
    {
        return sequence.append(buffer, len);
    }

    boolean insertSilence(double t, double len)
    {
        long s0 = timeToSamplesClip(t);
        long slen = (long) Math.floor(len * info.sampleRate + 0.5);

        if (sequence.insertSilence(s0, (int) slen))
        {
            return false;
        }

        return true;
    }

    public boolean createFromCopy(double t0, double t1, final Clip other)
    {
        long s0, s1;

        s0 = other.timeToSamplesClip(t0);
        s1 = other.timeToSamplesClip(t1);

        AudioSequence oldSequence = sequence;

        AtomicReference<AudioSequence> seqRef = new AtomicReference<AudioSequence>(sequence);
        if (!other.sequence.copy(s0, s1, seqRef))
        {
            sequence = oldSequence;
            return false;
        }
        else sequence = seqRef.get();

        return true;
    }
    public boolean paste(double start, final Clip other)
    {
        final boolean clipNeedsResampling = other.info.getSampleRate() != info.getSampleRate();
        final boolean clipNeedsNewFormat =  other.sequence.getInfo().format.getSampleSize() != sequence.getInfo().format.getSampleSize();
        Clip newClip;
        final Clip pastedClip;

        if (clipNeedsResampling || clipNeedsNewFormat)
        {
            newClip =new Clip(other, fileManager);
            if (clipNeedsResampling)
            {
                // The other clip's rate is different from ours, so resample
                // if (!newClip.resample(getInfo().sampleRate))
                return false;
            }
            if (clipNeedsNewFormat)
            {
                // Force sample formats to match.
                //newClip.convertToSampleFormat(sequence.getSampleFormat());
            }
            pastedClip = newClip;
        }
        else
        {
            // No resampling or format change needed, just use original clip without making a createFromCopy
            pastedClip = other;
        }

        long s0 = timeToSamplesClip(start);

        boolean result = false;
        if (sequence.paste(s0, pastedClip.sequence))
        {
            result = true;
        }

        return result;
    }

    //long GetIdealAppendLen() const;
    public synchronized boolean clear(double startTime, double endTime)
    {
        long startSample, endSample;

        startSample = timeToSamplesClip(startTime);
        endSample = timeToSamplesClip(endTime);

        if (sequence.delete(startSample, endSample-startSample))
        {
            return true;
        }
        return false;
    }


    //long GetIdealAppendLen() const;
    public synchronized boolean delete(long start, long len)
    {
        return sequence.delete(start, len);
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

        long s0 = 0, s1 = 0;

        s0 = timeToSamplesClip(startTime);
        s1 = timeToSamplesClip(endTime);

        return sequence.getMinMax(s0, s1-s0, outMin, outMax);
    }

    Long timeToSamplesClip(double t0)
    {
        Long s0 = 0L;

        if (t0 < offset)
            s0 = 0L;
        else if (t0 > offset + sequence.samplesCount/info.sampleRate)
            s0 = (long)sequence.samplesCount;
        else
            s0 = (long) Math.floor(((t0 - offset) * info.sampleRate) + 0.5);

        return s0;
    }

    double getStartTime()
    {
        // offset is the minimum value and it is returned; no clipping to 0
        return offset;
    }

    double getEndTime()
    {
        long numSamples = sequence.samplesCount;

        double maxLen = offset + (numSamples)/info.sampleRate/info.channels;
        // calculated value is not the length;
        // it is a maximum value and can be negative; no clipping to 0

        return maxLen;
    }

    boolean withinClip(double t)
    {
        double ts = Math.floor(t * info.sampleRate + 0.5);
        return ts > getStartSample() && ts < getEndSample() ;
    }

    boolean beforeClip(double t)
    {
        double ts = Math.floor(t * info.sampleRate + 0.5);
        return ts <= getStartSample();
    }

    boolean afterClip(double t)
    {
        double ts = Math.floor(t * info.sampleRate + 0.5);
        return ts >= getEndSample();
    }

    public  int getMinSamples()
    {
        return sequence.getMinSamples();
    }

    public int getMaxSamples()
    {
        return sequence.getMaxSamples();
    }

    public long getStartSample()
    {
        return (int)Math.floor(offset * info.sampleRate * info.channels + 0.5);
    }

    public long getEndSample()
    {
        return getStartSample() + sequence.samplesCount;
    }

    public long getNumSamples()
    {
        return sequence.samplesCount;
    }

    void setOffset(double offset)
    {
        this.offset = offset;
    }

    public double getOffset()
    {
        return offset;
    }

    void offset(double delta) { setOffset(getOffset() + delta); }

    public boolean isPlaceholder()
    {
        return isPlaceholder;
    }

    public void setPlaceholder(boolean placeholder)
    {
        isPlaceholder = placeholder;
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
