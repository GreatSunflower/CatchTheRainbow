package com.sunflower.catchtherainbow.AudioClasses;

import android.content.Context;
import android.media.AudioFormat;

import com.sunflower.catchtherainbow.Helper;
import com.un4seen.bass.BASS;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Created by SuperComputer on 2/25/2017.
 */

public class AudioFileData
{
    // Member variables representing frame data
    private int mNumFrames;
    private int[] mFrameGains;
    private int mFileSize;
    private float mAvgBitRate;
    private int mGlobalSampleRate;
    private int mGlobalChannels;

    private int handle;

    public AudioFileData(Context context, String path)
    {
        handle = BASS.BASS_StreamCreateFile(path, 0, 0, BASS.BASS_STREAM_DECODE/*|BASS.BASS_STREAM_PRESCAN|BASS.BASS_SAMPLE_FLOAT */);

        // extract avg bitrate attribute
        BASS.BASS_ChannelGetAttribute(handle, BASS.BASS_ATTRIB_BITRATE, mAvgBitRate);

        // extract channel info
        BASS.BASS_CHANNELINFO info = new BASS.BASS_CHANNELINFO();
        BASS.BASS_ChannelGetInfo(handle, info);

        long len = BASS.BASS_StreamGetFilePosition(handle, BASS.BASS_FILEPOS_END);

        int bufferSize = (int) len;
        ByteBuffer audioData = ByteBuffer.allocateDirect(bufferSize*2);
        audioData.order(ByteOrder.LITTLE_ENDIAN); // little-endian byte order
        BASS.BASS_ChannelGetData(handle, audioData, bufferSize*2);
        byte []frameGains = new byte[bufferSize]; // allocate a "short" array for the sample data
        audioData.get(frameGains);

        mFrameGains = Helper.getNormalizedBuffer(frameGains);

        this.mGlobalSampleRate = info.freq;
        this.mGlobalChannels = info.chans;
        this.mFileSize = bufferSize;
        this.mNumFrames = mGlobalSampleRate * mGlobalChannels;
        audioData.clear();
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

        int writerHandle = BASS.BASS_StreamCreateFile(appPath, 0, 0, BASS.BASS_STREAM_DECODE/*|BASS.BASS_STREAM_PRESCAN|BASS.BASS_SAMPLE_FLOAT */);
        BASS.BASS_StreamPutData(writerHandle, audioData, 0);



        handle = BASS.BASS_MusicLoad(path, 0, 0, BASS.BASS_MUSIC_DECODE, 0);
        // extract channel info
        info = new BASS.BASS_CHANNELINFO();
        BASS.BASS_ChannelGetInfo(handle, info);

        BASS.BASS_MusicFree(handle);
    }

    public int getNumFrames()
    {
        return mNumFrames;
    }

    public int getSamplesPerFrame()
    {
        return 1024; //mGlobalSampleRate / 1152 ; // fps
    }

    public int[] getFrameGains()
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
}
