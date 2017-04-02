package com.sunflower.catchtherainbow.Views.Effects;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sunflower.catchtherainbow.AudioClasses.AudioIO;
import com.sunflower.catchtherainbow.AudioClasses.AudioInfo;
import com.sunflower.catchtherainbow.AudioClasses.AudioSequence;
import com.sunflower.catchtherainbow.AudioClasses.BasePlayer;
import com.sunflower.catchtherainbow.AudioClasses.WaveTrack;
import com.sunflower.catchtherainbow.R;
import com.sunflower.catchtherainbow.Views.Helpful.CircularSeekBar;
import com.sunflower.catchtherainbow.Views.Helpful.DetailedSeekBar;
import com.un4seen.bass.BASS;

import java.io.ObjectInputStream;
import java.nio.ByteBuffer;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DistortionEffectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DistortionEffectFragment extends BaseEffectFragment
        implements DetailedSeekBar.OnSuperSeekBarListener
{
    public DistortionEffectFragment()
    {
        // Required empty public constructor
    }

    public static DistortionEffectFragment newInstance(String param1, String param2)
    {
        DistortionEffectFragment fragment = new DistortionEffectFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    //public static class BASS_DX8_DISTORTION {
    private CircularSeekBar fEdge;
    private DetailedSeekBar fPreLowpassCutoff, fGain, fPostEQCenterFrequency, fPostEQBandwidth;

    private int distortion;
    private BASS.BASS_DX8_DISTORTION bass_dx8_distortion;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.effect_distortion_fragment, container, false);

        fEdge = (CircularSeekBar) root.findViewById(R.id.fEdge);
        fPreLowpassCutoff = (DetailedSeekBar) root.findViewById(R.id.fPreLowpassCutoff);
        fGain = (DetailedSeekBar) root.findViewById(R.id.fGain);
        fPostEQCenterFrequency = (DetailedSeekBar) root.findViewById(R.id.fPostEQCenterFrequency);
        fPostEQBandwidth = (DetailedSeekBar) root.findViewById(R.id.fPostEQBandwidth);

        fEdge.setOnSeekBarChangeListener(new CircleSeekBarListener());
        fPreLowpassCutoff.setListener(this);
        fGain.setListener(this);
        fPostEQCenterFrequency.setListener(this);
        fPostEQBandwidth.setListener(this);

        return root;
    }

    @Override
    public void setChannel(int chan)
    {
        if(this.chan == chan) return;

        this.chan = chan;

        if(bass_dx8_distortion == null)
            setEffect();
        else
        {
            distortion = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_DISTORTION, 0);
            BASS.BASS_FXSetParameters(distortion, bass_dx8_distortion);
        }
    }

    public void setEffect()
    {
        distortion = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_DISTORTION, 0);
        bass_dx8_distortion = new BASS.BASS_DX8_DISTORTION();
        bass_dx8_distortion.fGain = -18;
        bass_dx8_distortion.fEdge = 15;
        bass_dx8_distortion.fPostEQCenterFrequency = 2400;
        bass_dx8_distortion.fPostEQBandwidth = 2400;
        bass_dx8_distortion.fPreLowpassCutoff = 8000;
        BASS.BASS_FXSetParameters(distortion, bass_dx8_distortion);
    }

    public class CircleSeekBarListener implements CircularSeekBar.OnCircularSeekBarChangeListener
    {
        @Override
        public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser)
        {
            int id = circularSeekBar.getId();
            if(id == R.id.fEdge)
            {
                bass_dx8_distortion.fEdge = (float) progress;
                BASS.BASS_FXSetParameters(distortion, bass_dx8_distortion);
            }
        }

        @Override
        public void onStopTrackingTouch(CircularSeekBar seekBar)
        {

        }

        @Override
        public void onStartTrackingTouch(CircularSeekBar seekBar)
        {

        }
    }

    public boolean onOk()
    {
        BASS.BASS_ChannelRemoveFX(chan, distortion);

        if(bass_dx8_distortion == null) return false;

        new ApplyEffectTask(getActivity(), getResources().getString(R.string.effect_apply_message), player.getTracks().get(0).getTrack(), BASS.BASS_FX_DX8_DISTORTION, bass_dx8_distortion ).execute();

        return true;
    }

    public boolean cancel() //при закрытии окна
    {
        BASS.BASS_ChannelRemoveFX(chan, distortion);
        return true;
    }

    @Override
    public void onChange(DetailedSeekBar seekBar, float selectedValue)
    {
        double res = (double)selectedValue;

        int id = seekBar.getId();

        if(id ==  R.id.fPreLowpassCutoff)
        {
            bass_dx8_distortion.fPreLowpassCutoff = (float) res;
            BASS.BASS_FXSetParameters(distortion, bass_dx8_distortion);
        }
        else if(id ==  R.id.fGain)
        {
            bass_dx8_distortion.fGain = (float) res;
            BASS.BASS_FXSetParameters(distortion, bass_dx8_distortion);
        }
        else if(id ==  R.id.fPostEQCenterFrequency)
        {
            bass_dx8_distortion.fPostEQCenterFrequency = (float) res;
            BASS.BASS_FXSetParameters(distortion, bass_dx8_distortion);
        }
        else if(id ==  R.id.fPostEQBandwidth)
        {
            bass_dx8_distortion.fPostEQBandwidth = (float) res;
            BASS.BASS_FXSetParameters(distortion, bass_dx8_distortion);
        }
    }
}

