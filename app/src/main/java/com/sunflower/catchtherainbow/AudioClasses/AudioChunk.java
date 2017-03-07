package com.sunflower.catchtherainbow.AudioClasses;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by SuperComputer on 3/6/2017.
 */

// passed from track
class AudioInfo implements Serializable
{
    public AudioInfo(int sampleRate, int channels)
    {
        this.sampleRate = sampleRate;
        this.channels = channels;
    }

    int sampleRate = 44100;
    int channels = 2;
}

//header of the file
class ChunkHeader implements Serializable
{
    AudioInfo info;
    int samplesCount;

    public ChunkHeader(AudioInfo info, int samplesCount)
    {
        this.info = info;
        this.samplesCount = samplesCount;
    }
}
public class AudioChunk implements Serializable
{
    // location of the file
    private String path;
    // number of samples it contains
    private int samplesCount;
    private AudioInfo audioInfo;

    // constr
    public AudioChunk(String path, int samplesCount, AudioInfo audioInfo)
    {
        this.path = path;
        this.samplesCount = samplesCount;
        this.audioInfo = audioInfo;
    }

    public boolean writeToDisk(byte []buffer, int samplesLen) throws IOException
    {
        if(buffer == null) return false;

        // buffer for outputting in ctr format
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(path));

        ChunkHeader header = new ChunkHeader(audioInfo, samplesLen);
        // write header
        outputStream.writeObject(header);

        // write sample data
        outputStream.write(buffer);

        // write all of the data
        outputStream.flush();

        // close the steam
        outputStream.close();

        return true;
    }

    // read samples from file and puts them into buffer array
    // @param buffer - where we shall put samples
    // @param start - first sample to get
    // @param length - number of samples after start
    // @return number of samples read or -1 if the was an error
    public int readToBuffer(byte []buffer, int start, int length)
    {
        // NOt tested yet!!!
        try
        {
            FileInputStream fileInputStream = new FileInputStream(path);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            // first read the header
            ChunkHeader header = (ChunkHeader)objectInputStream.readObject();

            //objectInputStream.skipBytes(start * 4/* Size of one sample */);

            int read = objectInputStream.read(buffer, start * 4, length/4);

            objectInputStream.close();

            return read / 4;
        }
        catch (IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        return -1;
    }

}
