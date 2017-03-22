package com.sunflower.catchtherainbow.AudioClasses;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by SuperComputer on 3/16/2017.
 * Manages audio chunks.
 */

public class WaveTrack implements Serializable
{
    private ArrayList<Clip> clips = new ArrayList<>();

    private AudioInfo info = new AudioInfo(44100, 2);

    private float gain = 0.5f;
    private float pan = 0;

    private boolean solo = false;
    private boolean mute = false;

    protected String name;
    private FileManager manager;

    protected transient ArrayList<WaveTrackListener> listeners = new ArrayList<>();

    public WaveTrack(String name, FileManager manager)
    {
        this.name = name;
        this.manager = manager;
    }

    // to initialize transient variable during deserialization
    private Object readResolve()
    {
        listeners = new ArrayList<>();
        return this;
    }

    public boolean set(ByteBuffer buffer, int start, int len)
    {
        for (Clip clip: clips)
        {
            int clipStart = clip.getStartSample();
            int clipEnd = clip.getEndSample();

            // in range
            if (clipEnd > start && clipStart < start+len)
            {
                int samplesToCopy = Math.min(start+len - clipStart, clip.getNumSamples());
                int startDelta = clipStart - start;
                int inClipDelta = 0;

                if (startDelta < 0)
                {
                    inClipDelta = -startDelta; // make positive value
                    samplesToCopy -= inClipDelta;
                    // samplesToCopy is now either len or  (clipEnd - clipStart) - (start - clipStart)
                    //    == clipEnd - start > 0 samplesToCopy is not more than len

                    startDelta = 0;
                    // startDelta is zero
                }

                if (clip.setSamples(buffer, inClipDelta, samplesToCopy, info))
                {
                    return false;
                }
            } // if in range
        }
        return true;
    }

    public int get(ByteBuffer buffer, int start, int len)
    {
        boolean doClear = true;
        for (final Clip clip: clips)
        {
            if (start >= clip.getStartSample() && start + len <= clip.getEndSample())
            {
                doClear = false;
                break;
            }
        }
        if (doClear)
        {
            // fill in empty space with zero
            AudioHelper.clearSamples(buffer, 0, len, info);
        }

        int bytesRead = 0;
        for (final Clip clip: clips)
        {
            int clipStart = clip.getStartSample();
            int clipEnd = clip.getEndSample();

            // in the range
            if (clipEnd > start && clipStart < start + len)
            {
                int samplesToCopy = Math.min(start+len - clipStart, clip.getNumSamples());
                int startDelta = clipStart - start;
                int inClipDelta = 0;

                if (startDelta < 0)
                {
                    inClipDelta = -startDelta; // make positive value
                    samplesToCopy -= inClipDelta;
                    // samplesToCopy is now either len or (clipEnd - clipStart) - (start - clipStart)
                    //  == clipEnd - start > 0
                    // samplesToCopy is not more than len
                    startDelta = 0;
                    // startDelta is zero
                }

                bytesRead += clip.getSamples(buffer, inClipDelta, samplesToCopy);
                //clip->GetSamples((samplePtr)(((char*)buffer) + startDelta.as_size_t() * SAMPLE_SIZE(format)), format, inclipDelta, samplesToCopy.as_size_t() )
            } // in the range
        } // for each clip

        return bytesRead;
    }

    /** Get the time at which the first clip in the track starts
     *
     * @return time in seconds, or zero if there are no clips in the track
     */
    public  double getStartTime()
    {
        boolean found = false;
        double best = 0.0;

        if (clips.isEmpty())
            return 0;

        for (Clip clip : clips)
        if (!found)
        {
            found = true;
            best = clip.getStartTime();
        }
        else if (clip.getStartTime() < best)
            best = clip.getStartTime();

        return best;
    }

    /** Get the time at which the last clip in the track ends, plus
     * recorded stuff
     *
     * @return time in seconds, or zero if there are no clips in the track.
     */
    public double getEndTime()
    {
        boolean found = false;
        double best = 0.0;

        if (clips.isEmpty())
            return 0;

        for (final Clip clip : clips)
        if (!found)
        {
            found = true;
            best = clip.getEndTime();
        }
        else if (clip.getEndTime() > best)
            best = clip.getEndTime();

        return best;
    }

    public int timeToSamples(double time)
    {
        return (int) Math.floor(time * info.sampleRate * info.channels + 0.5);
    }
    /** @brief Convert correctly between an number of samples and an (absolute) time in seconds.
     *
     * @param pos The time number of samples from the start of the track to convert.
     * @return The time in seconds.
     */
    public double samplesToTime(int pos)
    {
        return pos / info.sampleRate / info.channels;
    }

    public int getEndSample()
    {
        return timeToSamples(getEndTime());
    }

    public void addClip(Clip clip)
    {
        clips.add(clip);
    }

    // ------- Getters setters -------------
    public ArrayList<Clip> getClips()
    {
        return clips;
    }

    public AudioInfo getInfo()
    {
        return info;
    }

    public float getGain()
    {
        return gain;
    }

    public void setGain(float gain)
    {
        if(gain < 0 || gain > 1) return;

        this.gain = gain;
        for(WaveTrackListener listener: listeners)
            listener.onPropertyUpdated(this);
    }

    public float getPan()
    {
        return pan;
    }

    public void setPan(float pan)
    {
        this.pan = pan;
        for(WaveTrackListener listener: listeners)
            listener.onPropertyUpdated(this);
    }

    public boolean isSolo()
    {
        return solo;
    }

    public void setSolo(boolean solo)
    {
        this.solo = solo;
        for(WaveTrackListener listener: listeners)
            listener.onPropertyUpdated(this);
    }

    public boolean isMuted()
    {
        return mute;
    }

    public void setMuted(boolean mute)
    {
        this.mute = mute;
        for(WaveTrackListener listener: listeners)
            listener.onPropertyUpdated(this);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
        for(WaveTrackListener listener: listeners)
            listener.onPropertyUpdated(this);
    }

    // ----- Listeners -------
    public ArrayList<WaveTrackListener> getListeners()
    {
        return listeners;
    }

    public void addListener(WaveTrackListener listener)
    {
        if(listener != null)
            listeners.add(listener);
    }
    public void removeListener(WaveTrackListener listener)
    {
        if(listener != null)
            listeners.remove(listener);
    }
    // ----- listeners end-----

    public interface WaveTrackListener
    {
        void onPropertyUpdated(WaveTrack track);
    }
}
