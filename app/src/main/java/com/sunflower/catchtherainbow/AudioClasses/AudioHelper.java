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

        if(info.format.sampleSize == 4)
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
        byte[] lastChunkBuffer = new byte[(int) lastChunkSamplesCount];
        ByteBuffer lastChunkWrapper = ByteBuffer.wrap(lastChunkBuffer);
        chunk.readToBuffer(lastChunkWrapper, 0, (int)lastChunkSamplesCount);

        // data to add
        byte[] addBuffer = new byte[addLen];
        orig.get(addBuffer, 0, addLen/*/info.format.sampleSize*/);

        // put it all together
        ByteBuffer chunkBuffer = ByteBuffer.allocateDirect(addLen*info.format.sampleSize);
        chunkBuffer.put(lastChunkBuffer);
        chunkBuffer.put(addBuffer);

        final int newLastChunkLen = (int) (lastChunkSamplesCount + addLen);

        // save
        chunk.writeToDisk(chunkBuffer, newLastChunkLen);

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
}
