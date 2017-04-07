package com.sunflower.catchtherainbow.AudioClasses;

import android.support.v7.widget.ThemedSpinnerAdapter;
import android.util.Log;

import com.sunflower.catchtherainbow.Helper;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;


// manages audio chunks array
public class AudioSequence implements Serializable
{
    private static final String LOG_TAG = "Sequence";

    // max size of audio chunk in bytes
    public static final int maxChunkSize = 2097152;

    // number of samples in audio chunk
    private int minSamples;
    private int maxSamples;

    protected long samplesCount = 0;

    // audio info
    private AudioInfo info = new AudioInfo(44100, 2);

    private FileManager fileManager;

    private ArrayList<AudioChunkPos> chunks = new ArrayList<>();

    public AudioSequence(FileManager manager, AudioInfo info)
    {
        this.fileManager = manager;
        this.info = info;

        minSamples = maxChunkSize / info.format.getSampleSize();
        maxSamples = minSamples;
    }

    public AudioSequence(FileManager manager, AudioSequence other)
    {
        this.fileManager = manager;
        this.info = other.info;

        paste(0, other);

        // TEMP!!! USE only paste
      /*  for(int i = 0; i < other.chunks.size(); i++)
            this.chunks.add(other.chunks.get(i));*/

        minSamples = maxChunkSize / info.format.getSampleSize();
        maxSamples = minSamples;
    }

