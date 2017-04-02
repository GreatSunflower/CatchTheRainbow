package com.sunflower.catchtherainbow.AudioClasses;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

/**
 * Created by SuperComputer on 3/6/2017.
 */

//header of the file
class ChunkHeader implements Serializable
{
    AudioInfo info;
    int samplesCount;
    float []summary64K;
    float []summary256;

    public ChunkHeader(AudioInfo info, int samplesCount)
    {
        this.info = info;
        this.samplesCount = samplesCount;
    }
}
public class AudioChunk implements Serializable
{
    // location of the file
    protected String path;
    // number of samples it contains
    protected int samplesCount;
    protected AudioInfo audioInfo;

    // summary
    protected float min = 0, max = 0, rms = 0;

    protected int bytesPerFrame;

    protected int frames64K;
    protected int frames256;
    protected int totalSummaryBytes;

    // constr
    public AudioChunk(String path, int samplesCount, AudioInfo audioInfo)
    {
        this.path = path;
        this.samplesCount = samplesCount;
        this.audioInfo = audioInfo;

        bytesPerFrame = 4*4;/*size of float 3/*firlds*/;

        frames64K = (samplesCount + 65535) / 65536;
        frames256 = frames64K * 256;

        //int offset64K = headerTagLen;
        //int offset256 = offset64K + (frames64K * bytesPerFrame);
        totalSummaryBytes = (frames64K * bytesPerFrame) + (frames256 * bytesPerFrame);
    }

    /// Construct a SimpleBlockFile memory structure that will point to an
    /// existing chunk file.  This file must exist and be a valid block file.
    ///
    /// @param existingFile The disk file this SimpleBlockFile should use.
    public AudioChunk(String existingFile, int len, float min, float max, float rms)
    {
        this.path = path;
        this.samplesCount = samplesCount;
        this.audioInfo = audioInfo;

        bytesPerFrame = 4*4;;

        frames64K = (samplesCount + 65535) / 65536;
        frames256 = frames64K * 256;

        //int offset64K = headerTagLen;
        //int offset256 = offset64K + (frames64K * bytesPerFrame);
        totalSummaryBytes = (frames64K * bytesPerFrame) + (frames256 * bytesPerFrame);

        this.min = min;
        this.max = max;
        this.rms = rms;
    }


    public boolean writeToDisk(ByteBuffer buffer, int samplesLen) throws IOException
    {
        if(buffer == null) return false;

        FileOutputStream fileOutputStream = new FileOutputStream(path);
        // buffer for outputting in ctr format
        ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);

        ChunkHeader header = new ChunkHeader(audioInfo, samplesLen);
        header.summary64K = new float[frames64K * bytesPerFrame];

        header.summary256 = new float[((frames64K * bytesPerFrame) + (frames256 * bytesPerFrame))];

        calcSummary(buffer, samplesLen, audioInfo, header.summary64K, header.summary256);

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
    public int readToBuffer(ByteBuffer buffer, long start, long length)
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
            objectInputStream.skipBytes((int) (start * sampleSize));
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


        /// Create a copy of this BlockFile, but using a different disk file.
    ///
    /// @param newFileName The name of the NEW file to use.
    AudioChunk copy(String newFileName)
    {
        AudioChunk newBlockFile = new AudioChunk(newFileName, samplesCount, min, max, rms);

        return newBlockFile;
    }

