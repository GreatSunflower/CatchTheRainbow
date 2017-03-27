package com.sunflower.catchtherainbow.AudioClasses;

import android.app.Activity;
import android.util.Log;

import com.un4seen.bass.BASS;
import com.un4seen.bass.BASSmix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * Created by SuperComputer on 3/3/2017.
 */

public class AudioIO extends BasePlayer
{
    private static final String LOG_TAG = "AudioIO";

    private Project project;

//    private Handler handler = new Handler();

    private PlayerState state = PlayerState.NotInitialized;

    public AudioIO(Activity context, Project project)
    {
        super(context);
        this.project = project;
    }


    public void play()
    {
        if (state == PlayerState.NotInitialized || state == PlayerState.Stopped)
            initialize(false);

       // BASS.BASS_ChannelPlay(mixer, false);
        updateTrackAttributes(true);
        state = PlayerState.Playing;

       // handler.post(new Runnable()
        context.runOnUiThread(new Runnable()
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

    public void pause()
    {
        if(state != PlayerState.Playing) return;

       // BASS.BASS_ChannelPause(mixer);
        for (int i = 0; i < tracks.size(); i++)
        {
            TrackInfo trackInfo = tracks.get(i);

            BASS.BASS_ChannelSetLink(tracks.get(0).getChannel(), trackInfo.getChannel());
            int error = BASS.BASS_ErrorGetCode();
        }
        if(!tracks.isEmpty()) BASS.BASS_ChannelPause(tracks.get(0).getChannel());

        state = PlayerState.Paused;

       // handler.post(new Runnable()
        context.runOnUiThread(new Runnable()
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

         //   handler.post(new Runnable()
            context.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    for (AudioPlayerListener listener : audioPlayerListeners)
                    {
                        listener.onStop();
                    }
                }
            });
        } // is init
    }

    // releases all of the tracks!!! Careful!
    @Override
    public void eject()
    {
        for(int i = 0; i < tracks.size(); i++)
        {
            final TrackInfo trackInfo = tracks.get(i);
            BASS.BASS_ChannelPause(trackInfo.getChannel());
            BASS.BASS_ChannelStop(trackInfo.getChannel());
            //BASS.BASS_StreamFree(trackInfo.getChannel()); // crashes

            trackInfo.track.removeListener(trackListener);
        }
        tracks.clear();
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

          //  BASS.BASS_ChannelSetLink(tracks.get(0).getChannel(), trackInfo.getChannel());
            if(state == PlayerState.Paused && playIfStopped)
            {
                BASS.BASS_ChannelPlay(trackInfo.getChannel(), false);
                int error = BASS.BASS_ErrorGetCode();
            }
        }
     /*   if(state == PlayerState.Paused && playIfStopped && !tracks.isEmpty())
        {
            BASS.BASS_ChannelPlay(tracks.get(0).getChannel(), false);
            int error = BASS.BASS_ErrorGetCode();
        }*/
    }

    @Override
    public boolean isPlaying()
    {
        return state == PlayerState.Playing;
    }

    @Override
    public boolean isStopped()
    {
        return state == PlayerState.Stopped;
    }

    public boolean isPaused()
    {
        return state == PlayerState.Paused;
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
    public void setPosition(double position)
    {
        if(state == PlayerState.NotInitialized || state == PlayerState.Stopped) return;

        PlayerState cachedState = state;

        boolean autoPlay = cachedState == PlayerState.Playing;

        //BASS.BASS_ChannelSetPosition(mixer, BASS.BASS_ChannelSeconds2Bytes(mixer, position), BASS.BASS_POS_BYTE|BASS.BASS_POS_DECODE);
        initialize(position, autoPlay, false);
    }

    ArrayList<TrackInfo> tracks = new ArrayList<>();

    public void setTracks(ArrayList<WaveTrack> waveTracks)
    {
        eject();

        // update thread count(equals to the number of tracks)
        BASS.BASS_SetConfig(BASS.BASS_CONFIG_UPDATETHREADS, waveTracks.size());

        /*BASS.BASS_SetConfig(BASS.BASS_CONFIG_UPDATEPERIOD, 70);

        int len=BASS.BASS_GetConfig(BASS.BASS_CONFIG_UPDATEPERIOD); // get update period
        BASS.BASS_INFO info = new BASS.BASS_INFO();
        BASS.BASS_GetInfo(info); // retrieve device info
        len+=info.minbuf*10; // add the 'minbuf' plus 1ms margin
        BASS.BASS_SetConfig(BASS.BASS_CONFIG_BUFFER, len); // set the buffer length
        Log.e(LOG_TAG, "Buffer is now  " + len + " bytes long");

        BASS.BASS_SetConfig(BASS.BASS_CONFIG_SRC, 0);
        BASS.BASS_SetConfig(BASS.BASS_CONFIG_REC_BUFFER, 2000);*/

        for (int i = 0; i < waveTracks.size(); i++)
        {
            WaveTrack track = waveTracks.get(i);

            TrackInfo trackInfo = new TrackInfo(0, track, 0);
            int channel = BASS.BASS_StreamCreate(track.getInfo().sampleRate, track.getInfo().channels,
                    BASS.BASS_SAMPLE_FLOAT | BASS.BASS_STREAM_AUTOFREE, new StreamProc(), trackInfo);

            trackInfo.setChannel(channel);

            tracks.add(trackInfo);
            trackInfo.track.addListener(trackListener);
        }

        initialize(false);
    }

    public void setTrack(WaveTrack waveTrack)
    {
        eject();

        // update thread count(equals to the number of tracks)
        BASS.BASS_SetConfig(BASS.BASS_CONFIG_UPDATETHREADS, 1);

        TrackInfo trackInfo = new TrackInfo(0, waveTrack, 0);
        int channel = BASS.BASS_StreamCreate(waveTrack.getInfo().sampleRate, waveTrack.getInfo().channels,
                BASS.BASS_SAMPLE_FLOAT | BASS.BASS_STREAM_AUTOFREE, new StreamProc(), trackInfo);

        trackInfo.setChannel(channel);

        tracks.add(trackInfo);
        trackInfo.track.addListener(trackListener);


        initialize(false);
    }

    public ArrayList<TrackInfo> getTracks()
    {
        return tracks;
    }

    // used to determine track samples during playback
    public static class TrackInfo
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

        public WaveTrack getTrack()
        {
            return track;
        }
    }

    class StreamProc implements BASS.STREAMPROC
    {
        @Override
        public int STREAMPROC(int handle, ByteBuffer buffer, int length, Object user)
        {
            // get out if a wrong parameter passed
            if(!(user instanceof TrackInfo)) return BASS.BASS_STREAMPROC_END;

            TrackInfo track = (TrackInfo)user;
            int samplesRead = 0;

            try
            {
                int sampleSize = track.track.getInfo().format.getSampleSize();
                samplesRead = track.track.get(buffer, track.currentSample, length / sampleSize) / sampleSize;

                track.currentSample+=samplesRead;

                if (samplesRead == -1 || samplesRead < length / sampleSize || track.track.getEndSample() < track.currentSample)
                {
                    Log.e(LOG_TAG, "Normal end! " + track.track.name);

                    samplesRead |= BASS.BASS_STREAMPROC_END;

                   // checkFinish(track);
                    return samplesRead;
                }

                //Log.d(LOG_TAG, String.format("FileProcUserRead: requested {i}, read {i} ", length, bytesRead));
                return samplesRead * sampleSize;
            }
            catch(Exception ex)
            {
                Log.e(LOG_TAG, "File Read Exception! " + ex.getMessage());
                //track.currentSample = 0;
                //checkFinish(track);
            }
            samplesRead |= BASS.BASS_STREAMPROC_END;

            return BASS.BASS_STREAMPROC_END;
        }

        void checkFinish(TrackInfo trackInfo)
        {
            final TrackInfo longestTrack = findLongestTrack();

            if(trackInfo == longestTrack)
            {
                stop();
                // notify about the end
                //handler.post(new Runnable()
                context.runOnUiThread(new Runnable()
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

    protected class EndSync implements BASS.SYNCPROC
    {
        public void SYNCPROC(int handle, int channel, int data, Object user)
        {
          //  TrackInfo track = (TrackInfo)user;
            Log.e("TIME", "END!"/* + track.track.name*/);
            final TrackInfo longestTrack = findLongestTrack();

            TrackInfo trackInfo = (TrackInfo)user;

            if(trackInfo == longestTrack)
            {
                stop();
                // notify about the end
                //handler.post(new Runnable()
                context.runOnUiThread(new Runnable()
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

    @Override
    public void initialize(boolean autoPlay)
    {
        initialize(0, autoPlay, true);
    }

    public void reinitialize(boolean autoPlay)
    {
        PlayerState cachedState = state;
        int lastSample = 0;

        //final TrackInfo longestTrack = findLongestTrack();
        //if(longestTrack != null) lastSample = longestTrack.currentSample;

        initialize(0, autoPlay, true);

        if(cachedState == PlayerState.Playing)
            play();
      }

    private int mixer = 0;
    // start - time to start from
    public void initialize(final double start, final boolean autoPlay, final boolean notify)
    {
        stop();

        if(tracks.isEmpty()) return;

        AudioInfo info = project.getProjectAudioInfo();

        //ArrayList<WaveTrack> waveTracks = project.getTracks();

        /*mixer = BASSmix.BASS_Mixer_StreamCreate(info.sampleRate, info.channels, BASSmix.BASS_MIXER_NONSTOP | BASSmix.BASS_MIXER_POSEX | BASS.BASS_SAMPLE_FLOAT);
        for (int i = 0; i < tracks.size(); i++)
        {
            //WaveTrack track = waveTracks.get(i);

            TrackInfo trackInfo = tracks.get(i); // new TrackInfo(0, track, track.timeToSamples(start));
            AudioInfo audioInfo = trackInfo.track.getInfo();

            int channel = BASS.BASS_StreamCreate(audioInfo.sampleRate, audioInfo.channels,
                    BASS.BASS_SAMPLE_FLOAT|BASS.BASS_STREAM_DECODE|BASSmix.BASS_MIXER_BUFFER, new StreamProc(), trackInfo);

            trackInfo.setChannel(channel);
            trackInfo.setCurrentSample(trackInfo.track.timeToSamples(start));

            BASSmix.BASS_Mixer_StreamAddChannel(mixer, channel, BASSmix.BASS_MIXER_MATRIX|BASSmix.BASS_MIXER_DOWNMIX|BASSmix.BASS_MIXER_NORAMPIN|BASS.BASS_STREAM_AUTOFREE);
            BASSmix.BASS_Mixer_ChannelSetSync(channel, BASS.BASS_SYNC_END|BASS.BASS_SYNC_MIXTIME, 0, EndSync, 0);
        }

        BASS.BASS_ChannelPlay(mixer, true);

        BASS.BASS_ChannelSetSync(mixer, BASS.BASS_SYNC_END|BASS.BASS_SYNC_MIXTIME, 0, EndSync, 0);*/
        for (int i = 0; i < tracks.size(); i++)
        {
            //WaveTrack track = waveTracks.get(i);

            TrackInfo trackInfo = tracks.get(i); // new TrackInfo(0, track, track.timeToSamples(start));
            AudioInfo audioInfo = trackInfo.track.getInfo();

            int channel = BASS.BASS_StreamCreate(audioInfo.sampleRate, audioInfo.channels,
                    BASS.BASS_SAMPLE_FLOAT | BASS.BASS_STREAM_AUTOFREE, new StreamProc(), trackInfo);

            trackInfo.setChannel(channel);
            trackInfo.setCurrentSample(trackInfo.track.timeToSamples(start));

            BASS.BASS_ChannelSetSync(channel, BASS.BASS_SYNC_END|BASS.BASS_SYNC_MIXTIME, 0, new EndSync(), trackInfo);
        }
        //  BASS.BASS_ChannelPlay(mixer, true);

        final TrackInfo longestTrack = findLongestTrack();
        if (notify && longestTrack != null)
        {
            //handler.post(new Runnable()
            /*context.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {*/
            for (AudioPlayerListener listener : audioPlayerListeners)
            {
                listener.onInitialized((float) longestTrack.track.getEndTime());
            }
                /*}
            });*/
        }

        //Set callback
        //BASS.BASS_ChannelSetSync(mixer, BASS.BASS_STREAMPROC_END | BASS.BASS_SYNC_MIXTIME, 0, EndSync, 0);

        state = PlayerState.Paused;
        if(autoPlay)
            play();
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
