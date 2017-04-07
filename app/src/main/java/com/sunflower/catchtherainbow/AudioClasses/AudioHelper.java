package com.sunflower.catchtherainbow.AudioClasses;

import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by SuperComputer on 3/16/2017.
 * Manages audio chunks.
 */

public class AudioHelper
{
    public static boolean copySamples(ByteBuffer dest, ByteBuffer orig, int offset, int len, AudioInfo info)
    {
        if(dest == null || orig == null)
            return false;

   //     byte[] buff = new byte[len];
//        orig.get(buff, offset, len);

        orig.rewind();
        if(info.format.getSampleSize() == 4)
        {
            float[] buff = new float[len];
            orig.asFloatBuffer().get(buff, offset, len);
            for(int i = 0; i < buff.length; i++)
            {
                dest.putFloat(buff[i]);
            }
        }

  //      dest.put(buff);

        return true;
    }

    public static boolean appendSamples(ByteBuffer dest, ByteBuffer orig, int offset, int len, AudioInfo info)
    {
        return copySamples(dest, orig, offset, len, info);
    }

    public static boolean appendChunk(AudioChunk chunk, ByteBuffer orig, int addLen, AudioInfo info) throws IOException
    {
        if(chunk == null || orig == null)
            return false;

        long lastChunkSamplesCount = chunk.getSamplesCount();

        // original data
       // byte[] lastChunkBuffer = new byte[(int) lastChunkSamplesCount*info.format.getSampleSize()];
        ByteBuffer lastChunkWrapper = ByteBuffer.allocateDirect((int) lastChunkSamplesCount*info.format.getSampleSize());
        chunk.readToBuffer(lastChunkWrapper, 0, (int)lastChunkSamplesCount);
        lastChunkWrapper.rewind();
      //  lastChunkWrapper.get(lastChunkBuffer);

        // data to add
        byte[] addBuffer = new byte[addLen*info.format.getSampleSize()];
        orig.rewind();
        orig.get(addBuffer, 0, addLen * info.format.getSampleSize());

        int newLen = (int) ((addLen + lastChunkSamplesCount) * info.format.getSampleSize());
        // put it all together
        ByteBuffer chunkBuffer = ByteBuffer.allocateDirect(newLen);
        chunkBuffer.put(lastChunkWrapper);
        //orig.rewind();
        chunkBuffer.put(addBuffer);

        final int newLastChunkSamplesLen = (int) (lastChunkSamplesCount + addLen);

        /*byte test[] = new byte[newLen];
        chunkBuffer.rewind();
        chunkBuffer.get(test);*/

        // save
        chunk.writeToDisk(chunkBuffer, newLastChunkSamplesLen);

        return true;
    }

    public static boolean clearSamples(ByteBuffer dest, int offset, int len, AudioInfo info)
    {
        if(dest == null)
            return false;

        for(int i = 0; i < len; i++)
            dest.putFloat(0);

        return true;
    }

    public static int limitSampleBufferSize(int bufferSize, int limit)
    {
        return Math.min(bufferSize, Math.max(0, limit));
    }

    public static long limitSampleBufferSize(long bufferSize, long limit)
    {
        return Math.min(bufferSize, Math.max(0, limit));
    }


    public static long timeToSamples(double time, AudioInfo info)
    {
        return (long) Math.floor(time * info.sampleRate * info.channels + 0.5);
    }
    /** @brief Convert correctly between an number of samples and an (absolute) time in seconds.
     *
     * @param pos The time number of samples from the start of the track to convert.
     * @return The time in seconds.
     */
    public static double samplesToTime(long pos, AudioInfo info)
    {
        return pos / (double)info.sampleRate / info.channels;
    }
}