    ChunkHeader readHeader()
    {
        try
        {
            FileInputStream fileInputStream = new FileInputStream(path);

            BufferedInputStream buffStream = new BufferedInputStream(fileInputStream, 4096*2);
            ObjectInputStream objectInputStream = new ObjectInputStream(buffStream);

            // first read the header
            ChunkHeader header = (ChunkHeader)objectInputStream.readObject();

            objectInputStream.close();

            return header;
        }
        catch (IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    boolean read256(ByteBuffer buffer, long start, long len)
    {
        if(start < 0) return false;

        ChunkHeader header = readHeader();

        if(header == null) return false;

        start = Math.min(start, frames256);
        len = Math.min(len, frames256 - start);

        for(int i = (int) start; i < len; i++)
        {
            buffer.putFloat(header.summary256[i]);
        }

        return true;
    }

    boolean read64(ByteBuffer buffer, long start, long len)
    {
        if(start < 0) return false;

        ChunkHeader header = readHeader();

        if(header == null) return false;

        start = Math.min(start, frames64K);
        len = Math.min(len, frames64K - start);

        for(int i = (int)start; i < len; i++)
        {
            buffer.putFloat(header.summary64K[i]);
        }

        return true;
    }

    void getMinMax(int start, int len, Float outMin, Float outMax, Float outRMS)
    {
        ByteBuffer buffer = ByteBuffer.allocateDirect(len * 4);
        this.readToBuffer(buffer, start, len);
        float []floatBuffer = new float[len];
        buffer.asFloatBuffer().get(floatBuffer);

        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        float sumsq = 0;

        for(int i = 0; i < len; i++ )
        {
            float sample = floatBuffer[i];

            if( sample > max )
                max = sample;
            if( sample < min )
                min = sample;
            sumsq += (sample*sample);
        }

        outMin = min;
        outMax = max;
        outRMS = (float)Math.sqrt(sumsq/len);
    }

    void getMinMax(Float outMin, Float outMax, Float outRMS)
    {
        outMin = this.min;
        outMax = this.max;
        outRMS = this.rms;
    }

    void calcSummary(ByteBuffer buffer, int len, AudioInfo format, float[]summary64K, float[]summary256)
    {
      // float []summary64K = (float [])(fullSummary.get() + mSummaryInfo.offset64K);
      // float []summary256 = (float [])(fullSummary.get() + mSummaryInfo.offset256);

        float []fbuffer = new float[len];
        buffer.rewind();
        buffer.asFloatBuffer().get(fbuffer);

        calcSummaryFromBuffer(fbuffer, len, summary256, summary64K);
    }

    void calcSummaryFromBuffer(float []fbuffer, int len, float []summary256, float []summary64K)
    {
        int sumLen;

        float min, max;
        float sumsq;
        double totalSquares = 0.0;
        double fraction = 0.0;

        // Recalc 256 summaries
        sumLen = (len + 255) / 256;
        int summaries = 256;

        if(sumLen*3 > summary256.length)
        {
            sumLen = (len + 255) / 256;
        }

        for (int i = 0; i < sumLen; i++)
        {
            min = fbuffer[i * 256];
            max = fbuffer[i * 256];
            sumsq = ((float)min) * ((float)min);
            int jcount = 256;
            if (jcount > len - i * 256)
            {
                jcount = len - i * 256;
                fraction = 1.0 - (jcount / 256.0);
            }
            for (int j = 1; j < jcount; j++)
            {
                float f1 = fbuffer[i * 256 + j];
                sumsq += f1 * f1;
                if (f1 < min)
                    min = f1;
                else if (f1 > max)
                    max = f1;
            }

            totalSquares += sumsq;
            float rms = (float)Math.sqrt(sumsq / jcount);

            summary256[i * 3] = min;
            summary256[i * 3 + 1] = max;
            summary256[i * 3 + 2] = rms;  // The rms is correct, but this may be for less than 256 samples in last loop.
        }
        for (int i = sumLen; i < frames256; i++)
        {
            // filling in the remaining bits with non-harming/contributing values
            // rms values are not "non-harming", so keep  count of them:
            summaries--;
            summary256[i * 3] = Float.MAX_VALUE;  // min
            summary256[i * 3 + 1] = Float.MIN_VALUE;   // max
            summary256[i * 3 + 2] = 0.0f; // rms
        }

        // Calculate now while we can do it accurately
        this.rms = (float)Math.sqrt(totalSquares/len);

        // Recalc 64K summaries
        sumLen = (len + 65535) / 65536;

        for (int i = 0; i < sumLen; i++)
        {
            min = summary256[3 * i * 256];
            max = summary256[3 * i * 256 + 1];
            sumsq = (float)summary256[3 * i * 256 + 2];
            sumsq *= sumsq;
            for (int j = 1; j < 256; j++)
            {   // we can overflow the useful summary256 values here, but have put non-harmful values in them
                if (summary256[3 * (i * 256 + j)] < min)
                    min = summary256[3 * (i * 256 + j)];
                if (summary256[3 * (i * 256 + j) + 1] > max)
                    max = summary256[3 * (i * 256 + j) + 1];
                float r1 = summary256[3 * (i * 256 + j) + 2];
                sumsq += r1*r1;
            }

            double denom = (i < sumLen - 1) ? 256.0 : summaries - fraction;
            float rms = (float)Math.sqrt(sumsq / denom);

            summary64K[i * 3] = min;
            summary64K[i * 3 + 1] = max;
            summary64K[i * 3 + 2] = rms;
        }
        for (int i = sumLen; i < frames64K; i++)
        {
            summary64K[i * 3] = 0.0f;  // probably should be FLT_MAX, need a test case
            summary64K[i * 3 + 1] = 0.0f; // probably should be -FLT_MAX, need a test case
            summary64K[i * 3 + 2] = 0.0f; // just padding
        }

        // Recalc block-level summary (mRMS already calculated)
        min = summary64K[0];
        max = summary64K[1];

        for (int i = 1; i < sumLen; i++)
        {
            if (summary64K[3*i] < min)
                min = summary64K[3*i];
            if (summary64K[3*i+1] > max)
                max = summary64K[3*i+1];
        }

        this.min = min;
        this.max = max;
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