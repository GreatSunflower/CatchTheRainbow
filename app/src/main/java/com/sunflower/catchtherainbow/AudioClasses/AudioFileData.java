package com.sunflower.catchtherainbow.AudioClasses;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.sunflower.catchtherainbow.Helper;
import com.un4seen.bass.BASS;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by SuperComputer on 2/25/2017.
 */

enum SampleFormat
{
    int_8Bit,
    int_16Bit,
    float_32Bit
}

public class AudioFileData
{
    private static final String LOG_TAG = "Reader";
    // Member variables representing frame data
    private int mNumFrames;
    private ArrayList<Integer> mFrameGains = new ArrayList<Integer>();
    private int mFileSize;
    private float mAvgBitRate;
    private int mGlobalSampleRate;
    private int mGlobalChannels;
    private int duration = 0; // ms

    private int handle;

    private AudioFileProgressListener listener;

    private Context context;

    public AudioFileData(Context context)
    {
         this.context = context;
    }

    // read a file from disk
    public boolean readFile(String path) throws IOException
    {
        handle = BASS.BASS_StreamCreateFile(path, 0, 0, BASS.BASS_STREAM_DECODE|BASS.BASS_SAMPLE_FLOAT);

        long len = BASS.BASS_ChannelGetLength(handle, BASS.BASS_POS_BYTE);
        duration = (int)BASS.BASS_ChannelBytes2Seconds(handle, len);

        // extract channelHandle info
        BASS.BASS_CHANNELINFO info = new BASS.BASS_CHANNELINFO();
        BASS.BASS_ChannelGetInfo(handle, info);

        this.mGlobalSampleRate = info.freq;
        // number of channels
        this.mGlobalChannels = info.chans;

        // make sure that project directory is created
        Helper.checkDirectory(Environment.getExternalStorageDirectory().toString() + "/Catch The Rainbow");
        // path to decoded audio file
        String trackDirectory = /*context.getApplicationInfo().dataDir*/ Environment.getExternalStorageDirectory().toString() + "/Catch The Rainbow" + "/WaveTrackTest/";
        boolean created = Helper.createOrRecreateDir(trackDirectory);

        // no directory
        if(!created)
        {
            Log.e(LOG_TAG, "Track directory is in a lot of trouble!");
            return false;
        }

        // size in bytes
        int bufferSize = 1048576*4;
        long totalBytesRead = 0;

        // number of files
        int fileCount = 0;

        ArrayList<AudioChunk>audioChunks = new ArrayList<>();

        // read data piece by piece
        while(totalBytesRead < len)
        {
            ByteBuffer audioData = ByteBuffer.allocateDirect(bufferSize);
            //audioData.order(ByteOrder.LITTLE_ENDIAN); // little-endian byte order
            int bytesRead = BASS.BASS_ChannelGetData(handle, audioData, bufferSize);

          //  short[] frameGains = new short[bufferSize/2]; // allocate an array for the sample data
          //  audioData.asShortBuffer().get(frameGains);

            totalBytesRead += bytesRead;

            byte buffer[] = new byte[bufferSize/2];
            audioData.get(buffer);

            // append file with new data
           /* for(int i = 0; i < frameGains.length; i++)
            {
                outputStream.writeShort(frameGains[i]);
            }*/

            String filePath = trackDirectory + "/" + fileCount + ".ac";
            AudioChunk chunk = new AudioChunk(filePath, buffer.length, new AudioInfo(44100, 2));
            chunk.writeToDisk(buffer, buffer.length);

            audioChunks.add(chunk);

            fileCount++;

            // notify about progress
            if(listener != null)
                listener.onProgressUpdate((int) ((float)totalBytesRead / len * 100));
        }

        Log.i(LOG_TAG, "Audio chunks created: " + audioChunks.size());

        long trackLengthInBytes = BASS.BASS_ChannelGetLength(handle, BASS.BASS_POS_BYTE);
        long frameLengthInBytes = BASS.BASS_ChannelSeconds2Bytes(handle, 0.02d);
        this.mNumFrames = (int) Math.round(1f * trackLengthInBytes / frameLengthInBytes);


        //mFrameGains = buffer.toArray(mFrameGains);
        // this.mNumFrames = mGlobalSampleRate * mGlobalChannels;
        //BASS.BASS_StreamFree(handle);

        //int writerHandle = BASS.BASS_StreamCreateFile(appPath, 0, 0, BASS.BASS_STREAM_DECODE/*|BASS.BASS_STREAM_PRESCAN|BASS.BASS_SAMPLE_FLOAT */);
        //BASS.BASS_StreamPutData(writerHandle, audioData, 0);

       // handle = BASS.BASS_MusicLoad(path, 0, 0, BASS.BASS_MUSIC_DECODE, 0);
        // extract channelHandle info
        //info = new BASS.BASS_CHANNELINFO();
        //BASS.BASS_ChannelGetInfo(handle, info);

        return true;
    }

    /// Retrieves the minimum, maximum, and maximum RMS of the
/// specified sample data in this block.
    void getMinMax(short[] buff, MinMax mm)
    {
        float min = mm.min;
        float max = mm.max;
        float sumsq = 0;

        for(int i = 0; i < buff.length; i++)
        {
            float sample = buff[i];

            if( sample > max )
                max = sample;
            if( sample < min )
                min = sample;
            sumsq += (sample*sample);
        }

        mm.min = min;
        mm.max = max;
        mm.RMS = (float) Math.sqrt(sumsq/buff.length);
    }

    public AudioFileProgressListener getListener()
    {
        return listener;
    }

    public void setListener(AudioFileProgressListener listener)
    {
        this.listener = listener;
    }

    class MinMax
    {
        float min = Float.MAX_VALUE, max = Float.MIN_VALUE, RMS = Float.MIN_VALUE;
    }

    public int getNumFrames()
    {
        return mNumFrames;
    }

    public int getSamplesPerFrame()
    {
        return 1024; //mGlobalSampleRate / mNumFrames; //mGlobalSampleRate / 1152 ; // fps
    }

    public ArrayList<Integer> getFrameGains()
    {
        return mFrameGains;
    }

    public float getAvgBitrateKbps()
    {
        return mGlobalSampleRate * mGlobalChannels * 2 / 1024;// mAvgBitRate;
    }

    public int getSampleRate()
    {
        return mGlobalSampleRate;
    }

    public int getChannels()
    {
        return mGlobalChannels;
    }

    public int getSeekableFrameOffset(int frame) {
        return -1;
    }

    public void setFrameGains(ArrayList<Integer> mFrameGains)
    {
        this.mFrameGains = mFrameGains;
    }

    public interface AudioFileProgressListener
    {
        void onProgressUpdate(int progress);
    }
}
