package com.sunflower.catchtherainbow.AudioClasses;

import android.util.Log;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;


// manages audio chunks array
public class AudioSequence implements Serializable
{
    private static final String LOG_TAG = "Sequence";

    // max size of audio chunk in bytes
    public static final int maxChunkSize = 2097152;

    // number of samples in audio chunk
    private int minSamples;
    private int maxSamples;

    protected int samplesCount = 0;

    // audio info
    private AudioInfo info = new AudioInfo(44100, 2);

    private FileManager fileManager;

    private ArrayList<AudioChunkPos> chunks = new ArrayList<>();

    public AudioSequence(FileManager manager, AudioInfo info)
    {
        this.fileManager = manager;
        this.info = info;

        minSamples = maxChunkSize / info.format.sampleSize;
        maxSamples = minSamples;
    }

    public boolean copyWrite(ByteBuffer scratch,
                             ByteBuffer buffer, AudioChunkPos chunk,
                             int blockRelativeStart, int len)
    {
        // We don't ever write to an existing block; to support Undo,
        // we copy the old block entirely into memory, dereference it,
        // make the change, and then write the NEW block to disk.

        int length = chunk.getChunk().getSamplesCount();
        if(length > maxSamples || blockRelativeStart + len > length)
            return false;

        int sampleSize = getInfo().format.sampleSize;

        read(scratch, chunk, 0, length);
        AudioHelper.copySamples(scratch, buffer, blockRelativeStart * sampleSize, len * sampleSize, getInfo());

        try
        {
           // fileManager.removeRef()
            chunk.chunk = fileManager.createAudioChunk(scratch, length, getInfo());
        }
        catch (IOException e)
        {
            Log.e("Sequence", e.getMessage());
            return false;
        }

        return true;
    }

    // adds samples to the end
    public boolean append(ByteBuffer buffer, long len) throws IOException
    {
        int chunksCount = chunks.size();

        AudioChunkPos lastChunk = getLastChunk();

        // there is at least one chunk
        if(lastChunk != null)
        {
            long lastChunkSamplesCount = lastChunk.getChunk().getSamplesCount();
            // last chunk has some space to fill
            if(lastChunkSamplesCount < minSamples)
            {
                final long addLen = Math.min(maxSamples - lastChunkSamplesCount, len);

               // int totalNewSize = (int) (lastChunkSamplesCount + buffer.length);
               /* byte[] resChunkBuffer = new byte[(int)addLen+(int)lastChunkSamplesCount];

                byte[] lastChunkBuffer = new byte[(int) lastChunkSamplesCount];
                lastChunk.getChunk().readToBuffer(lastChunkBuffer, 0, (int)lastChunkSamplesCount);

                byte[] addBuffer = new byte[(int) addLen];
                buffer.get(addBuffer, 0, (int) addLen/info.format.sampleSize);

                ByteBuffer chunkBuffer = ByteBuffer.allocate((int)addLen);
                chunkBuffer.put(lastChunkBuffer);
                chunkBuffer.put(addBuffer);

                chunkBuffer.get(resChunkBuffer);

                final int newLastChunkLen = (int) (lastChunkSamplesCount + addLen);
                lastChunk.getChunk().writeToDisk(resChunkBuffer, newLastChunkLen);*/

                AudioHelper.appendChunk(lastChunk.getChunk(), buffer, (int) addLen, info);

                len -= addLen;
                samplesCount += addLen;
            }
        }

        // temp file count
        int q = 1;
        // append the rest as new chunks
        while (len > 0)
        {
            final long chunkLen = Math.min(maxSamples, len);

            ByteBuffer res = ByteBuffer.allocateDirect((int) chunkLen * info.format.sampleSize);
            AudioHelper.copySamples(res, buffer, 0, (int) chunkLen, info);

            AudioChunk file = fileManager.createAudioChunk(res, (int)chunkLen, info);

            //String tempFileName = SuperApplication.getAppDirectory() + "/" + (chunksCount+q) + ".ac";
            //q++;

            //file = new AudioChunk(tempFileName, (int) len, info);

            //byte[] addBuffer = new byte[(int) chunkLen];
            //buffer.get(addBuffer, 0, (int) chunkLen/info.format.sampleSize);

            //file.writeToDisk(res.array(), (int)chunkLen);

            chunks.add(new AudioChunkPos(file, samplesCount));

            samplesCount += chunkLen;
            len -= chunkLen;
        }
        return true;
    }

