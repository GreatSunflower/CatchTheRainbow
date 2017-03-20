package com.sunflower.catchtherainbow.Views.Effects;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sunflower.catchtherainbow.R;
import com.sunflower.catchtherainbow.Views.Helpful.CircularSeekBar;
import com.sunflower.catchtherainbow.Views.Helpful.DetailedSeekBar;
import com.un4seen.bass.BASS;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
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

    public void setEffect()
    {
        distortion = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_DISTORTION, 0);
        bass_dx8_distortion = new BASS.BASS_DX8_DISTORTION();
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
