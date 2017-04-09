package com.sunflower.catchtherainbow.AudioClasses;

import android.util.Log;

import com.sunflower.catchtherainbow.Helper;
import com.un4seen.bass.BASS;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

/**
 * Created by SuperComputer on 4/5/2017.
 * Streams audio track
 */

public class StreamProc implements BASS.STREAMPROC
{
    private static final String LOG_TAG = "Stream Proc";

    @Override
    public int STREAMPROC(int handle, ByteBuffer buffer, int length, Object user)
    {
        // get out if a wrong parameter passed
        if(!(user instanceof TrackInfo)) return BASS.BASS_STREAMPROC_END;

        TrackInfo track = (TrackInfo)user;
        int samplesRead = 0;

        try
        {
            Date startDate = new Date();

            int sampleSize = track.track.getInfo().format.getSampleSize();
            samplesRead = track.track.get(buffer, track.currentSample, length / sampleSize) / sampleSize;

            track.currentSample+=samplesRead;

            //Log.e(LOG_TAG, "Len: " + length + ", SLen: " + length/4 + ", Samples Read: " + samplesRead);
            if (samplesRead <= 0 || samplesRead < length / sampleSize || track.track.getEndSample() < track.currentSample)
            {
                Log.e(LOG_TAG, "Normal end! " + track.track.name);

                samplesRead |= BASS.BASS_STREAMPROC_END;

                return samplesRead;
            }

            Date endDate = new Date();
            long difference = endDate.getTime() - startDate.getTime();

            Log.e(LOG_TAG, Helper.millisecondsToSeconds(difference)+", Read: " + samplesRead);

            //Log.d(LOG_TAG, String.format("FileProcUserRead: requested {i}, read {i} ", length, bytesRead));
            return samplesRead * sampleSize;
        }
        catch(Exception ex)
        {
            Log.e(LOG_TAG, "File Read Exception! " + ex.getMessage());
            //track.currentSample = 0;
            //checkFinish(track);
        }
        samplesRead |= BASS.BASS_STREAMPROC_END;

        return BASS.BASS_STREAMPROC_END;
    }
};