    // returns position of the block
    // @param pos - number of samples
    // returns -1 if failed
    public int findChunk(int pos)
    {
        if(pos < 0 || pos >= samplesCount)
            return -1;

        int chunksCount = chunks.size();

        int lo = 0, hi = chunksCount, guess;
        int loSamples = 0, hiSamples = samplesCount;

        // search for a much needed chunk
        while (true)
        {
            final double frac = (double) (pos - loSamples) / (double) (hiSamples - loSamples);
            guess = Math.min(hi - 1, lo + (int) (frac * (hi - lo)));
            final AudioChunkPos chunk = chunks.get(guess);

            //lo <= guess && guess < hi && lo < hi
            if(chunk.getChunk().getSamplesCount() < 0 ||
                    (lo > guess || guess > hi || lo > hi))
                return -1;

            if (pos < chunk.getStart())
            {
                hi = guess;
                hiSamples = chunk.getStart();
            }
            else
            {
                int nextStart = chunk.getStart() + chunk.getChunk().getSamplesCount();
                if (pos < nextStart)
                    break;
                else
                {
                    lo = guess + 1;
                    loSamples = nextStart;
                }
            }
        }

        return guess;
    }

    public int read(ByteBuffer buffer, AudioChunkPos chunkRef, int start, int len)
    {
        AudioChunk chunk = chunkRef.getChunk();

       // byte[]b = new byte[len];
        int res = chunk.readToBuffer(buffer, start, len);

        //ByteBuffer fff= ByteBuffer.allocateDirect(66);
        //buffer.put(b);

        if (res/info.format.sampleSize != len)
        {
            Log.e(LOG_TAG, "Expected to read " + len + " bytes. Read: " + (res/info.format.sampleSize));
        }

        return res;
    }

    // pass null buffer to set silence
    public boolean set(ByteBuffer buffer, int start, int len, AudioInfo info)
    {
        if (start < 0 || start >= samplesCount || start+len > samplesCount)
            return false;

        ByteBuffer scratch = ByteBuffer.allocateDirect (maxSamples * info.format.sampleSize);

        ByteBuffer temp = null;
        if (buffer != null && info.format.sampleSize != getInfo().format.sampleSize)
        {
            int size = AudioHelper.limitSampleBufferSize(maxSamples, len);
            temp = ByteBuffer.allocateDirect(size * info.format.sampleSize);
        }

        int pos = findChunk(start);

        while (len > 0)
        {
            AudioChunkPos chunkPos = chunks.get(pos);
            // start is within block
            int bstart = start - chunkPos.getStart();
            int fileLength = chunkPos.getChunk().getSamplesCount();
            int blen = AudioHelper.limitSampleBufferSize(fileLength - bstart, len);

            if (buffer != null)
            {
                if (info.format.sampleSize == getInfo().format.sampleSize)
                    copyWrite(scratch, buffer, chunkPos, bstart, blen);
                else
                {
                    // To do: remove the extra movement.  Can we copy-samples within CopyWrite?
                    AudioHelper.copySamples(temp, buffer, bstart, blen, info);
                    copyWrite(scratch, temp, chunkPos, bstart, blen);
                }
                // buffer += (blen * info.format.sampleSize);
            }
            else // Silence
            {
                if (start == chunkPos.start && blen == fileLength)
                {
                    chunkPos.chunk = new SilentChunk(blen);
                }
                else
                {
                    // Odd partial blocks of silence at start or end.
                    temp = ByteBuffer.allocateDirect(blen * info.format.sampleSize);
                    AudioHelper.clearSamples(temp, 0, blen, info);
                    // Otherwise write silence just to the portion of the block
                    copyWrite(scratch, temp, chunkPos, bstart, blen);
                }
            }

            len -= blen;
            start += blen;
            pos++;
        }
        return true;
    }

    // fills buffer with samples
    public int get(ByteBuffer buffer, int start, int len)
    {
        if(start == samplesCount)
            return 0;

        if(start < 0 || start > samplesCount || start + len > samplesCount)
        {
            Log.e(LOG_TAG, "Get: out of range");
            return -1;
        }

        int pos = findChunk(start);

        return get(pos, buffer, start, len);
    }

    public int get(int pos, ByteBuffer buffer, int start, int len)
    {
        int bytesRead = 0;
        while (len > 0)
        {
            AudioChunkPos chunkRef = chunks.get(pos);
            // start is in block
            int bstart = start - chunkRef.getStart();
            // bstart is not more than block length
            int blen = Math.min(len, chunkRef.getChunk().getSamplesCount() - bstart);

            bytesRead += read(buffer, chunkRef, bstart, blen);

            len -= blen;
            pos++;
            start += blen;
        }

        return bytesRead;
    }

    public AudioInfo getInfo()
    {
        return info;
    }

    public AudioChunkPos getLastChunk()
    {
        int chunksCount = chunks.size();

        // no chunks
        if(chunksCount == 0)
            return null;

        return chunks.get(chunksCount-1);
    }

    class AudioChunkPos implements Serializable
    {
        protected AudioChunk chunk;
        protected int start;

        public AudioChunkPos(AudioChunk chunk, int start)
        {
            this.chunk = chunk;
            this.start = start;
        }

        public AudioChunk getChunk()
        {
            return chunk;
        }

        public int getStart()
        {
            return start;
        }
    }

}
