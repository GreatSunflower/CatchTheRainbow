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
import com.sunflower.catchtherainbow.AudioClasses.TrackInfo;
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

        new ApplyEffectTask(getActivity(), getResources().getString(R.string.effect_apply_message), player.getTracks().get(0).getTrack(), BASS.BASS_FX_DX8_DISTORTION, bass_dx8_distortion, range ).execute();

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

