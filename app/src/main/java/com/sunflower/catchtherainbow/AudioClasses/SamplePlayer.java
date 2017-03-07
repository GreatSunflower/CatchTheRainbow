package com.sunflower.catchtherainbow.AudioClasses;

import android.app.Activity;
import android.util.Log;

import com.un4seen.bass.BASS;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileLock;

/**
 * Created by SuperComputer on 3/3/2017.
 */

public class SamplePlayer extends SuperAudioPlayer
{
    private static final String LOG_TAG = "Sample Player";
    protected String currentStreamPath = "";
    protected ObjectInputStream inputStream;

    public SamplePlayer(Activity context)
    {
        super(context);
    }

    protected final BASS.BASS_FILEPROCS fileProc = new BASS.BASS_FILEPROCS()
    {
        @Override
        public void FILECLOSEPROC(Object user)
        {
            try
            {
                inputStream.close();
            }
            catch (IOException e)
            {
                Log.e(LOG_TAG, "File close Exception!!!");
                e.printStackTrace();
            }
        }
        @Override
        public long FILELENPROC(Object user)
        {
            // stream has been created
            if (inputStream != null)
            try
            {
                return inputStream.available();
            }
            catch (IOException e)
            {
                Log.e(LOG_TAG, "File length Exception!!!");
                e.printStackTrace();
            }

            return 0L;
        }
        @Override
        public int FILEREADPROC(ByteBuffer buffer, int length, Object user)
        {
            if (inputStream == null)
                return 0;
            try
            {
                // at first we need to create a byte[] with the size of the requested length
                int bytesRead = 0;
                while(bytesRead < length)
                {
                    float value = inputStream.readFloat();
                    buffer.putFloat(value);

                    bytesRead += 4;
                }

                //Log.d(LOG_TAG, String.format("FileProcUserRead: requested {0}, read {1} ", length, bytesRead));
                //Log.d(LOG_TAG, buffer.get(0)+"");
                return bytesRead;
            }
            catch(Exception ex)
            {
                Log.e(LOG_TAG, "File Read Exception!!!");
            }
            return 0;
        }
        @Override
        public boolean FILESEEKPROC(long offset, Object user)
        {
            if (inputStream == null)
                return false;
            try
            {
               // inputStream.mark(0);
                //inputStream.skip(4 * 2 + 8);

                //long pos = inputStream.skip(offset);
                return true;
            }
            catch(Exception ex)
            {
                Log.e(LOG_TAG, "File Seek Exception!!!");
                return false;
            }
        }
    };

    protected BASS.STREAMPROC streamProc = new BASS.STREAMPROC()
    {
        @Override
        public int STREAMPROC(int handle, ByteBuffer buffer, int length, Object user)
        {
            if (inputStream == null)
                return 0;
            try
            {
                //buffer.order(ByteOrder.LITTLE_ENDIAN);
                int bytesRead = 0;

                while (bytesRead < length)
                {
                    if(inputStream.available() == 0)
                    {
                        break;
                    }

                    short value = inputStream.readShort();
                    buffer.putShort(value);

                    bytesRead += 2;
                }

                if (bytesRead < length)
                {
                    bytesRead |= BASS.BASS_STREAMPROC_END;
                    inputStream.close();
                    inputStream = null;

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
                Log.e(LOG_TAG, "File Read Exception!!!");
            }
            return 0;
        }
    };

    protected final BASS.SYNCPROC EndSync = new BASS.SYNCPROC()
    {
        public void SYNCPROC(int handle, int channel, int data, Object user)
        {
            try
            {
                inputStream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            isPlaying = false;
            for(AudioPlayerListener listener: audioPlayerListeners)
            {
                listener.onCompleted();
            }
        }
    };

    public void playPause(boolean play)
    {
        if(currentStreamPath == null) return;

        if(play)
        {
            if(inputStream == null)
            {
                createNewStream();
            }
            BASS.BASS_ChannelPlay(channelHandle, false);
            int error = BASS.BASS_ErrorGetCode();
            isPlaying = true;
        }
        else
        {
            BASS.BASS_ChannelPause(channelHandle);
            isPlaying = false;
        }
    }

    @Override
    public void setPosition(int position)
    {
        if(inputStream != null)
        {
            BASS.BASS_ChannelSetPosition(channelHandle,  BASS.BASS_ChannelSeconds2Bytes(channelHandle, position), BASS.BASS_POS_BYTE);
            try
            {
                stop();
                createNewStream();
                inputStream.skipBytes(4*2+8);
                int bytesToSkip = (int) BASS.BASS_ChannelSeconds2Bytes(channelHandle, position);
                long skipped = inputStream.skipBytes(bytesToSkip);
                Log.e(LOG_TAG, "Bytes to skip: " + bytesToSkip + ", skipped: " + skipped);
                BASS.BASS_ChannelSetPosition(channelHandle, bytesToSkip, BASS.BASS_POS_BYTE);
                playPause(true);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
        //BASS.BASS_ChannelSetPosition(channelHandle, BASS.BASS_ChannelSeconds2Bytes(channelHandle, position), BASS.BASS_POS_BYTE);
    }

    private int getNearestPowerOfFourNumber(int n)
    {
        if(n == 0)
            return 0;
        while(!isPowerOfFour(n))
        {
            n++;
        }
        return n;
    }

    private boolean isPowerOfFour(int n)
    {
        if(n == 0)
            return false;
        while(n != 1)
        {
            if(n%4 != 0)
                return false;
            n = n/4;
        }
        return true;
    }

    private boolean createNewStream()
    {
        try
        {
            BASS.BASS_ChannelStop(channelHandle);
            BASS.BASS_StreamFree(channelHandle);

            inputStream = new ObjectInputStream(new FileInputStream(currentStreamPath));
            this.sampleRate = inputStream.readInt();
            this.channels = inputStream.readInt();

            long len = inputStream.readLong();

            channelHandle = BASS.BASS_StreamCreate(sampleRate, channels, BASS.BASS_SAMPLE_FLOAT, streamProc, 0);
            //channelHandle = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS.BASS_SAMPLE_FLOAT, fileProc, 0);
            int error = BASS.BASS_ErrorGetCode();

            int totalTime = (int)BASS.BASS_ChannelBytes2Seconds(channelHandle, len);

            for(AudioPlayerListener listener: audioPlayerListeners)
            {
                listener.onInitialized(totalTime);
            }
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public void open(String path, boolean autoPlay) throws IOException
    {
        BASS.BASS_ChannelStop(channelHandle);

        //Free memory
        BASS.BASS_StreamFree(channelHandle);

        currentStreamPath = path;
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
        BASS.BASS_ChannelSetSync(channelHandle, BASS.BASS_STREAMPROC_END, 0, EndSync, 0);

        if(autoPlay)
            playPause(true);
    }


}
