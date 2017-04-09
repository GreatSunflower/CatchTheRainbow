package com.sunflower.catchtherainbow.Views.Effects;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.sunflower.catchtherainbow.AudioClasses.AudioIO;
import com.sunflower.catchtherainbow.AudioClasses.AudioInfo;
import com.sunflower.catchtherainbow.AudioClasses.AudioSequence;
import com.sunflower.catchtherainbow.AudioClasses.TrackInfo;
import com.sunflower.catchtherainbow.AudioClasses.WaveTrack;
import com.sunflower.catchtherainbow.Views.Editing.MainAreaFragment;
import com.un4seen.bass.BASS;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Alexandr on 28.02.2017.
 */

public abstract class BaseEffectFragment extends Fragment
{
    protected int chan = 0;
    protected AudioIO player;

    protected MainAreaFragment.SampleRange range;

    public BaseEffectFragment() {/*Required empty public constructor*/}

    public void setChannel(int chan)
    {
        this.chan = chan;
    }

    public void setEffect()
    {

    }

    public boolean onOk()
    {
        return false;
    }

    public boolean cancel()
    {
        return false;
    }

    public void setPlayer(AudioIO player)
    {
        this.player = player;
    }

    public MainAreaFragment.SampleRange getRange()
    {
        return range;
    }

    public void setRange(MainAreaFragment.SampleRange range)
    {
        this.range = range;
    }
}

// Saves effect samples to track
class ApplyEffectTask extends AsyncTask<Void, Integer, Void>
{
    private boolean running;
    private ProgressDialog progressDialog;
    private Context context;
    private String message;
    private int[] effectIds;
    private Object[] effects;
    private WaveTrack track;
    private MainAreaFragment.SampleRange range;

    public ApplyEffectTask(Context context, String message, WaveTrack track, int effectId, Object effect, MainAreaFragment.SampleRange range)
    {
        this.context = context;
        this.message = message;
        this.track = track;
        this.range = range;

        this.effectIds = new int[] {effectId};
        this.effects = new Object[] {effect};
    }

    public ApplyEffectTask(Context context, String message, WaveTrack track, int []effectIds, Object []effects, MainAreaFragment.SampleRange range)
    {
        this.context = context;
        this.message = message;
        this.track = track;
        this.range = range;

        this.effectIds = effectIds;
        this.effects = effects;
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        TrackInfo trackInfo = new TrackInfo(0, track, 0); // new TrackInfo(0, track, track.timeToSamples(start));
        AudioInfo audioInfo = trackInfo.getTrack().getInfo();

        int sampleSize = audioInfo.getFormat().getSampleSize();

        int channel = BASS.BASS_StreamCreate(audioInfo.getSampleRate(), audioInfo.getChannels(),
                BASS.BASS_SAMPLE_FLOAT|BASS.BASS_STREAM_DECODE, new ApplyEffectStreamProc(), trackInfo);

        trackInfo.setChannel(channel);
        trackInfo.setCurrentSample(trackInfo.getTrack().timeToSamples(0));

        for(int i = 0; i < effectIds.length; i++)
        {
            // set effects
            int fx = BASS.BASS_ChannelSetFX(channel, effectIds[i], 0);
            BASS.BASS_FXSetParameters(fx, effects[i]);
        }

        // length in bytes
        long len =  track.getEndSample() * sampleSize;//BASS.BASS_ChannelGetLength(channel, BASS.BASS_POS_BYTE);

        // size in bytes
        int bufferSize = AudioSequence.maxChunkSize / 2; /*1048576*4*/;

        long totalBytesRead = 0;

        if(range != null)
        {
            trackInfo.setCurrentSample(range.startingSample);
            totalBytesRead = range.startingSample * sampleSize;
            len = range.getLen() * sampleSize;

            if(range.getLen() > 100000) bufferSize = (int) (range.getLen() * sampleSize / 16);
            else bufferSize = (int) (range.getLen() * sampleSize / 6);
        }

        // read data piece by piece
        while(len > 0)
        {
            ByteBuffer audioData = ByteBuffer.allocateDirect(bufferSize);
            int bytesRead = BASS.BASS_ChannelGetData(channel, audioData, bufferSize);

            if(bytesRead <= 0)
            {
                publishProgress(100);
                break;
            }

            try
            {
                track.set(audioData, totalBytesRead / sampleSize, (bytesRead) / sampleSize);
            }
            catch (IOException | ClassNotFoundException e)
            {
                e.printStackTrace();
            }

            totalBytesRead += bytesRead;
            len -= bytesRead;


            publishProgress((int) ((range.getLen() * sampleSize) / (float)len * 100f));
        }

        return null;
    }

