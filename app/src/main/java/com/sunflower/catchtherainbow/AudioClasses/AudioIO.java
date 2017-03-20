package com.sunflower.catchtherainbow.AudioClasses;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import com.un4seen.bass.BASS;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by SuperComputer on 3/3/2017.
 */

public class AudioIO extends BasePlayer
{
    private static final String LOG_TAG = "AudioIO";

    private Project project;

    private Handler handler = new Handler();

    private PlayerState state = PlayerState.NotInitialized;

    public AudioIO(Activity context, Project project)
    {
        super(context);
        this.project = project;
    }


    public synchronized void play()
    {
        if (state == PlayerState.NotInitialized)
            initialize();

        updateTrackAttributes(true);
        state = PlayerState.Playing;

        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                for (AudioPlayerListener listener : audioPlayerListeners)
                {
                    listener.onPlay();
                }
            }
        });
    }

    public synchronized void pause()
    {
        if(state != PlayerState.Playing) return;

        for (int i = 0; i < tracks.size(); i++)
        {
            TrackInfo trackInfo = tracks.get(i);

            BASS.BASS_ChannelPause(trackInfo.getChannel());
            int error = BASS.BASS_ErrorGetCode();
        }

        state = PlayerState.Paused;

        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                for (AudioPlayerListener listener : audioPlayerListeners)
                {
                    listener.onPause();
                }
            }
        });
    }

    public void stop()
    {
        if(state != PlayerState.NotInitialized)
        {
            for(int i = 0; i < tracks.size(); i++)
            {
                TrackInfo trackInfo = tracks.get(i);

                BASS.BASS_ChannelStop(trackInfo.getChannel());
            }

            state = PlayerState.Stopped;

            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    for (AudioPlayerListener listener : audioPlayerListeners)
                    {
                        listener.onCompleted();
                    }
                }
            });
        }
    }

    @Override
    public synchronized void eject()
    {
        if(state == PlayerState.NotInitialized) return;

        for(int i = 0; i < tracks.size(); i++)
        {
            TrackInfo trackInfo = tracks.get(i);
            BASS.BASS_ChannelStop(trackInfo.getChannel());
            //BASS.BASS_StreamFree(trackInfo.getChannel()); // crashes
            trackInfo.track.removeListener(trackListener);
        }
        tracks.clear();

        state = PlayerState.NotInitialized;
    }

    // refreshes attributes in real time such as pan, gain and so on
    public void updateTrackAttributes(boolean playIfStopped)
    {
        if(state == PlayerState.NotInitialized) return;

        for(int i = 0; i < tracks.size(); i++)
        {
            TrackInfo trackInfo = tracks.get(i);

            if(trackInfo.track.isMuted())
            {
                BASS.BASS_ChannelSetAttribute(trackInfo.getChannel(), BASS.BASS_ATTRIB_VOL, 0);
            }
            else BASS.BASS_ChannelSetAttribute(trackInfo.getChannel(), BASS.BASS_ATTRIB_VOL, trackInfo.track.getGain());

            // soon will be there
            // BASS.BASS_ChannelSetAttribute(trackInfo.getChannel(), BASS.BASS_ATTRIB_PAN, trackInfo.track.getPan());

            if(state == PlayerState.Paused && playIfStopped)
            {
                BASS.BASS_ChannelPlay(trackInfo.getChannel(), false);
                int error = BASS.BASS_ErrorGetCode();
            }
        }
    }

    @Override
    public boolean isPlaying()
    {
        return state == PlayerState.Playing;
    }

    @Override
    public boolean isInitialized()
    {
        return state != PlayerState.NotInitialized;
    }

    @Override
    public double getProgress()
    {
        final TrackInfo longestTrack = findLongestTrack();
        if(longestTrack == null) return 0;
       //double position = BASS.BASS_ChannelBytes2Seconds(longestTrack.getChannel(), BASS.BASS_ChannelGetPosition(longestTrack.getChannel(), BASS.BASS_POS_BYTE));
        double position = longestTrack.track.samplesToTime(longestTrack.currentSample);
        return position;
    }

    @Override
    public synchronized void setPosition(double position)
    {
        if(state == PlayerState.NotInitialized || state == PlayerState.Stopped) return;

        PlayerState cachedState = state;

        initialize(position, false);

        state = PlayerState.Paused;

        if(cachedState == PlayerState.Playing) play();
        else pause();
    }

    ArrayList<TrackInfo> tracks = new ArrayList<>();

    // used to determine track samples during playback
    private class TrackInfo
    {
        protected WaveTrack track;

        protected int channel;

        protected int currentSample = 0;

        public TrackInfo(int channel, WaveTrack track, int currentSample)
        {
            this.channel = channel;
            this.track = track;
            this.currentSample = currentSample;
        }

        public void setCurrentSample(int currentSample)
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

        public int getCurrentSample()
        {
            return currentSample;
        }
    }

    class StreamProc implements BASS.STREAMPROC
    {
        @Override
        public int STREAMPROC(int handle, ByteBuffer buffer, int length, Object user)
        {
            TrackInfo track = (TrackInfo)user;
            int samplesRead = 0;

            try
            {
                int sampleSize = track.track.getInfo().format.sampleSize;
                samplesRead = track.track.get(buffer, track.currentSample, length / sampleSize) / sampleSize;

                track.currentSample+=samplesRead;

                if (samplesRead == -1 || samplesRead < length / sampleSize)
                {
                    Log.e(LOG_TAG, "Normal end! " + track.track.name);

                    //samplesRead |= BASS.BASS_STREAMPROC_END;

                    checkFinish(track);
                    return BASS.BASS_STREAMPROC_END;
                }

                //Log.d(LOG_TAG, String.format("FileProcUserRead: requested {i}, read {i} ", length, bytesRead));
                return samplesRead * sampleSize;
            }
            catch(Exception ex)
            {
                Log.e(LOG_TAG, "File Read Exception! " + ex.getMessage());
                //track.currentSample = 0;
                checkFinish(track);
            }
            //samplesRead |= BASS.BASS_STREAMPROC_END;

            return BASS.BASS_STREAMPROC_END;
        }

        void checkFinish(TrackInfo trackInfo)
        {
            final TrackInfo longestTrack = findLongestTrack();

            if(trackInfo == longestTrack)
            {
                eject();
                // notify about the end
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (AudioPlayerListener listener : audioPlayerListeners)
                        {
                            listener.onCompleted();
                        }
                    }
                });
            } // if
        }
    };

    protected final BASS.SYNCPROC EndSync = new BASS.SYNCPROC()
    {
        public void SYNCPROC(int handle, int channel, int data, Object user)
        {

            for(AudioPlayerListener listener: audioPlayerListeners)
            {
                listener.onCompleted();
            }
        }
    };


    @Override
    public void initialize()
    {
        initialize(0, true);
    }

    public void reinitialize()
    {
        PlayerState cachedState = state;
        int lastSample = 0;

        //final TrackInfo longestTrack = findLongestTrack();
        //if(longestTrack != null) lastSample = longestTrack.currentSample;

        initialize(0, true);

        if(cachedState == PlayerState.Playing)
            play();
      }

    // start - time to start from
    public synchronized void initialize(double start, boolean notify)
    {
        eject();

        AudioInfo info = project.getProjectAudioInfo();

        BASS.BASS_SetConfig(BASS.BASS_CONFIG_UPDATETHREADS, project.getTracks().size()+1);

        //mixer = BASSmix.BASS_Mixer_StreamCreate(info.sampleRate, info.channels, BASS.BASS_SAMPLE_FLOAT/*|BASSmix.BASS_MIXER_END /*|BASS.BASS_STREAM_AUTOFREE|BASSmix.BASS_MIXER_BUFFER*/);

        ArrayList<WaveTrack> waveTracks = project.getTracks();
        for(int i = 0; i < waveTracks.size(); i++)
        {
            WaveTrack track = waveTracks.get(i);

            TrackInfo trackInfo = new TrackInfo(0, track, track.timeToSamples(start));
            int channel = BASS.BASS_StreamCreate(track.getInfo().sampleRate, track.getInfo().channels,
                    BASS.BASS_SAMPLE_FLOAT/*|BASS.BASS_STREAM_AUTOFREE*/, new StreamProc(), trackInfo);

            trackInfo.setChannel(channel);

            tracks.add(trackInfo);
            trackInfo.track.addListener(trackListener);

            //BASS.BASS_ChannelPlay(channel, true);

            //  int channel = BASS.BASS_StreamCreate(track.getInfo().sampleRate, track.getInfo().channels,
            //          BASS.BASS_STREAM_DECODE|BASS.BASS_SAMPLE_FLOAT|BASSmix.BASS_MIXER_BUFFER, streamProc, new TrackInfo(0, track, 0));
            /*boolean isOk = BASSmix.BASS_Mixer_StreamAddChannel(mixer, channel, 0);
            if(!isOk)
            {
                Log.e("Player", "Fuck!");
            }*/
        }

        final TrackInfo longestTrack = findLongestTrack();
        if (notify && longestTrack != null)
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    for (AudioPlayerListener listener : audioPlayerListeners)
                    {
                        listener.onInitialized((float) longestTrack.track.getEndTime());
                    }
                }
            });

        //Set callback
        //BASS.BASS_ChannelSetSync(mixer, BASS.BASS_STREAMPROC_END | BASS.BASS_SYNC_MIXTIME, 0, EndSync, 0);

        state = PlayerState.Paused;

    }

    private WaveTrack.WaveTrackListener trackListener = new WaveTrack.WaveTrackListener()
    {
        @Override
        public void onPropertyUpdated(WaveTrack track)
        {
            updateTrackAttributes(false);
        }
    };

    // returns null if an error occurred
    private TrackInfo findLongestTrack()
    {
        if(tracks.isEmpty()) return null;

        TrackInfo maxTrack = tracks.get(0);
        for(int i = 0; i < tracks.size(); i++)
        {
            TrackInfo track = tracks.get(i);

            if(track.track.getEndTime() > maxTrack.track.getEndTime())
            {
                maxTrack = track;
            }
        }
        return maxTrack;
    }

    private static void ThrowOnError()
    {
        int err = BASS.BASS_ErrorGetCode();
        if (err != BASS.BASS_OK)
        {
            //throw new Exception(String.format("bass.dll reported {0}.", err));
            Log.e(LOG_TAG, "BASS reported " + err);
        }
    }
}
