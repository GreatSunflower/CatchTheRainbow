package com.sunflower.catchtherainbow.AudioClasses;

import com.sunflower.catchtherainbow.Helper;

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
    private float gain;
    private float pan;
    protected String name;
    private FileManager manager;

    public WaveTrack(String name, FileManager manager)
    {
        this.name = name;
        this.manager = manager;
    }

    public boolean get(ByteBuffer buffer, int start, int len)
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

                clip.getSamples(buffer, inClipDelta, samplesToCopy);
                //clip->GetSamples((samplePtr)(((char*)buffer) + startDelta.as_size_t() * SAMPLE_SIZE(format)), format, inclipDelta, samplesToCopy.as_size_t() )
            } // in the range
        } // for each clip

        return true;
    }

    public void addClip(Clip clip)
    {
        clips.add(clip);
    }
}
