package com.sunflower.catchtherainbow.Views.Effects;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sunflower.catchtherainbow.R;
import com.sunflower.catchtherainbow.Views.Helpful.DetailedSeekBar;

import be.tarsos.dsp.PitchShifter;
import be.tarsos.dsp.effects.DelayEffect;
import be.tarsos.dsp.effects.FlangerEffect;
import be.tarsos.dsp.resample.RateTransposer;

/**
 * Created by SuperComputer on 2/7/2017.
 */

public class DefaultEffectsFragment extends Fragment implements DetailedSeekBar.OnSuperSeekBarListener
{
    DelayEffect delayEffect;
    RateTransposer rateTransposer;
    FlangerEffect flangerEffect;

    public DefaultEffectsFragment()
    {
        // Required empty public constructor
    }

    public static DefaultEffectsFragment newInstance()
    {
        DefaultEffectsFragment fragment = new DefaultEffectsFragment();
        return fragment;
        // Required empty public constructor
    }

    DetailedSeekBar sb_echo, sb_decay, sb_rateTransposer, sb_flangerWetness, sb_flangerDryness, sb_frangerLFO;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.effects_default_fragment, container, false);

        sb_echo = (DetailedSeekBar) root.findViewById(R.id.echoLengthSeekBar);
        sb_decay = (DetailedSeekBar) root.findViewById(R.id.sb_Decay);
        sb_rateTransposer = (DetailedSeekBar) root.findViewById(R.id.sb_RateTransposer);

        // Flanger
        sb_flangerWetness = (DetailedSeekBar) root.findViewById(R.id.sb_FlangerWetness);
        sb_flangerDryness = (DetailedSeekBar) root.findViewById(R.id.sb_FlangerDryness);
        sb_frangerLFO = (DetailedSeekBar) root.findViewById(R.id.sb_FlangerLFO);

        sb_echo.setListener(this);
        sb_decay.setListener(this);
        sb_rateTransposer.setListener(this);

        sb_flangerWetness.setListener(this);
        sb_flangerDryness.setListener(this);
        sb_frangerLFO.setListener(this);

        return root;
    }

    public void setEffects(DelayEffect delayEffect, RateTransposer rateTransposer, FlangerEffect flangerEffect)
    {
        this.delayEffect = delayEffect;
        this.rateTransposer = rateTransposer;
        this.flangerEffect = flangerEffect;
    }

    @Override
    public void onChange(DetailedSeekBar seekBar, float selectedValue)
    {
        double res = (double)selectedValue;

        int id = seekBar.getId();

        if(id == R.id.echoLengthSeekBar)
        {
            delayEffect.setEchoLength(res);
        }
        else if(id ==  R.id.sb_Decay)
        {
            delayEffect.setDecay(res);
        }
        else if(id ==  R.id.sb_RateTransposer)
        {
            rateTransposer.setFactor(res);
        }
        else if(id ==  R.id.sb_FlangerDryness)
        {
            flangerEffect.setDry(res);
        }
        else if(id ==  R.id.sb_FlangerWetness)
        {
            flangerEffect.setWet(res);
        }
        else if(id ==  R.id.sb_FlangerLFO)
        {
            flangerEffect.setLFOFrequency(res);
        }

    }
}
