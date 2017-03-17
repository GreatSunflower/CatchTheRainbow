package com.sunflower.catchtherainbow.AudioClasses;

import android.content.Intent;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * Created by SuperComputer on 3/17/2017.
 * Manages audio chunks.
 */

public class FileManager implements Serializable
{
    private String samplesFolder;

    private Long currentFileCount = 0L;

    // keeps references for each audio chunk
    HashMap<String, Integer> audioChunks = new HashMap<>();

    public FileManager(String samplesFolder)
    {
        this.samplesFolder = samplesFolder;
    }

    public AudioChunk createAudioChunk(ByteBuffer sampleData, int len, AudioInfo format) throws IOException
    {
        String fileName = samplesFolder + "/" + currentFileCount + ".ac";

        AudioChunk chunk = new AudioChunk(fileName, len, format);
        chunk.writeToDisk(sampleData.array(), len);

        audioChunks.put(fileName, 0);

        currentFileCount++;

        return chunk;
    }

    private int getFileRefCount(String filename)
    {
        int res = 0;

        if(filename.contains(filename))
            res = audioChunks.get(filename);

        return res;
    }


}