    public boolean copyWrite(ByteBuffer scratch,
                             ByteBuffer buffer, AudioChunkPos chunk,
                             long blockRelativeStart, int len)
    {
        // We don't ever write to an existing block; to support Undo,
        // we createFromCopy the old block entirely into memory, dereference it,
        // make the change, and then write the NEW block to disk.

        int length = chunk.getChunk().getSamplesCount();
        if(length > maxSamples || blockRelativeStart + len > length)
            return false;

        int sampleSize = getInfo().format.getSampleSize();

        read(scratch, chunk, 0, length);
        scratch.rewind();
        scratch.position((int) (blockRelativeStart * sampleSize));
        AudioHelper.copySamples(scratch, buffer, 0/*(int) blockRelativeStart*/, len, getInfo());

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

                AudioHelper.appendChunk(lastChunk.chunk, buffer, (int) addLen, info);

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

            ByteBuffer res = ByteBuffer.allocateDirect((int) chunkLen * info.format.getSampleSize());
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

    boolean appendChunk(final AudioChunkPos b)
    {
        // Quick check to make sure that it doesn't overflow
        if ((samplesCount) + ((double)b.getChunk().getSamplesCount()) > maxSamples * 5)
            return false;

        // Bump ref count if not locked, else createFromCopy
        AudioChunkPos newBlock = new AudioChunkPos(fileManager.copyChunk(b.getChunk()), samplesCount);
        if (newBlock.getChunk() == null)
        {
            return false;
        }

        chunks.add(newBlock);
        samplesCount += newBlock.getChunk().getSamplesCount();

        return true;
    }

    // returns position of the block
    // @param pos - number of samples
    // returns -1 if failed
    public int findChunk(long pos)
    {
        if(pos < 0 || pos >= samplesCount)
            return -1;

        int chunksCount = chunks.size();

        int lo = 0, hi = chunksCount, guess;
        long loSamples = 0, hiSamples = samplesCount;

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
                long nextStart = chunk.getStart() + chunk.getChunk().getSamplesCount();
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

    public int read(ByteBuffer buffer, AudioChunkPos chunkRef, long start, long len)
    {
        AudioChunk chunk = chunkRef.getChunk();

       // byte[]b = new byte[len];
        int res = chunk.readToBuffer(buffer, start, len);

        //ByteBuffer fff= ByteBuffer.allocateDirect(66);
        //buffer.put(b);

        if (res/info.format.getSampleSize() != len)
        {
            Log.e(LOG_TAG, "Expected to read " + len + " bytes. Read: " + (res/info.format.getSampleSize()));
        }

        return res;
    }

    // pass null buffer to set silence
    public boolean set(ByteBuffer buffer, long start, long len, AudioInfo info)
    {
        if (start < 0 || start >= samplesCount || start+len > samplesCount)
            return false;

        ByteBuffer scratch = ByteBuffer.allocateDirect (maxSamples * info.format.getSampleSize());

        ByteBuffer temp = null;
        if (buffer != null && info.format.getSampleSize() != getInfo().format.getSampleSize())
        {
            long size = AudioHelper.limitSampleBufferSize(maxSamples, len);
            temp = ByteBuffer.allocateDirect((int) (size * info.format.getSampleSize()));
        }

        int pos = findChunk(start);

        while (len > 0)
        {
            AudioChunkPos chunkPos = chunks.get(pos);
            // start is within block
            long bstart = start - chunkPos.getStart();
            int fileLength = chunkPos.getChunk().getSamplesCount();
            long blen = AudioHelper.limitSampleBufferSize(fileLength - bstart, len);

            if (buffer != null)
            {
                if (info.format.getSampleSize() == getInfo().format.getSampleSize())
                    copyWrite(scratch, buffer, chunkPos, bstart, (int) blen);
                else
                {

                    AudioHelper.copySamples(temp, buffer, (int)bstart, (int)blen, info);
                    copyWrite(scratch, temp, chunkPos, bstart, (int) blen);
                }
                // buffer += (blen * info.format.sampleSize);
            }
            else // Silence
            {
                if (start == chunkPos.start && blen == fileLength)
                {
                    chunkPos.chunk = new SilentChunk((int) blen);
                }
                else
                {
                    // Odd partial blocks of silence at start or end.
                    temp = ByteBuffer.allocateDirect((int) (blen * info.format.getSampleSize()));
                    AudioHelper.clearSamples(temp, 0, (int) blen, info);
                    // Otherwise write silence just to the portion of the block
                    copyWrite(scratch, temp, chunkPos, bstart, (int) blen);
                }
            }

            len -= blen;
            start += blen;
            pos++;
        }
        return true;
    }

    // fills buffer with samples
    public int get(ByteBuffer buffer, long start, int len)
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

    public int get(int pos, ByteBuffer buffer, long start, int len)
    {
        int bytesRead = 0;
        while (len > 0)
        {
            AudioChunkPos chunkRef = chunks.get(pos);
            // start is in block
            long bstart = start - chunkRef.getStart();
            // bstart is not more than block length
            long blen = Math.min(len, chunkRef.getChunk().getSamplesCount() - bstart);

            bytesRead += read(buffer, chunkRef, bstart, blen);

            len -= blen;
            pos++;
            start += blen;
        }

        return bytesRead;
    }

    public boolean copy(long start, long end, AtomicReference<AudioSequence> dest)
    {
        if (start >= end || start >= samplesCount || end < 0)
            return false;

        int numBlocks = chunks.size();

        int b0 = findChunk(start);
        int b1 = findChunk(end - 1);

        if (b0 < 0 || b0 > numBlocks || b1 > numBlocks || b0 > b1)
            return false;

        dest.set(new AudioSequence(fileManager, info));

        ByteBuffer buffer = ByteBuffer.allocateDirect(maxSamples * info.format.getSampleSize());

        int blocklen;

        // Do the first block

        final AudioChunkPos block0 = chunks.get(b0);
        if (start != block0.start)
        {
            final AudioChunk file = block0.getChunk();
            // Nonnegative result is length of block0 or less:
            blocklen = (int) (Math.min(end, block0.start + file.getSamplesCount()) - start);

            get(b0, buffer, start, blocklen);

            try
            {
                dest.get().append(buffer, blocklen);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return false;
            }
        }
        else --b0;

        // If there are blocks in the middle, createFromCopy the blockfiles directly
        for (int bb = b0 + 1; bb < b1; ++bb)
            dest.get().appendChunk(chunks.get(bb)); // Increase ref count or duplicate file

        // Do the process last chunk
        if (b1 > b0)
        {
            final AudioChunkPos block = chunks.get(b1);
            final AudioChunk file = block.getChunk();
            // s1 is within block:
            blocklen = (int) (end - block.start);

            if (blocklen < file.getSamplesCount())
            {
                get(b1, buffer, block.start, blocklen);
                try
                {
                    dest.get().append(buffer, blocklen);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return false;
                }
            }
            else
            {
                // Special case, createFromCopy exactly
                dest.get().appendChunk(block); // Increase ref count or duplicate file
            }
        }

        return true;
    }
    public boolean paste(long pos, final AudioSequence src)
    {
        if ((pos < 0) || (pos > samplesCount))
        {
            Log.e("Paste", "Out of range");
            return false;
        }

        // Quick check to make sure that it doesn't overflow
        if (samplesCount + src.samplesCount > 1999999999)
        {
            Log.e("Paste", "Overflow");
            return false;
        }

        if (src.info.format.getSampleSize() != info.format.getSampleSize())
        {
            Log.e("Paste", "Sample sizes do not match");
            return false;
        }

        final ArrayList<AudioChunkPos> srcBlock = src.chunks;
        long addedLen = src.samplesCount;
        final int srcNumBlocks = srcBlock.size();
        int sampleSize = info.format.getSampleSize();

        if (addedLen == 0 || srcNumBlocks == 0)
            return true;

        final int numBlocks = chunks.size();

        if (numBlocks == 0 || (pos == samplesCount && chunks.get(chunks.size()-1).getChunk().getSamplesCount() >= minSamples))
        {
            // Special case: this track is currently empty, or it's safe to append
            // onto the end because the current last block is longer than the
            // minimum size

            for (int i = 0; i < srcNumBlocks; i++)
                appendChunk(srcBlock.get(i)); // Increase ref count or duplicate file

            return true;
        }

        final int b = (pos == samplesCount) ? chunks.size() - 1 : findChunk(pos);

        if(b < 0 || b >= numBlocks) return false;
        AudioChunkPos pBlock = chunks.get(b);
        final int length = pBlock.getChunk().getSamplesCount();
        final long largerBlockLen = addedLen + length;
        // PRL: when insertion point is the first sample of a block,
        // and the following test fails, perhaps we could test
        // whether coalescence with the previous block is possible.
        if (largerBlockLen <= maxSamples)
        {
            // Special case: we can fit all of the NEW samples inside of
            // one block!
            AudioChunkPos block = pBlock;
            // largerBlockLen is not more than mMaxSamples...
            ByteBuffer buffer = ByteBuffer.allocateDirect((int) (largerBlockLen * sampleSize));

            // ...and addedLen is not more than largerBlockLen
            long sAddedLen = addedLen;
            // s lies within block:
            long splitPoint = pos - block.start;
            read(buffer, block, 0, splitPoint);
            src.get(0, buffer,0, (int) sAddedLen);
            read(buffer, block, splitPoint, length - splitPoint);

            AudioChunk file = null;
            try
            {
                file = fileManager.createAudioChunk(buffer, largerBlockLen, info);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return false;
            }

            block.chunk = file;

            for (int i = b + 1; i < numBlocks; i++)
                chunks.get(i).start += addedLen;

            samplesCount += addedLen;

            return true;
        }

        // Case three: if we are inserting four or fewer blocks,
        // it's simplest to just lump all the data together
        // into one big block along with the split block,
        // then resplit it all
        ArrayList<AudioChunkPos> newBlock = new ArrayList<>(numBlocks + srcNumBlocks + 2);
        for(int i = 0; i < b; i++)
            newBlock.add(chunks.get(i));

        AudioChunkPos splitBlock = chunks.get(b);
        long splitLen = splitBlock.getChunk().getSamplesCount();
        // s lies within splitBlock
        long splitPoint = pos - splitBlock.start;

        int i;
        if (srcNumBlocks <= 4)
        {
            // addedLen is at most four times maximum block size
            long sAddedLen = addedLen;
            final long sum = splitLen + sAddedLen;

            ByteBuffer sumBuffer = ByteBuffer.allocateDirect((int) (sum * sampleSize));
            read(sumBuffer, splitBlock, 0, splitPoint);
            src.get(0, sumBuffer, 0, (int) sAddedLen);
            read(sumBuffer, splitBlock, splitPoint, splitLen - splitPoint);

            makeSequence(newBlock, splitBlock.start, sumBuffer, sum);
        }
        else
        {
            // The final case is that we're inserting at least five blocks.
            // We divide these into three groups: the first two get merged
            // with the first half of the split block, the middle ones get
            // copied in as is, and the last two get merged with the last
            // half of the split block.

            final long srcFirstTwoLen = srcBlock.get(0).getChunk().getSamplesCount() + srcBlock.get(1).getChunk().getSamplesCount();
            final long leftLen = splitPoint + srcFirstTwoLen;

            final AudioChunkPos penultimate = srcBlock.get(srcNumBlocks - 2);
            final long srcLastTwoLen =    penultimate.getChunk().getSamplesCount()
                    + srcBlock.get(srcNumBlocks - 1).getChunk().getSamplesCount();
            final long rightSplit = splitBlock.getChunk().getSamplesCount() - splitPoint;
            final long rightLen = rightSplit + srcLastTwoLen;

            ByteBuffer sampleBuffer = ByteBuffer.allocateDirect((int) (Math.max(leftLen, rightLen) * sampleSize));

            read(sampleBuffer, splitBlock, 0, splitPoint);
            src.get(0, sampleBuffer, 0, (int) srcFirstTwoLen);

            makeSequence(newBlock, splitBlock.start, sampleBuffer, leftLen);

            for (i = 2; i < srcNumBlocks - 2; i++)
            {
                final AudioChunkPos block = srcBlock.get(i);
                AudioChunk file = fileManager.copyChunk(block.getChunk());
                if (file == null)
                {
                    return false;
                }

                newBlock.add(new AudioChunkPos(file, block.start + pos));
            }

            long lastStart = penultimate.start;
            src.get(srcNumBlocks - 2, sampleBuffer, lastStart, (int) srcLastTwoLen);
            read(sampleBuffer, splitBlock, splitPoint, rightSplit);

            makeSequence(newBlock, pos + lastStart, sampleBuffer, rightLen);
        }

        // Copy remaining blocks to NEW block array and
        // swap the NEW block array in for the old
        for (i = b + 1; i < numBlocks; i++)
        {
            AudioChunkPos chunkPos = chunks.get(i);
            newBlock.add(new AudioChunkPos(chunkPos.getChunk(), chunkPos.getStart() + addedLen));
        }

        chunks.clear();
        chunks.addAll(newBlock);

        samplesCount += addedLen;

        return true;
    }

    //long GetIdealAppendLen() const;
    public synchronized boolean delete(long start, long len)
    {
        if (len == 0)
            return true;
        if (len < 0 || start < 0 || start >= samplesCount)
            return false;

        final int numBlocks = chunks.size();

        int b0 = findChunk(start);
        int b1 = findChunk(start + len - 1);

        int sampleSize = info.format.getSampleSize();

        // Special case: if the samples to DELETE are all within a single
        // block and the resulting length is not too small, perform the
        // deletion within this block:
        AudioChunkPos chunk;
        long length /*= chunk.getChunk().getSamplesCount()*/;

        // One buffer for reuse in various branches here
        ByteBuffer scratch = null;
        // The maximum size that will ever be needed
        final int scratchSize = maxSamples + minSamples;

        if (b0 == b1 && (length = (chunk = chunks.get(b0)).getChunk().getSamplesCount()) - len >= minSamples)
        {
            AudioChunkPos b = chunk;
            // start is within block
            long pos = start - b.start;

            if(len < length)
                return false;
            // len must be less than length
            // because start + len - 1 is also in the block...
            long newLen = length - len;

            scratch = ByteBuffer.allocateDirect(scratchSize * sampleSize);

            read(scratch, b, 0, pos);
            read(scratch, b, pos + len, newLen - pos); // pos + len is not more than the length of the block

            try
            {
                b = new AudioChunkPos(fileManager.createAudioChunk(scratch, newLen, info), (int) b.start);
                chunks.set(b0, b);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return false;
            }

            for (int j = b0 + 1; j < numBlocks; j++)
                chunks.get(j).start -= len;

            // update total samples
            samplesCount -= len;

            return true;
        }

        // Create a NEW array of blocks
        ArrayList<AudioChunkPos> newBlock = new ArrayList<>(numBlocks - (b1 - b0) + 2);

        for(int i = 0; i < b0; i++)
            newBlock.add(chunks.get(i));

        int i;

        // First grab the samples in block b0 before the deletion point into preBuffer.  If this is enough samples for its own block,
        // or if this would be the first block in the array, write it out.
        // Otherwise combine it with the previous block (splitting them 50/50 if necessary).
        final AudioChunkPos preBlock = chunks.get(b0);
        // start is within preBlock
        long preBufferLen = start - preBlock.start;
        if (preBufferLen > 0)
        {
            if (preBufferLen >= minSamples || b0 == 0)
            {
                //if (scratch == null)
                    scratch = ByteBuffer.allocateDirect(scratchSize * sampleSize);
                read(scratch, preBlock, 0, preBufferLen);
                AudioChunk pFile = null;
                try
                {
                    pFile = fileManager.createAudioChunk(scratch, preBufferLen, info);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return false;
                }

                newBlock.add(new AudioChunkPos(pFile, preBlock.start));
            }
            else
            {
                final AudioChunkPos prepreBlock = chunks.get(b0 - 1);
                final long prepreLen = prepreBlock.getChunk().getSamplesCount();
                final long sum = prepreLen + preBufferLen;

                //if (scratch == null)
                    scratch = ByteBuffer.allocateDirect(scratchSize * sampleSize);

                read(scratch, preBlock, 0, prepreLen);
                read(scratch, preBlock, 0, preBufferLen);

                newBlock.remove(newBlock.size() - 1);
                makeSequence(newBlock, prepreBlock.start, scratch, sum);
            }
        }

        // Now, symmetrically, grab the samples in block b1 after the
        // deletion point into postBuffer.  If this is enough samples
        // for its own block, or if this would be the last block in
        // the array, write it out.  Otherwise combine it with the
        // subsequent block (splitting them 50/50 if necessary).
        final AudioChunkPos postBlock = chunks.get(b1);
            // start + len - 1 lies within postBlock
        final long postBufferLen = ((postBlock.start + postBlock.getChunk().getSamplesCount()) - start + len);
        if (postBufferLen > 0)
        {
            if (postBufferLen >= minSamples || b1 == numBlocks - 1)
            {
               // if (scratch == null)
                    // Last use of scratch, can ask for smaller
                    scratch = ByteBuffer.allocateDirect((int) (postBufferLen * sampleSize));
                // start + len - 1 lies within postBlock
                long pos = start + len - postBlock.start;
                read(scratch, postBlock, pos, postBufferLen);
                AudioChunk file = null;
                try
                {
                    file = fileManager.createAudioChunk(scratch, postBufferLen, info);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return false;
                }

                newBlock.add(new AudioChunkPos(file, (int) start));
            }
            else
            {
                AudioChunkPos postpostBlock = chunks.get(b1 + 1);
                final long postpostLen = postpostBlock.getChunk().getSamplesCount();
                final long sum = postpostLen + postBufferLen;

                //if (scratch == null)
                    // Last use of scratch, can ask for smaller
                    scratch = ByteBuffer.allocateDirect((int) (sum * sampleSize));
                // start + len - 1 lies within postBlock
                long pos = start + len - postBlock.start;
                read(scratch, postBlock, pos, postBufferLen);
                read(scratch, postpostBlock, 0, postpostLen);

                makeSequence(newBlock, start, scratch, sum);
                b1++;
            }
        }

        // Copy the remaining chunks over from the old array
        for (i = b1 + 1; i < numBlocks; i++)
        {
            AudioChunkPos chunkPos = chunks.get(i);
            newBlock.add(new AudioChunkPos(chunkPos.chunk, chunkPos.start + (-len)));
        }
        // Substitute our NEW array for the old one
        chunks.clear();
        chunks.addAll(newBlock);

        // Update total number of samples and do a consistency check.
        samplesCount -= len;

        return true;
    }

    void makeSequence(ArrayList<AudioChunkPos> list, long start, ByteBuffer buffer, long len)
    {
        if (len <= 0)
            return;
        long num = (len + (maxSamples - 1)) / maxSamples;

        for (long i = 0; i < num; i++)
        {
            AudioChunkPos b = new AudioChunkPos();

            final int offset = (int) (i * len / num);
            b.start = (int) (start + offset);
            int newLen = (int) ( ((i + 1) * len / num) - offset);

            ByteBuffer bufStart = ByteBuffer.allocateDirect ((int) (/*len + */offset * info.format.getSampleSize()));
            byte[]buff = new byte[newLen];
            buffer.get(buff, offset, newLen);
            bufStart.put(buff);

            try
            {
                b.chunk = fileManager.createAudioChunk(bufStart, newLen, info);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            list.add(b);
        } // for
    }

    boolean insertSilence(long s0, int len)
    {
        // Quick check to make sure that it doesn't overflow
        if ((samplesCount + len) > maxSamples * 5)
            return false;

        if (len <= 0)
            return true;

        // Create a NEW track containing as much silence as we
        // need to insert, and then call Paste to do the insertion.
        // We make use of a SilentBlockFile, which takes up no
        // space on disk.

        AudioSequence sTrack = new AudioSequence(fileManager, info);

        long idealSamples = maxSamples;

        long pos = 0;

        // Could nBlocks overflow a size_t?  Not very likely.  You need perhaps
        // 2 ^ 52 samples which is over 3000 years at 44.1 kHz.
        long nBlocks = (len + idealSamples - 1) / idealSamples;

        AudioChunk silentFile = new SilentChunk((int) idealSamples);
        while (len >= idealSamples)
        {
            sTrack.chunks.add(new AudioChunkPos(silentFile, pos));

            pos += idealSamples;
            len -= idealSamples;
        }
        if (len != 0)
        {
            // len is not more than idealSamples:
            sTrack.chunks.add(new AudioChunkPos(new SilentChunk(len), pos));
            pos += len;
        }

        sTrack.samplesCount = (int) pos;

        return paste(s0, sTrack);
    }

    // wave data
    boolean getWaveData(long startSample, long frameCount, long samplesPerFrame, WaveData waveData)
    {
        if(waveData == null || samplesPerFrame < 1) return false;

        int divider = 1;

        if(samplesPerFrame >= 256 && samplesPerFrame < 65536)
            divider = 256;
        else if(samplesPerFrame >= 65536)
            divider = 65536;
        else
        {
            divider = 1;
            if (samplesPerFrame < 10)
                waveData.individualSamples = true;
        }

        int index = findChunk(startSample);
        int frame = 0;

        long len = samplesPerFrame * frameCount;

        //Log.e("Sequence","Total Len: " + len);

        while (len > 0 && index < chunks.size() && index >= 0)
        {
            AudioChunkPos chunkPos = chunks.get(index);

            // start is in block
            long bStart = startSample - chunkPos.getStart();
            // bstart is not more than block length
            long bLen = Math.min(len, chunkPos.getChunk().getSamplesCount() - bStart);

            if(bLen == 0) break;

            // decrement while we can
            len -= bLen;
            startSample += bLen;

            /*bStart = Math.max(0, bStart / divider);
            int inclusiveEndPosition = Math.min( (bLen / divider) - 1, (startSample) / divider);
            bLen = Helper.clamp( 1 + inclusiveEndPosition - bStart, 0, maxSamples);*/
            //bStart = frames64K * bytesPerFrame;

            ByteBuffer buffer = null;

            // read data depending on zoom level
            switch (divider)
            {
                case 1: // read sample data
                    bLen *= 2;
                    buffer = ByteBuffer.allocateDirect((int) (bLen * 4));
                    chunkPos.getChunk().readToBuffer(buffer, bStart, bLen);
                    break;
                case 256: // summary 256
                    bStart = bStart / divider;
                    bLen = bLen / samplesPerFrame * 6;
                    //bLen *= 24;
                    buffer = ByteBuffer.allocateDirect((int) (bLen * 4));
                    chunkPos.getChunk().read256(buffer, bStart, bLen);
                    break;
                case 65536: // summary 65536
                    bStart = bStart / divider;
                    bLen = bLen / samplesPerFrame * 6;
                    //bStart = bStart / chunkPos.chunk.frames64K;
                    //bLen = chunkPos.chunk.frames64K - bStart;
                    //bLen *= 24;
                    buffer = ByteBuffer.allocateDirect((int) (bLen * 4));
                    chunkPos.getChunk().read64(buffer, bStart, bLen);
                    break;
            }

            // get float buffer
            float []floatBuffer = new float[(int) bLen];
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.rewind();
            buffer.asFloatBuffer().get(floatBuffer);

            int curPos = 0;

            int div = Math.round(floatBuffer.length / (float) frameCount / 2)/*(int) (samplesPerFrame / divider)*/;
            //Log.e("SEQUENCE", "Div: " + div+"");

            // no data gathered
            if(div == 0)
            {
                index++;
                continue;
            }
            /*if(div == 0)
            {
                for (; curPos < waveData.max.length; ) // fill with silence
                {
                    if(frame >= waveData.max.length-1) break;

                    waveData.max[frame] = 0 ;
                    waveData.min[frame] = 0 ;

                    frame++;
                    curPos++;
                }
                continue;
            }*/

            // round samples to fit the frame
            for(; curPos < bLen; )
            {
                if(frame >= waveData.max.length) break;

                if(curPos < 0 || curPos >= floatBuffer.length)
                {
                    Log.e("SEQUENCE", "Unexpected end at " + curPos+"");
                    break;
                }

                // find average values
                float avgMin = Float.MAX_VALUE, avgMax = Float.MIN_VALUE;

                // read pcm data
                if(divider == 1)
                {
                    for (int i = 0; i < div; i++)
                    {
                        /*if (curPos < floatBuffer.length)
                            avgMin += floatBuffer[curPos];
                        if (curPos + 2 < floatBuffer.length)
                            avgMax += floatBuffer[curPos + 2];*/

                        if (curPos >= floatBuffer.length /*|| curPos + 2 >= floatBuffer.length*/) break;

                        if (waveData.individualSamples)
                        {
                            avgMin = floatBuffer[curPos];
                            avgMax = floatBuffer[curPos];
                        }
                        else
                        {
                            if (floatBuffer[curPos] < avgMin)
                                avgMin = floatBuffer[curPos];
                            if (floatBuffer[curPos] > avgMax)
                                avgMax = floatBuffer[curPos];
                        }

                        curPos += 2;
                    }

                    // modify wave data
                    waveData.max[frame] = avgMax;
                    waveData.min[frame] = avgMin;
                }
                else // read summary data
                {
                    for (int i = 0; i < div; i++)
                    {
                        if (curPos >= floatBuffer.length || curPos + 1 >= floatBuffer.length) break;

                        if(floatBuffer[curPos] < avgMin) avgMin = floatBuffer[curPos];
                        if(floatBuffer[curPos + 1] > avgMax) avgMax = floatBuffer[curPos + 1];

                        curPos+=3; // should be 6
                    }

                    // modify wave data
                    waveData.max[frame] = avgMax ;
                    waveData.min[frame] = avgMin ;

                    //curPos += div + 3*6/*+3+1*/;
                }
                frame++;
            } // for

            index++;
        }

        //Log.e("Sequence", "Frames written: " + frame + ", Divider: " + divider);
        return true;
    }

    boolean getMinMax(long start, long len, Float outMin, Float outMax)
    {
        if (len == 0 || chunks.size() == 0)
        {
            outMin = 0.0f;
            outMax = 0.0f;
            return true;
        }

        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;

        int block0 = findChunk(start);
        int block1 = findChunk(start + len - 1);

        // First calculate the min/max of the blocks in the middle of this region;
        // this is very fast because we have the min/max of every entire block
        // already in memory.

        for (int b = block0 + 1; b < block1; ++b)
        {
            Float blockMin = 0.f, blockMax = 0.f, blockRMS = 0.f;
            chunks.get(b).chunk.getMinMax(blockMin, blockMax, blockRMS);

            if (blockMin < min)
                min = blockMin;
            if (blockMax > max)
                max = blockMax;
        }

        {
            Float block0Min = 0f, block0Max = 0f, block0RMS = 0f;
            AudioChunkPos theBlock = chunks.get(block0);
            AudioChunk theFile = theBlock.getChunk();
            theFile.getMinMax(block0Min, block0Max, block0RMS);

            if (block0Min < min || block0Max > max)
            {
                // start lies within theBlock:
                int s0 = (int) (start - theBlock.start);
                int maxl0 = (int) (theBlock.start + theFile.getSamplesCount() - start);

                int l0 = (int) AudioHelper.limitSampleBufferSize(maxl0, len);

                Float partialMin = 0f, partialMax = 0f, partialRMS = 0f;
                theFile.getMinMax(s0, l0, partialMin, partialMax, partialRMS);
                if (partialMin < min)
                    min = partialMin;
                if (partialMax > max)
                    max = partialMax;
            }
        }

        if (block1 > block0)
        {
            Float block1Min = 0f, block1Max = 0f, block1RMS = 0f;
            AudioChunkPos theBlock = chunks.get(block1);
            AudioChunk theFile = theBlock.getChunk();
            theFile.getMinMax(block1Min, block1Max, block1RMS);

            if (block1Min < min || block1Max > max)
            {
                // start + len - 1 lies in theBlock:
                int l0 = (int) (start + len - theBlock.start);

                Float partialMin = 0f, partialMax = 0f, partialRMS = 0f;
                theFile.getMinMax(0, l0, partialMin, partialMax, partialRMS);
                if (partialMin < min)
                min = partialMin;
                if (partialMax > max)
                max = partialMax;
            }
        }

        outMin = min;
        outMax = max;

        return true;
    }


    public int getMinSamples()
    {
        return minSamples;
    }

    public int getMaxSamples()
    {
        return maxSamples;
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
        protected long start;

        public AudioChunkPos() {}

        public AudioChunkPos(AudioChunk chunk, long start)
        {
            this.chunk = chunk;
            this.start = start;
        }

        public AudioChunk getChunk()
        {
            return chunk;
        }

        public long getStart()
        {
            return start;
        }
    }

}

