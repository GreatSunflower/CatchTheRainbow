package com.sunflower.catchtherainbow.Views.Effects;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.sunflower.catchtherainbow.R;
import com.sunflower.catchtherainbow.Views.Helpful.DetailedSeekBar;
import com.un4seen.bass.BASS;
import com.un4seen.bass.BASS_FX;

/**
 * Created by SuperComputer on 2/7/2017.
 */

public class DefaultEffectsFragment extends Fragment implements DetailedSeekBar.OnSuperSeekBarListener, CompoundButton.OnCheckedChangeListener
{
    private int chan = 0;
    public DefaultEffectsFragment()
    {
        // Required empty public constructor
    }

    public static DefaultEffectsFragment newInstance(int chan)
    {
        DefaultEffectsFragment fragment = new DefaultEffectsFragment();
        fragment.chan = chan;
        return fragment;
        // Required empty public constructor
    }

    /////////////////////////////////////ECHO//////////////////////////////////////

    DetailedSeekBar sb_echofWetDryMix, sb_echofFeedback, sb_echofLeftDelay, sb_echofRightDelay;
    Switch sw_echoIPanDelay;
    private int echo;
    BASS.BASS_DX8_ECHO bass_dx8_echo;

    /////////////////////////////////////FLANGER//////////////////////////////////////
    DetailedSeekBar sb_flangerfWetDryMix, sb_flangerfDepth, sb_flangerfFeedback, sb_flangerfFrequency, sb_flangerfDelay;
    private int flanger;
    BASS.BASS_DX8_FLANGER bass_dx8_flanger;