   /* @Override
    protected Void doInBackground(Void... params)
    {

        TrackInfo trackInfo = new TrackInfo(0, track, 0); // new TrackInfo(0, track, track.timeToSamples(start));
        AudioInfo audioInfo = trackInfo.getTrack().getInfo();

        int sampleSize = audioInfo.getFormat().getSampleSize();

        int channel = BASS.BASS_StreamCreate(audioInfo.getSampleRate(), audioInfo.getChannels(),
                BASS.BASS_SAMPLE_FLOAT|BASS.BASS_STREAM_DECODE, new ApplyEffectStreamProc(), trackInfo);

        trackInfo.setChannel(channel);
        trackInfo.setCurrentSample(trackInfo.getTrack().timeToSamples(0));

        for(int i = 0; i < effectIds.length; i++)
        {
            // set effects
            int fx = BASS.BASS_ChannelSetFX(channel, effectIds[i], 0);
            BASS.BASS_FXSetParameters(fx, effects[i]);
        }

        // length in bytes
        final long len =  track.getEndSample() * sampleSize;//BASS.BASS_ChannelGetLength(channel, BASS.BASS_POS_BYTE);

        // size in bytes
        int bufferSize = AudioSequence.maxChunkSize / 2; /*1048576*4*/;
        /*int totalBytesRead = 0;

        // read data piece by piece
        while(totalBytesRead < len)
        {
            ByteBuffer audioData = ByteBuffer.allocateDirect(bufferSize);
            int bytesRead = BASS.BASS_ChannelGetData(channel, audioData, bufferSize);

            if(bytesRead <= 0)
            {
                publishProgress(100);
                break;
            }

            track.set(audioData, totalBytesRead / sampleSize, (totalBytesRead + bytesRead) / sampleSize);

            totalBytesRead += bytesRead;


            publishProgress((int) ((float)totalBytesRead / len * 100));
        }

        return null;
    }*/

    @Override
    protected void onProgressUpdate(Integer... values)
    {
        super.onProgressUpdate(values);
        progressDialog.setProgress(values[0]);
        //progressDialog.setMessage(String.valueOf(values[0]));
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        running = true;

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(message);
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
       /* progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                running = false;
            }
        });*/
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        super.onPostExecute(aVoid);

        progressDialog.dismiss();
    }
}

class ApplyEffectStreamProc implements BASS.STREAMPROC
{
    @Override
    public int STREAMPROC(int handle, ByteBuffer buffer, int length, Object user)
    {
        // get out if a wrong parameter passed
        if(!(user instanceof TrackInfo)) return BASS.BASS_STREAMPROC_END;

        TrackInfo track = (TrackInfo)user;
        int samplesRead = 0;

        long currentSample = track.getCurrentSample();

        try
        {
            int sampleSize = track.getTrack().getInfo().getFormat().getSampleSize();
            samplesRead = track.getTrack().get(buffer, track.getCurrentSample(), length / sampleSize) / sampleSize;

            track.setCurrentSample(track.getCurrentSample() + samplesRead);

            if (samplesRead <= 0 || samplesRead < length / sampleSize || track.getTrack().getEndSample() < track.getCurrentSample())
            {
                Log.e("Decoding", "Normal end! " + track.getTrack().getName());

                samplesRead |= BASS.BASS_STREAMPROC_END;

                return samplesRead;
            }

            //Log.d(LOG_TAG, String.format("FileProcUserRead: requested {i}, read {i} ", length, bytesRead));
            return samplesRead * sampleSize;
        }
        catch(Exception ex)
        {
            Log.e("Decoding", "File Read Exception! " + ex.getMessage());
            //track.currentSample = 0;
        }
        samplesRead |= BASS.BASS_STREAMPROC_END;

        return samplesRead;
    }
};

