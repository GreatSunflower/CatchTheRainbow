package com.sunflower.catchtherainbow.AudioClasses;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import com.sunflower.catchtherainbow.Helper;
import com.un4seen.bass.BASS;
import com.un4seen.bass.BASSmix;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by SuperComputer on 3/3/2017.
 * Plays audio tracks and records sound
 */

public class AudioIO extends BasePlayer
{
    private static final String LOG_TAG = "AudioIO";

    private Project project;

//    private Handler handler = new Handler();

    private PlayerState state = PlayerState.NotInitialized;

    private boolean isRecording = false;
    private TrackInfo recorderTrack = null;

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

        if(isRecording && recorderTrack != null)
        {
            BASS.BASS_ChannelPlay(recorderTrack.channel, false);
        }

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

        if(isRecording && recorderTrack != null)
        {
            BASS.BASS_ChannelPause(recorderTrack.channel);
        }

       // BASS.BASS_ChannelPause(mixer);
        for (int i = 0; i < tracks.size(); i++)
        {
            TrackInfo trackInfo = tracks.get(i);

           // BASS.BASS_ChannelSetLink(tracks.get(0).getChannel(), trackInfo.getChannel());
            BASS.BASS_ChannelPause(trackInfo.getChannel());
            int error = BASS.BASS_ErrorGetCode();
        }
       // if(!tracks.isEmpty()) BASS.BASS_ChannelPause(tracks.get(0).getChannel());

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
        stop(true);
    }

    // lets specify notification setting
    private void stop(boolean notify)
    {
        if(state != PlayerState.NotInitialized)
        {
            if(isRecording && recorderTrack != null)
            {
                BASS.BASS_ChannelStop(recorderTrack.channel);

                // write remaining buffer
                appender.addToQueue(new TrackAppender.BufferData(tempBuff, tempBuff.position()/4));

                isRecording = false;
                recorderTrack = null;
            }

            for(int i = 0; i < tracks.size(); i++)
            {
                TrackInfo trackInfo = tracks.get(i);

                if(recorderTrack != null && trackInfo.getTrack() == recorderTrack.getTrack()) continue;

                BASS.BASS_ChannelStop(trackInfo.getChannel());
            }

            state = PlayerState.Stopped;

            if(notify)
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

    private TrackAppender appender;
    public void startRecording(WaveTrack recorderTrack)
    {
        if(recorderTrack == null) return;


        AudioInfo info = project.getProjectAudioInfo();

        int recHandle = BASS.BASS_RecordStart(info.sampleRate, info.channels, BASS.BASS_RECORD_PAUSE/*|BASS.BASS_SAMPLE_FLOAT*/, new RecordProc(), null);

        this.recorderTrack = new TrackInfo(recHandle, recorderTrack, 0);

        initialize(false);

        appender = new TrackAppender(recorderTrack);
        tempBuff = ByteBuffer.allocateDirect(AudioSequence.maxChunkSize/3);
        tempBuff.order(ByteOrder.LITTLE_ENDIAN);

        isRecording = true;

        BASS.BASS_ChannelPlay(recHandle, false);

        // BASS.BASS_ChannelPlay(mixer, false);
        updateTrackAttributes(true);
        state = PlayerState.Playing;

        context.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                for (AudioPlayerListener listener : audioPlayerListeners)
                {
                    listener.onStartRecording();
                }
            }
        });
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

            // it is a recording track. Leave it alone!
            if(recorderTrack != null && trackInfo.getTrack() == recorderTrack.getTrack()) continue;

            if(trackInfo.track.isMuted())
            {
                BASS.BASS_ChannelSetAttribute(trackInfo.getChannel(), BASS.BASS_ATTRIB_VOL, 0);
            }
            else BASS.BASS_ChannelSetAttribute(trackInfo.getChannel(), BASS.BASS_ATTRIB_VOL, trackInfo.track.getGain());

            // soon will be there
            BASS.BASS_ChannelSetAttribute(trackInfo.getChannel(), BASS.BASS_ATTRIB_PAN, trackInfo.track.getPan());

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
        double position = 0;
        // if we are recording sound, get data based on that track!
        if(isRecording && recorderTrack != null)
        {
            position = recorderTrack.track.samplesToTime(recorderTrack.currentSample);
        }
        else // based on longest track
        {
            final TrackInfo longestTrack = findLongestTrack();
            if (longestTrack == null) return 0;
            //double position = BASS.BASS_ChannelBytes2Seconds(longestTrack.getChannel(), BASS.BASS_ChannelGetPosition(longestTrack.getChannel(), BASS.BASS_POS_BYTE));
            position = longestTrack.track.samplesToTime(longestTrack.currentSample);
        }
        return position;
    }

    @Override
    public void setPosition(double position)
    {
        if(state == PlayerState.NotInitialized || state == PlayerState.Stopped) return;

        // cannot change position during recording
        if(isRecording) return;

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
            int channel = 0;

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
        int channel = 0;

        trackInfo.setChannel(channel);

        tracks.add(trackInfo);
        trackInfo.track.addListener(trackListener);


        initialize(false);
    }

    public ArrayList<TrackInfo> getTracks()
    {
        return tracks;
    }

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
        stop(false);

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
            TrackInfo trackInfo = tracks.get(i); // new TrackInfo(0, track, track.timeToSamples(start));
            AudioInfo audioInfo = trackInfo.track.getInfo();

            // leave recorder track alone
            if(recorderTrack != null && trackInfo.getTrack() == recorderTrack.getTrack()) continue;

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

    // --------------------------------- Simple getters/setters----------------------
    public boolean isRecording()
    {
        return isRecording;
    }

    public TrackInfo getRecorderTrack()
    {
        return recorderTrack;
    }
    // --------------------------------- Simple getters/setters end----------------------

    private ByteBuffer tempBuff = ByteBuffer.allocateDirect(AudioSequence.maxChunkSize);

    protected class RecordProc implements BASS.RECORDPROC
    {
        @Override
        public boolean RECORDPROC(int handle, final ByteBuffer buffer, final int length, Object user)
        {
            try
            {
                buffer.order(ByteOrder.LITTLE_ENDIAN);

                //final float []fbuf = new float[length * 2];
                final int floatLen = length / 2;
                final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(floatLen*4);
                //byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                for (int a = 0; a < length/2; a++)
                {
                    float num = (float)buffer.getShort()/ 32768.f;
                    byteBuffer.putFloat(num);
                    //Log.e("Sample: ", num+"");
                }

                /*final int floatLen = length * 2;
                ByteBuffer temp = ByteBuffer.allocateDirect(floatLen*4);
                //temp.order(ByteOrder.LITTLE_ENDIAN);

                final float []fbuf = new float[floatLen];
                for (int a = 0; a < length/2; a++)
                //for (int a = length/2-1; a >= 0; a--)
                {
                    float num = buffer.getShort(a) / 32768.f;
                    fbuf[a] = num;

                    temp.putFloat(num);
                    //Log.e("Sample: ", num+"");
                }

                temp.order(ByteOrder.LITTLE_ENDIAN);

                temp.rewind();
                FloatBuffer ibuffer=temp.asFloatBuffer();
                float[] floatBuff=new float[floatLen];// allocate a "float" array for the sample data
                ibuffer.get(floatBuff);


                /*FloatBuffer floatBufferBuffer = buffer.asFloatBuffer();
                float[] floatBuff=new float[length/4];// allocate a "float" array for the sample data
                floatBufferBuffer.get(floatBuff);


                ByteBuffer res = ByteBuffer.allocateDirect(floatLen*4);
                for(int i = 0; i < floatBuff.length; i++)
                    res.putFloat(floatBuff[i]);*/
                if(byteBuffer.limit() < tempBuff.limit() - tempBuff.position())
                {
                    byteBuffer.rewind();
                    tempBuff.put(byteBuffer);
                }
                else
                {
                    appender.addToQueue(new TrackAppender.BufferData(tempBuff, tempBuff.position()/4));
                    //recorderTrack.getTrack().getClips().get(0).append(tempBuff, recorderTrack.currentSample);

                    tempBuff = ByteBuffer.allocateDirect(AudioSequence.maxChunkSize/3);
                    tempBuff.order(ByteOrder.LITTLE_ENDIAN);
                    tempBuff.put(byteBuffer);
                }

                recorderTrack.currentSample += floatLen;

                //recorderTrack.getTrack().getClips().get(0).append(byteBuffer, floatLen);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    protected class EndSync implements BASS.SYNCPROC
    {
        public void SYNCPROC(int handle, int channel, int data, Object user)
        {
            TrackInfo track = (TrackInfo)user;
            Log.e("TIME", "END! " + track.track.name);
            final TrackInfo longestTrack = findLongestTrack();

            TrackInfo trackInfo = (TrackInfo)user;

            if(!isRecording && trackInfo == longestTrack)
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
    } // class
}

// appends tracks in background
class TrackAppender implements Runnable
{
    private static final String LOG_TAG = "Track Appender";

    private Queue<BufferData> bufferToAppend = new LinkedList<>();

    private WaveTrack track;

    private Handler handler = new Handler();

    private Thread workingThread;
    // is thread running
    private boolean isRunning = false;
    // tells the thread to stop
    private boolean shouldStop = false;

    public TrackAppender(WaveTrack track)
    {
        this.track = track;
    }

    public void setTrack(WaveTrack track)
    {
        this.track = track;
    }

    public synchronized void addToQueue(BufferData... buffers)
    {
        for (BufferData b : buffers)
            bufferToAppend.add(b);

        // start new thread in case it's not alive
        if (!isRunning)
            start();
    }

    private void start()
    {
        shouldStop = false;
        // create a new thread in case it's not running
        if (!isRunning)
        {
            workingThread = new Thread(this);
            workingThread.setPriority(Thread.MAX_PRIORITY);
            workingThread.start();
        }
    }

    public void stop()
    {
        shouldStop = true;
    }

    int count = 0;

    // Don not call this method directly!!!!
    @Override
    public void run()
    {
        isRunning = true;

        BufferData buffer;
        // go through the queue
        while((buffer = bufferToAppend.poll()) != null)
        {
            Date startDate = new Date();
            try
            {
                track.getClips().get(0).append(buffer.byteBuffer, buffer.len);
            }
            catch (IOException | ClassNotFoundException e)
            {
                e.printStackTrace();
            }

            if(shouldStop)
                break;

            Date endDate = new Date();
            long difference = endDate.getTime() - startDate.getTime();

            //Log.e("TIME", Helper.millisecondsToSeconds(difference)+"");

            //Log.e(LOG_TAG, "Iter: " + count++ + "");
        }

        isRunning = false;
    }

    public static class BufferData
    {
        ByteBuffer byteBuffer;
        int len;

        public BufferData(ByteBuffer byteBuffer, int len)
        {
            this.byteBuffer = byteBuffer;
            this.len = len;
        }
    }
}