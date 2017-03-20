package com.sunflower.catchtherainbow.Views.Effects;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sunflower.catchtherainbow.R;
import com.sunflower.catchtherainbow.Views.Helpful.DetailedSeekBar;
import com.un4seen.bass.BASS;

public class FlangerEffectFragment extends BaseEffectFragment implements DetailedSeekBar.OnSuperSeekBarListener
{
    public FlangerEffectFragment()
    {
        // Required empty public constructor
    }

    public static FlangerEffectFragment newInstance()
    {
        FlangerEffectFragment fragment = new FlangerEffectFragment();
        return fragment;
    }

    /////////////////////////////////////FLANGER//////////////////////////////////////
    DetailedSeekBar sb_flangerfWetDryMix, sb_flangerfDepth, sb_flangerfFeedback, sb_flangerfFrequency, sb_flangerfDelay;
    private int flanger;
    BASS.BASS_DX8_FLANGER bass_dx8_flanger;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.effect_flanger_fragment, container, false);

        /////////////////////////////////////start FLANGER//////////////////////////////////////
        sb_flangerfWetDryMix = (DetailedSeekBar) root.findViewById(R.id.flangerfWetDryMixSeekBar);
        sb_flangerfDepth = (DetailedSeekBar) root.findViewById(R.id.flangerfDepthSeekBar);
        sb_flangerfFeedback = (DetailedSeekBar) root.findViewById(R.id.flangerfFeedbackSeekBar);
        sb_flangerfFrequency = (DetailedSeekBar) root.findViewById(R.id.flangerfFrequencySeekBar);
        sb_flangerfDelay = (DetailedSeekBar) root.findViewById(R.id.flangerfDelaySeekBar);

        sb_flangerfWetDryMix.setListener(this);
        sb_flangerfDepth.setListener(this);
        sb_flangerfFeedback.setListener(this);
        sb_flangerfFrequency.setListener(this);
        sb_flangerfDelay.setListener(this);

        /* DWORD lWaveform; DWORD lPhase;*/
        return root;
    }

    public boolean cancel() //при закрытии окна
    {
        BASS.BASS_ChannelRemoveFX(chan, flanger);
        return true;
        // close only this fragment!
        //getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    public void setEffect()
    {
        flanger = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_FLANGER, 0);
        bass_dx8_flanger =new BASS.BASS_DX8_FLANGER();
        //bass_dx8_flanger.fWetDryMix = 0;
        BASS.BASS_FXSetParameters(flanger, bass_dx8_flanger);
    }

    @Override
    public void onChange(DetailedSeekBar seekBar, float selectedValue)
    {
        double res = (double)selectedValue;

        int id = seekBar.getId();

        ///////////////start FLANGER///////////

        if(id ==  R.id.flangerfWetDryMixSeekBar)
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
    }
}
