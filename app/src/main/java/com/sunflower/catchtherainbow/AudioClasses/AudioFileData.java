package com.sunflower.catchtherainbow.AudioClasses;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.support.v4.content.res.TypedArrayUtils;

import com.sunflower.catchtherainbow.Helper;
import com.un4seen.bass.BASS;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by SuperComputer on 2/25/2017.
 */

public class AudioFileData
{
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
    public void readFile(String path)
    {
        handle = BASS.BASS_StreamCreateFile(path, 0, 0, BASS.BASS_STREAM_DECODE|BASS.BASS_STREAM_PRESCAN/*|BASS.BASS_SAMPLE_FLOAT*/);

        long len = BASS.BASS_ChannelGetLength(handle, BASS.BASS_POS_BYTE);
        duration = (int)BASS.BASS_ChannelBytes2Seconds(handle, len);

        // size in bytes
        int bufferSize = (int) (len/100);
        long bytesRead = 0;

        //ArrayList<Integer> buffer = new ArrayList<>();

        // read data piece by piece
        while(bytesRead < len)
        {
           // bytesRead += bufferSize * 4;

            ByteBuffer audioData = ByteBuffer.allocateDirect(bufferSize * 4);
            audioData.order(ByteOrder.LITTLE_ENDIAN); // little-endian byte order
            bytesRead += BASS.BASS_ChannelGetData(handle, audioData, bufferSize * 4);
            int[] frameGains = new int[bufferSize/2]; // allocate a "short" array for the sample data
            audioData.asIntBuffer().get(frameGains);

            // append gains array
            Integer[]tmpGains = Helper.getNormalizedBuffer(frameGains);

            Collections.addAll(mFrameGains, tmpGains);

           // int[]oldGains = Arrays.copyOf(mFrameGains, mFrameGains.length);

            //mFrameGains = new int[tmpGains.length + oldGains.length];

            //System.arraycopy(oldGains, 0, mFrameGains, 0, oldGains.length);
            //System.arraycopy(tmpGains, 0, mFrameGains, oldGains.length, tmpGains.length);

            // notify about progress
            if(listener != null)
                listener.onProgressUpdate((int) ((float)bytesRead / len * 100));
        }

        long trackLengthInBytes = BASS.BASS_ChannelGetLength(handle, BASS.BASS_POS_BYTE);
        long frameLengthInBytes = BASS.BASS_ChannelSeconds2Bytes(handle, 0.02d);
        this.mNumFrames = (int) Math.round(1f * trackLengthInBytes / frameLengthInBytes);

        /*int partSize = buffer.size() / 100;

        mFrameGains = new Integer[buffer.size()];
        for(int i = 0; i < buffer.size(); i++)
        {
            Integer val = buffer.remove(0);
            mFrameGains[i] = val;
        }*/

        //mFrameGains = buffer.toArray(mFrameGains);

        // extract avg bitrate attribute
        BASS.BASS_ChannelGetAttribute(handle, BASS.BASS_ATTRIB_BITRATE, mAvgBitRate);

        // extract channel info
        BASS.BASS_CHANNELINFO info = new BASS.BASS_CHANNELINFO();
        BASS.BASS_ChannelGetInfo(handle, info);

        this.mGlobalSampleRate = info.freq;
        this.mGlobalChannels = info.chans;
        this.mFileSize = bufferSize;



        // this.mNumFrames = mGlobalSampleRate * mGlobalChannels;
        //BASS.BASS_StreamFree(handle);

        String appPath = context.getApplicationInfo().dataDir + "/" + "test.mo3";
        File file = new File(appPath);
        if(file.exists()) file.delete();
        try
        {
            file.createNewFile();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        //int writerHandle = BASS.BASS_StreamCreateFile(appPath, 0, 0, BASS.BASS_STREAM_DECODE/*|BASS.BASS_STREAM_PRESCAN|BASS.BASS_SAMPLE_FLOAT */);
        //BASS.BASS_StreamPutData(writerHandle, audioData, 0);


        handle = BASS.BASS_MusicLoad(path, 0, 0, BASS.BASS_MUSIC_DECODE, 0);
        // extract channel info
        info = new BASS.BASS_CHANNELINFO();
        BASS.BASS_ChannelGetInfo(handle, info);
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
