package com.sunflower.catchtherainbow.AudioClasses;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
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
    private HashMap<String, AudioChunk> audioChunks = new HashMap<>();

    private ArrayList<String> chunkReferences = new ArrayList<>();

    public FileManager(String samplesFolder)
    {
        this.samplesFolder = samplesFolder;
    }

    public AudioChunk createAudioChunk(ByteBuffer sampleData, int len, AudioInfo format) throws IOException
    {
        String fileName = samplesFolder + "/" + currentFileCount + ".ac";

        AudioChunk chunk = new AudioChunk(fileName, len, format);
        chunk.writeToDisk(sampleData, len);

        audioChunks.put(fileName, chunk);

        currentFileCount++;

        return chunk;
    }

    public AudioChunkRef createChunkRef(String refPath, int start, int len, AudioInfo format)
    {
        String fileName = samplesFolder + "/" + currentFileCount + ".ac";

        if(refPath == null || !new File(refPath).exists()) return null;

        AudioChunkRef refChunk = new AudioChunkRef(fileName, refPath, start, len, format);

        audioChunks.put(refChunk.getPath(), refChunk);
        chunkReferences.add(refPath);

        currentFileCount++;

        return refChunk;
    }

    /*public boolean removeChunkRef(AudioChunk refChunk)
    {
        //String fileName = samplesFolder + "/" + currentFileCount + ".ac";
        if(refChunk == null || !audioChunks.containsKey(refChunk.getPath())) return false;

        long count = audioChunks.get(refChunk.getPath());

        audioChunks.put(refChunk.getPath(), ++count);

        return true;
    }*/

    private long getFileRefCount(String filename)
    {
        long res = 0;

        if(audioChunks.containsKey(filename))
        {
            for(String str: chunkReferences)
            {
                if(str.equals(filename))
                    res++;
            }
        }

        return res;
    }

    public HashMap<String, AudioChunk> getAudioChunks()
    {
        return audioChunks;
    }


}
