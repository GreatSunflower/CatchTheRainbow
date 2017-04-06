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
import com.un4seen.bass.BASS;

import java.nio.ByteBuffer;

/**
 * Created by Alexandr on 28.02.2017.
 */

public abstract class BaseEffectFragment extends Fragment
{
    protected int chan = 0;
    protected AudioIO player;

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

    public ApplyEffectTask(Context context, String message, WaveTrack track, int effectId, Object effect)
    {
        this.context = context;
        this.message = message;
        this.track = track;

        this.effectIds = new int[] {effectId};
        this.effects = new Object[] {effect};
    }

    public ApplyEffectTask(Context context, String message, WaveTrack track, int []effectIds, Object []effects)
    {
        this.context = context;
        this.message = message;
        this.track = track;

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
        final long len =  track.getEndSample() * sampleSize;//BASS.BASS_ChannelGetLength(channel, BASS.BASS_POS_BYTE);

        // size in bytes
        int bufferSize = AudioSequence.maxChunkSize / 2; /*1048576*4*/;
        int totalBytesRead = 0;

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
    }

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

            if (samplesRead == -1 || samplesRead < length / sampleSize || track.getTrack().getEndSample() < track.getCurrentSample())
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

