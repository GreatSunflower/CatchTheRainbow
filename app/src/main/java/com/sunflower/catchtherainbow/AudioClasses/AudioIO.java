package com.sunflower.catchtherainbow.AudioClasses;

import android.app.Activity;
import android.util.Log;

import com.sunflower.catchtherainbow.Helper;
import com.un4seen.bass.BASS;
import com.un4seen.bass.BASSmix;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by SuperComputer on 3/3/2017.
 */

public class AudioIO extends SuperAudioPlayer
{
    private static final String LOG_TAG = "AudioIO";
    protected String currentStreamPath = "";
    protected int mixer;

    private Project project;

    public AudioIO(Activity context, Project project)
    {
        super(context);
        this.project = project;
    }


    public void playPause(boolean play)
    {
        if(currentStreamPath == null) return;

        if(play)
        {
            BASS.BASS_ChannelPlay(mixer, false);
            int error = BASS.BASS_ErrorGetCode();
            isPlaying = true;
        }
        else
        {
            BASS.BASS_ChannelPause(mixer);
            isPlaying = false;
        }
    }

    @Override
    public void setPosition(int position)
    {
        //BASS.BASS_ChannelSetPosition(channelHandle, BASS.BASS_ChannelSeconds2Bytes(channelHandle, position), BASS.BASS_POS_BYTE);
    }

    private boolean createNewStream()
    {
        BASS.BASS_ChannelStop(channelHandle);
        BASS.BASS_StreamFree(channelHandle);

        channelHandle = BASS.BASS_StreamCreate(sampleRate, channels, BASS.BASS_SAMPLE_FLOAT, streamProc, 0);
        //channelHandle = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS.BASS_SAMPLE_FLOAT, fileProc, 0);
        int error = BASS.BASS_ErrorGetCode();

        //int totalTime = (int)BASS.BASS_ChannelBytes2Seconds(channelHandle, len);

        for(AudioPlayerListener listener: audioPlayerListeners)
        {
            listener.onInitialized(0);
        }
        return true;
    }


    private int currentSample = 0;
    protected BASS.STREAMPROC streamProc = new BASS.STREAMPROC()
    {
        @Override
        public int STREAMPROC(int handle, ByteBuffer buffer, int length, Object user)
        {
            try
            {
                //buffer.order(ByteOrder.LITTLE_ENDIAN);
                int bytesRead;

                WaveTrack track = (WaveTrack)user;
                bytesRead = track.get(buffer, currentSample, length / track.getInfo().format.sampleSize) / track.getInfo().format.sampleSize;

                currentSample+=bytesRead;

                if (bytesRead == -1 || bytesRead < length)
                {
                    bytesRead |= BASS.BASS_STREAMPROC_END;

                    isPlaying = false;
                    for(AudioPlayerListener listener: audioPlayerListeners)
                    {
                        listener.onCompleted();
                    }
                    return BASS.BASS_STREAMPROC_END;
                }

                //Log.d(LOG_TAG, String.format("FileProcUserRead: requested {i}, read {i} ", length, bytesRead));
                return bytesRead;
            }
            catch(Exception ex)
            {
                Log.e(LOG_TAG, "File Read Exception! " + ex.getMessage());
                currentSample = 0;
                playPause(false);
            }
            return 0;
        }
    };

    protected final BASS.SYNCPROC EndSync = new BASS.SYNCPROC()
    {
        public void SYNCPROC(int handle, int channel, int data, Object user)
        {

            isPlaying = false;
            for(AudioPlayerListener listener: audioPlayerListeners)
            {
                listener.onCompleted();
            }
        }
    };


    @Override
    public void open(String path, final boolean autoPlay) throws IOException
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                BASS.BASS_ChannelStop(mixer);

                //Free memory
                BASS.BASS_StreamFree(mixer);

                AudioInfo info = project.getProjectAudioInfo();

                mixer = BASSmix.BASS_Mixer_StreamCreate(info.sampleRate, info.channels, BASS.BASS_SAMPLE_FLOAT|BASSmix.BASS_MIXER_END /*|BASS.BASS_STREAM_AUTOFREE|BASSmix.BASS_MIXER_BUFFER*/);
               /* BASS.BASS_SetConfig(BASS.BASS_CONFIG_UPDATEPERIOD, 20);
                BASS.BASS_SetConfig(BASS.BASS_CONFIG_BUFFER, 100);
                BASS.BASS_SetConfig(BASSmix.BASS_CONFIG_MIXER_BUFFER, 5);
                BASS.BASS_SetConfig(BASSmix.BASS_CONFIG_MIXER_POSEX, 5000);*/

                for(WaveTrack track: project.getTracks())
                {
                    int channel = BASS.BASS_StreamCreate(track.getInfo().sampleRate, track.getInfo().channels, BASS.BASS_STREAM_DECODE|BASS.BASS_SAMPLE_FLOAT, streamProc, track);

                    /*int size = 0;
                    for(AudioChunk c: project.getFileManager().getAudioChunks().keySet())
                    {
                        size += c.getSamplesCount();
                    }
                    ByteBuffer buffer = ByteBuffer.allocateDirect(size);
                    for(AudioChunk c: project.getFileManager().getAudioChunks().keySet())
                    {
                        c.readToBuffer(buffer, 0, c.getSamplesCount());
                    }
                    int channel = BASS.BASS_StreamCreateFile(buffer, 0, 0, BASS.BASS_STREAM_DECODE|BASS.BASS_SAMPLE_FLOAT);*/

                    boolean isOk = BASSmix.BASS_Mixer_StreamAddChannel(mixer, channel, BASSmix.BASS_MIXER_BUFFER);
                    //if(!isOk) throw new IOException("Error adding a channel!!!");
                }


                //createNewStream();

                /*channelHandle = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, 0, fileProc, 0);
                //channelHandle = BASS.BASS_StreamCreate(sampleRate, channels, BASS.BASS_SAMPLE_FLOAT|BASS.BASS_STREAM_AUTOFREE, streamProc, 0);
                int error = BASS.BASS_ErrorGetCode();
                long bytes = BASS.BASS_ChannelGetLength(channelHandle, BASS.BASS_POS_BYTE);
                int totalTime = (int)BASS.BASS_ChannelBytes2Seconds(channelHandle, bytes);

                for(AudioPlayerListener listener: audioPlayerListeners)
                {
                    listener.onInitialized(totalTime);
                }*/

                //Set callback
                BASS.BASS_ChannelSetSync(mixer, BASS.BASS_STREAMPROC_END, 0, EndSync, 0);

                if (autoPlay)
                    playPause(true);
            }
        }).start();
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
