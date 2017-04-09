package com.sunflower.catchtherainbow.AudioClasses;

import java.io.IOException;
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

    public boolean set(ByteBuffer buffer, long start, long len) throws IOException, ClassNotFoundException
    {
        for (Clip clip: clips)
        {
            long clipStart = clip.getStartSample();
            long clipEnd = clip.getEndSample();

            // in range
            if (clipEnd > start && clipStart < start+len)
            {
                long samplesToCopy = Math.min(start+len - clipStart, clip.getNumSamples());
                long startDelta = clipStart - start;
                long inClipDelta = 0;

                if (startDelta < 0)
                {
                    inClipDelta = -startDelta; // make positive value
                    samplesToCopy -= inClipDelta;
                    // samplesToCopy is now either len or  (clipEnd - clipStart) - (start - clipStart)
                    //    == clipEnd - start > 0 samplesToCopy is not more than len

                    startDelta = 0;
                    // startDelta is zero
                }

                if (!clip.setSamples(buffer, inClipDelta, samplesToCopy, info))
                {
                    return false;
                }
            } // if in range
        }
        return true;
    }

    public int get(ByteBuffer buffer, long start, int len) throws IOException, ClassNotFoundException
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
            long clipStart = clip.getStartSample();
            long clipEnd = clip.getEndSample();

            // in the range
            if (clipEnd > start && clipStart < start + len)
            {
                long samplesToCopy = Math.min(start+len - clipStart, clip.getNumSamples());
                long startDelta = clipStart - start;
                long inClipDelta = 0;

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

                bytesRead += clip.getSamples(buffer, inClipDelta, (int) samplesToCopy);
                //clip->GetSamples((samplePtr)(((char*)buffer) + startDelta.as_size_t() * SAMPLE_SIZE(format)), format, inclipDelta, samplesToCopy.as_size_t() )
            } // in the range
        } // for each clip

        return bytesRead;
    }

    public WaveTrack copy(double t0, double t1) throws IOException, ClassNotFoundException
    {
        if (t1 <= t0)
            return null;

        WaveTrack newTrack;

        newTrack = new WaveTrack("", manager);

        for (final Clip clip : clips)
        {
            if (t0 <= clip.getStartTime() && t1 >= clip.getEndTime())
            {
                // Whole clip is in createFromCopy region
                //printf("createFromCopy: clip %i is in createFromCopy region\n", (int)clip);
                Clip newClip = new Clip(clip, manager);
                newTrack.clips.add(newClip);
                newClip.offset(-t0);
            }
            else
            if (t1 > clip.getStartTime() && t0 < clip.getEndTime())
            {
                // Clip is affected by command
                Clip newClip = new Clip(clip, manager);
                double clip_t0 = t0;
                double clip_t1 = t1;
                if (clip_t0 < clip.getStartTime())
                    clip_t0 = clip.getStartTime();
                if (clip_t1 > clip.getEndTime())
                    clip_t1 = clip.getEndTime();

                newClip.offset(-t0);
                if (newClip.getOffset() < 0)
                    newClip.setOffset(0);


                if (newClip.createFromCopy(clip_t0, clip_t1, clip))
                {
                    newTrack.clips.add(newClip); // transfer ownership
                }

            } // if
        } // for

        // If the selection ends in whitespace, create a placeholder
        // clip representing that whitespace
        if (newTrack.getEndTime() + 1.0 / newTrack.info.sampleRate < t1 - t0)
        {
            Clip placeholder = new Clip(manager, newTrack.info);
            placeholder.setPlaceholder(true);
            if (placeholder.insertSilence(0, (t1 - t0) - newTrack.getEndTime()) )
            {
                placeholder.offset(newTrack.getEndTime());
                newTrack.clips.add(placeholder); // transfer ownership
            }
        }

        return newTrack;
    }

    public boolean paste(double t0, WaveTrack src) throws IOException, ClassNotFoundException
    {
        if(src == null)
            return false;

        WaveTrack other = src;

        //
        // Pasting is a bit complicated, because with the existence of multiclip mode,
        // we must guess the behaviour the user wants.
        //
        // Currently, two modes are implemented:
        //
        // - If a single clip should be pasted, and it should be pasted inside another
        //   clip, no NEW clips are generated. The audio is simply inserted.
        //   This resembles the old (pre-multiclip support) behaviour. However, if
        //   the clip is pasted outside of any clip, a NEW clip is generated. This is
        //   the only behaviour which is different to what was done before, but it
        //   shouldn't confuse users too much.
        //
        // - If multiple clips should be pasted, or a single clip that does not fill
        // the duration of the pasted track, these are always pasted as single
        // clips, and the current clip is splitted, when necessary. This may seem
        // strange at first, but it probably is better than trying to auto-merge
        // anything. The user can still merge the clips by hand (which should be a
        // simple command reachable by a hotkey or single mouse click).
        //

        if (other.clips.size() == 0)
            return false;

        boolean singleClipMode = (other.clips.size() == 1 && other.getStartTime() == 0.0);

        double insertDuration = other.getEndTime();

        if (!singleClipMode)
        {
            // We need to insert multiple clips, so split the current clip and
            // move everything to the right, then try to paste again
            if (!isEmpty(t0, getEndTime()))
            {
                WaveTrack tmp = cut(t0, getEndTime()+1.0/info.sampleRate);
                boolean bResult = paste(t0 + insertDuration, tmp);

                if(!bResult) return false;
            }
        }
        else
        {
            // We only need to insert one single clip, so just move all clips
            // to the right of the paste point out of the way
            for (final Clip clip : clips)
            {
                if (clip.getStartTime() > t0-(1.0/info.sampleRate))
                    clip.offset(insertDuration);
            }
        }

        if (singleClipMode)
        {
            // Single clip mode
            Clip insideClip = null;

            for (final Clip clip : clips)
            {
                if (clip.withinClip(t0))
                {
                    insideClip = clip;
                    break;
                }
            }

            if (insideClip != null)
            {
                return insideClip.paste(t0, other.getClipByIndex(0));
            }

            // Just fall through and exhibit NEW behaviour
        }

        // Insert NEW clips

        for (final Clip clip : other.clips)
        {
            // Don't actually paste in placeholder clips
            if (!clip.isPlaceholder())
            {
                Clip newClip = new Clip(clip, manager);
               // newClip.resample(rate);
                newClip.offset(t0);
                clips.add(newClip); // transfer ownership
            }
        }
        return true;
    }

    WaveTrack cut(double t0, double t1) throws IOException, ClassNotFoundException
    {
        if (t1 < t0)
            return null;

        WaveTrack tmp = copy(t0, t1);

        if (tmp == null)
            return tmp;

        if (!clear(t0, t1))
            return null;

        return tmp;
    }

    Clip getClipByIndex(int index)
    {
        if(index < clips.size())
            return clips.get(index);
        else
            return null;
    }

    boolean isEmpty(double t0, double t1)
    {
        for (final Clip clip : clips)
        {
            if (!clip.beforeClip(t1) && !clip.afterClip(t0))
            {
                // We found a clip that overlaps this region
                return false;
            }
        }
        // Otherwise, no clips overlap this region
        return true;
    }


    public boolean clear(double t0, double t1) throws IOException, ClassNotFoundException
    {
        return handleDelete(t0, t1, false);
    }

    private boolean handleDelete(double t0, double t1, boolean split) throws IOException, ClassNotFoundException
    {
        if (t1 < t0)
            return false;

        ArrayList<Clip> clipsToDelete = new ArrayList<>();
        ArrayList<Clip> clipsToAdd = new ArrayList<>();

        for (Clip clip : clips)
        {
            if (clip.beforeClip(t0) && clip.afterClip(t1))
            {
                // Whole clip must be deleted
                clipsToDelete.remove(clip);
            }
            else if (!clip.beforeClip(t1) && !clip.afterClip(t0))
            {
                    if (split)
                    {
                        // Three cases:
                        if (clip.beforeClip(t0))
                        {
                            // Delete from the left edge
                            clip.clear(clip.getStartTime(), t1);
                            clip.offset(t1-clip.getStartTime());
                        }
                        else
                        if (clip.afterClip(t1))
                        {
                            // Delete to right edge
                            clip.clear(t0, clip.getEndTime());
                        }
                        else
                        {
                            // Delete in the middle of the clip...we actually create two
                            // NEW clips out of the left and right halves...

                            // left
                            Clip clipToAdd = new Clip(clip, manager);
                            clipsToAdd.add(clipToAdd);
                            clipToAdd.clear(t0, clip.getEndTime());

                            // right
                            clipToAdd = new Clip(clip, manager);
                            clipsToAdd.add(clipToAdd);
                            Clip right = clipsToAdd.get(clipsToAdd.size()-1);
                            right.clear(clip.getStartTime(), t1);
                            right.offset(t1 - clip.getStartTime());

                            clipsToDelete.add(clip);
                        }
                    }
                    else
                    { // (We are not doing a split cut)
                        // clip->Clear keeps points < t0 and >= t1 via Envelope::CollapseRegion

                        if (!clip.clear(t0, t1))
                            return false;
                    }
            }
            else
            {
                if (clip.beforeClip(t1))
                {
                    // Clip is "behind" the region -- offset it unless we're splitting
                    // or we're using the "don't move other clips" mode
                    if (!split )
                        clip.offset(-(t1 - t0));
                }
            } // else
        } // for

        for (Clip clip: clipsToDelete)
        {
            clips.remove(clip);
        }

        for (Clip clip: clipsToAdd)
            clips.add(clip); // transfer ownership

        return true;
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

    public long timeToSamples(double time)
    {
        return (long) Math.floor(time * info.sampleRate * info.channels + 0.5);
    }
    /** @brief Convert correctly between an number of samples and an (absolute) time in seconds.
     *
     * @param pos The time number of samples from the start of the track to convert.
     * @return The time in seconds.
     */
    public double samplesToTime(long pos)
    {
        return pos / (double)info.sampleRate / info.channels;
    }

    public long getEndSample()
    {
        return timeToSamples(getEndTime());
    }

    public void addClip(Clip clip)
    {
        clips.add(clip);
    }

    public Clip getClipAtSample(int sample)
    {
        for (Clip clip: clips)
        {
            long start = clip.getStartSample();
            long len   = clip.getNumSamples();

            if (sample >= start && sample < start + len)
                return clip;
        }

        return null;
    }

    public Clip createClip()
    {
        Clip clip = new Clip(manager, info);
        clips.add(clip);
        return clip;
    }

    public Clip rightmostOrNewClip()
    {
        if (clips.isEmpty())
        {
            Clip clip = createClip();
            clip.setOffset(0);
            return clip;
        }
        else
        {
            Clip rightmost = clips.get(0);
            double maxOffset = rightmost.getOffset();
            for (int i = clips.size()-1; i > 0; i--)
            {
                Clip clip = clips.get(i);
                double offset = clip.getOffset();
                if (maxOffset < offset)
                {
                    maxOffset = offset;
                    rightmost = clip;
                }
            }
            return rightmost;
        }
    }

    boolean append(ByteBuffer buffer, int len) throws IOException, ClassNotFoundException
    {
        return rightmostOrNewClip().append(buffer, len);
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