// Saves effect samples to track
class ApplyEffectTask extends AsyncTask<Void, Integer, Void>
{
    private boolean running;
    private ProgressDialog progressDialog;
    private Context context;
    private String message;
    private int effectId;
    private Object effect;
    private WaveTrack track;


    public ApplyEffectTask(Context context, String message, WaveTrack track, int effectId, Object effect)
    {
        this.context = context;
        this.message = message;
        this.effectId = effectId;
        this.effect = effect;
        this.track = track;
    }

    @Override
    protected Void doInBackground(Void... params)
    {

        AudioIO.TrackInfo trackInfo = new AudioIO.TrackInfo(0, track, 0); // new TrackInfo(0, track, track.timeToSamples(start));
        AudioInfo audioInfo = trackInfo.getTrack().getInfo();

        int sampleSize = audioInfo.getFormat().getSampleSize();

        int channel = BASS.BASS_StreamCreate(audioInfo.getSampleRate(), audioInfo.getChannels(),
                BASS.BASS_SAMPLE_FLOAT|BASS.BASS_STREAM_DECODE, new ApplyEffectStreamProc(), trackInfo);

        trackInfo.setChannel(channel);
        trackInfo.setCurrentSample(trackInfo.getTrack().timeToSamples(0));

        // set effects
        int fx = BASS.BASS_ChannelSetFX(channel, effectId, 0);
        BASS.BASS_FXSetParameters(fx, effect);

        // length in bytes
        final long len =  track.getEndSample() * sampleSize;//BASS.BASS_ChannelGetLength(channel, BASS.BASS_POS_BYTE);

        // size in bytes
        int bufferSize = AudioSequence.maxChunkSize; /*1048576*4*/;
        int totalBytesRead = 0;

        // read data piece by piece
        while(totalBytesRead < len)
        {
            ByteBuffer audioData = ByteBuffer.allocateDirect(bufferSize);
            int bytesRead = BASS.BASS_ChannelGetData(channel, audioData, bufferSize);

            if(bytesRead == -1)
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
        Toast.makeText(context, "Progress Start", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        super.onPostExecute(aVoid);
        Toast.makeText(context, "Progress Ended", Toast.LENGTH_LONG).show();

        progressDialog.dismiss();
    }
}

class ApplyEffectStreamProc implements BASS.STREAMPROC
{
    @Override
    public int STREAMPROC(int handle, ByteBuffer buffer, int length, Object user)
    {
        // get out if a wrong parameter passed
        if(!(user instanceof AudioIO.TrackInfo)) return BASS.BASS_STREAMPROC_END;

        AudioIO.TrackInfo track = (AudioIO.TrackInfo)user;
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

