package com.sunflower.catchtherainbow.AudioClasses;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Date;

/**
 * Created by SuperComputer on 3/6/2017.
 */

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

    public boolean writeToDisk(ByteBuffer buffer, int samplesLen) throws IOException
    {
        if(buffer == null) return false;

        FileOutputStream fileOutputStream = new FileOutputStream(path);
        // buffer for outputting in ctr format
        ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);

        ChunkHeader header = new ChunkHeader(audioInfo, samplesLen);
        // write header
        outputStream.writeObject(header);

        buffer.rewind();
        // test
        /*float []shortBuffer = new float[samplesLen/4];
        buffer.asFloatBuffer().get(shortBuffer);
        for(int i = 0; i < shortBuffer.length; i++)
            outputStream.writeFloat(shortBuffer[i]);*/
       // fileChannel.write(buffer);

        byte[]res = new byte[samplesLen*audioInfo.format.getSampleSize()];
        buffer.get(res);

        // write sample data
        outputStream.write(res);

        // write all of the data
        outputStream.flush();

        // close the steam
        outputStream.close();

        return true;
    }

    // read samples from file and puts them into buffer
    // @param buffer - where we shall put samples
    // @param start - first sample to get
    // @param length - number of samples after start
    // @return number of samples read or -1 if the was an error
    public int readToBuffer(ByteBuffer buffer, int start, int length)
    {
        int sampleSize = audioInfo.format.getSampleSize();

        int read = 0;

        try
        {
            Date startDate = new Date();

            FileInputStream fileInputStream = new FileInputStream(path);

            BufferedInputStream buffStream = new BufferedInputStream(fileInputStream, 4096*2);
            ObjectInputStream objectInputStream = new ObjectInputStream(buffStream);

            // first read the header
            ChunkHeader header = (ChunkHeader)objectInputStream.readObject();

            //objectInputStream.skipBytes(start * 4/* Size of one sample */);


            objectInputStream.skipBytes(start * sampleSize);
           // read = (int) fileChannel.read(new ByteBuffer[]{buffer}, start, length);

            while (read < length * sampleSize)
            {
                if(objectInputStream.available() == 0)
                    break;

                if(sampleSize == 4)
                {
                    float b = objectInputStream.readFloat();

                    buffer.putFloat(b);
                    read += 4;
                }
            }
            /*objectInputStream.skipBytes(start);
            for (int i = 0; i < length; i++)
            {
                if(objectInputStream.available() == 0)
                    break;
                //float b = (float) objectInputStream.readFloat();
               // buffer.putFloat(b);
                float b = objectInputStream.readFloat();
                //buffer[i] = b;
                buffer.putFloat(b);
                read+=4;
            }
            /* byte []arr = new byte[length*sampleSize];
            read = objectInputStream.read(arr, start*sampleSize, length*sampleSize);
            buffer.put(arr);*/

            //int read = objectInputStream.read(buffer, start/* * sampleSize*/, length/*/sampleSize*/);
            //int read = buffer.length;

            objectInputStream.close();

            Date endDate = new Date();
            long difference = endDate.getTime() - startDate.getTime();

            //Log.e("TIME", difference+"" + ", Read: " + (read/sampleSize) + ", Needed: " + length);

            return read;
        }
        catch (IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        return read;
    }

    public String getPath()
    {
        return path;
    }

    public int getSamplesCount()
    {
        return samplesCount;
    }

    public AudioInfo getAudioInfo()
    {
        return audioInfo;
    }
}

// references an existing chunk
class AudioChunkRef extends AudioChunk
{
    protected String refPath;
    protected int refStartSample = 0;

    public AudioChunkRef(String filePath, String refPath, int start, int len, AudioInfo audioInfo)
    {
        super(filePath, len, audioInfo);

        this.refPath = refPath;
        this.refStartSample = start;
    }
}

// references an existing chunk
class SilentChunk extends AudioChunk
{
    protected String refPath;
    protected int refStartSample = 0;

    public SilentChunk(int len)
    {
        super("", len, null);
    }

    public int readToBuffer(ByteBuffer buffer, int start, int length)
    {
        AudioHelper.clearSamples(buffer, 0, length, null);

        return length;
    }
}