    /////////////////////////////////////start PHASER//////////////////////////////////////
    //lChannel
    DetailedSeekBar sb_phaserfDryMix, sb_phaserfWetMix, sb_phaserfFeedback, sb_phaserfRate, sb_phaserfRange, sb_phaserfFreq;
    private int phaser;
    BASS_FX.BASS_BFX_PHASER bass_bfx_phaser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.effects_default_fragment, container, false);

        /////////////////////////////////////start ECHO//////////////////////////////////////
        /*
        fWetDryMix	Ratio of wet (processed) signal to dry (unprocessed) signal. Must be in the range from 0 through 100 (all wet). The default value is 50.
        fFeedback	Percentage of output fed back into input, in the range from 0 through 100. The default value is 50.
        fLeftDelay	Delay for left channel, in milliseconds, in the range from 1 through 2000. The default value is 500 ms.
        fRightDelay	Delay for right channel, in milliseconds, in the range from 1 through 2000. The default value is 500 ms.
        lPanDelay	Value that specifies whether to swap left and right delays with each successive echo. The default value is FALSE, meaning no swap.*/

        sb_echofWetDryMix = (DetailedSeekBar) root.findViewById(R.id.echofWetDryMixSeekBar);
        sb_echofFeedback = (DetailedSeekBar) root.findViewById(R.id.echofFeedbackSeekBar);
        sb_echofLeftDelay = (DetailedSeekBar) root.findViewById(R.id.echofLeftDelaySeekBar);
        sb_echofRightDelay = (DetailedSeekBar) root.findViewById(R.id.echofRightDelaySeekBar);
        sw_echoIPanDelay = (Switch) root.findViewById(R.id.switch_panDalay);

        sb_echofWetDryMix.setListener(this);
        sb_echofFeedback.setListener(this);
        sb_echofLeftDelay.setListener(this);
        sb_echofRightDelay.setListener(this);
        sw_echoIPanDelay.setOnCheckedChangeListener(this);

        echo = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_ECHO, 0);
        bass_dx8_echo =new BASS.BASS_DX8_ECHO();
        bass_dx8_echo.fWetDryMix = 0;
        bass_dx8_echo.fFeedback = 0;
        bass_dx8_echo.fLeftDelay = 500;
        bass_dx8_echo.fRightDelay = 500;
        BASS.BASS_FXSetParameters(echo, bass_dx8_echo);
        //BASS.BASS_ChannelRemoveFX(chan, echo); //при закрытии окна

        /////////////////////////////////////start FLANGER//////////////////////////////////////
        /*
        fWetDryMix	Ratio of wet (processed) signal to dry (unprocessed) signal. Must be in the range from 0 through 100 (all wet). The default value is 50.
        fDepth	Percentage by which the delay time is modulated by the low-frequency oscillator (LFO). Must be in the range from 0 through 100. The default value is 100.
        fFeedback	Percentage of output signal to feed back into the effect's input, in the range from -99 to 99. The default value is -50.
        fFrequency	Frequency of the LFO, in the range from 0 to 10. The default value is 0.25.
        lWaveform	Waveform of the LFO... 0 = triangle, 1 = sine. By default, the waveform is sine.
        fDelay	Number of milliseconds the input is delayed before it is played back, in the range from 0 to 4. The default value is 2 ms.
        lPhase	Phase differential between left and right LFOs, one of BASS_DX8_PHASE_NEG_180, BASS_DX8_PHASE_NEG_90, BASS_DX8_PHASE_ZERO, BASS_DX8_PHASE_90 and BASS_DX8_PHASE_180. The default value is BASS_DX8_PHASE_ZERO.*/

        sb_flangerfWetDryMix = (DetailedSeekBar) root.findViewById(R.id.flangerfWetDryMixSeekBar);
        sb_flangerfDepth = (DetailedSeekBar) root.findViewById(R.id.flangerfDepthSeekBar);
        sb_flangerfFeedback = (DetailedSeekBar) root.findViewById(R.id.flangerfFeedbackSeekBar);
        sb_flangerfFrequency = (DetailedSeekBar) root.findViewById(R.id.flangerfFrequencySeekBar);
        sb_flangerfDelay = (DetailedSeekBar) root.findViewById(R.id.flangerfDelaySeekBar);

        /* DWORD lWaveform; DWORD lPhase;*/

        sb_flangerfWetDryMix.setListener(this);
        sb_flangerfDepth.setListener(this);
        sb_flangerfFeedback.setListener(this);
        sb_flangerfFrequency.setListener(this);
        sb_flangerfDelay.setListener(this);

        flanger = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_FLANGER, 0);
        bass_dx8_flanger =new BASS.BASS_DX8_FLANGER();
        //bass_dx8_flanger.fWetDryMix = 0;
        BASS.BASS_FXSetParameters(flanger, bass_dx8_flanger);

        /////////////////////////////////////start PHASER//////////////////////////////////////
        /*
        float fDryMix;	 // dry (unaffected) signal mix			 [-2......2]
		float fWetMix;	 // wet (affected) signal mix			 [-2......2]
		float fFeedback; // output signal to feed back into input [-1......1]
		float fRate;	 // rate of sweep in cycles per second	 [0<....<10]
		float fRange;	 // sweep range in octaves				 [0<....<10]
		float fFreq;	 // base frequency of sweep				 [0<...1000]
		int   lChannel;	 // BASS_BFX_CHANxxx flag/s*/

        sb_phaserfDryMix = (DetailedSeekBar) root.findViewById(R.id.phaserfDryMixSeekBar);
        sb_phaserfWetMix = (DetailedSeekBar) root.findViewById(R.id.phaserfWetMixSeekBar);
        sb_phaserfFeedback = (DetailedSeekBar) root.findViewById(R.id.phaserfFeedbackSeekBar);
        sb_phaserfRate = (DetailedSeekBar) root.findViewById(R.id.phaserfRateSeekBar);
        sb_phaserfRange = (DetailedSeekBar) root.findViewById(R.id.phaserfRangeSeekBar);
        sb_phaserfFreq = (DetailedSeekBar) root.findViewById(R.id.phaserfFreqSeekBar);

        sb_phaserfDryMix.setListener(this);
        sb_phaserfWetMix.setListener(this);
        sb_phaserfFeedback.setListener(this);
        sb_phaserfRate.setListener(this);
        sb_phaserfRange.setListener(this);
        sb_phaserfFreq.setListener(this);

        /*flanger = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_FLANGER, 0);
        bass_dx8_flanger =new BASS.BASS_DX8_FLANGER();
        //bass_dx8_flanger.fWetDryMix = 0;
        BASS.BASS_FXSetParameters(flanger, bass_dx8_flanger);*/

        //BASS_FX_BFX_PHASER
        //BASS_BFX_PHASER
        /*phaser = BASS_FX.BASS_se();//.BASS_ChannelSetFX(chan, BASS_FX.BASS_FX_BFX_PHASER, 0);
        bass_bfx_phaser =new BASS_FX.BASS_BFX_PHASER();
        //bass_dx8_flanger.fWetDryMix = 0;
        bass_bfx_phaser = BASS_FX.BASS_FX_BPM_BeatSetParameters(flanger, 0, 0, 0);*/

        return root;
    }

    /*public void setEffects(DelayEffect delayEffect, RateTransposer rateTransposer, FlangerEffect flangerEffect)
    {
        this.delayEffect = delayEffect;
        this.rateTransposer = rateTransposer;
        this.flangerEffect = flangerEffect;
    }*/

    @Override
    public void onChange(DetailedSeekBar seekBar, float selectedValue)
    {
        double res = (double)selectedValue;

        int id = seekBar.getId();

        ////////////////start ECHO//////////////////

        if(id == R.id.echofWetDryMixSeekBar)
        {
            //BASS.BASS_FXReset(echo);
            //BASS.BASS_DX8_ECHO bass_dx8_echo = new BASS.BASS_DX8_ECHO();
            //BASS.BASS_FXGetParameters(echo, bass_dx8_echo);
            bass_dx8_echo.fWetDryMix = (float) res;
            BASS.BASS_FXSetParameters(echo, bass_dx8_echo);
        }
        if(id == R.id.echofFeedbackSeekBar)
        {
            bass_dx8_echo.fFeedback = (float) res;
            BASS.BASS_FXSetParameters(echo, bass_dx8_echo);
        }
        else if(id ==  R.id.echofLeftDelaySeekBar)
        {
            bass_dx8_echo.fLeftDelay = (float) res;
            BASS.BASS_FXSetParameters(echo, bass_dx8_echo);
        }
        else if(id ==  R.id.echofRightDelaySeekBar)
        {
            bass_dx8_echo.fRightDelay = (float) res;
            BASS.BASS_FXSetParameters(echo, bass_dx8_echo);
        }

        ///////////////start FLANGER///////////

        else if(id ==  R.id.flangerfWetDryMixSeekBar)
        {
            bass_dx8_flanger.fWetDryMix = (float) res;
            BASS.BASS_FXSetParameters(flanger, bass_dx8_flanger);
        }
        else if(id ==  R.id.flangerfDepthSeekBar)
        {
            bass_dx8_flanger.fDepth = (float) res;
            BASS.BASS_FXSetParameters(flanger, bass_dx8_flanger);
        }
        else if(id ==  R.id.flangerfFeedbackSeekBar)
        {
            bass_dx8_flanger.fFeedback = (float) res;
            BASS.BASS_FXSetParameters(flanger, bass_dx8_flanger);
        }
        else if(id ==  R.id.flangerfFrequencySeekBar)
        {
            bass_dx8_flanger.fFrequency = (float) res;
            BASS.BASS_FXSetParameters(flanger, bass_dx8_flanger);
        }
        else if(id ==  R.id.flangerfDelaySeekBar)
        {
            bass_dx8_flanger.fDelay = (float) res;
            BASS.BASS_FXSetParameters(flanger, bass_dx8_flanger);
        }

        /////////////////start PHASER///////////////


    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        if (isChecked) {bass_dx8_echo.lPanDelay = true;}
        else {bass_dx8_echo.lPanDelay = false;}

        BASS.BASS_FXSetParameters(echo, bass_dx8_echo);
    }
}
