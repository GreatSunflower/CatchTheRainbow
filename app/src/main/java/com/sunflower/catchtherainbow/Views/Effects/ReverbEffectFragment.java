package com.sunflower.catchtherainbow.Views.Effects;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sunflower.catchtherainbow.R;
import com.sunflower.catchtherainbow.Views.Helpful.DetailedSeekBar;
import com.un4seen.bass.BASS;

public class ReverbEffectFragment extends BaseEffectFragment implements DetailedSeekBar.OnSuperSeekBarListener
{
    public ReverbEffectFragment()
    {
        // Required empty public constructor
    }

    public static ReverbEffectFragment newInstance()
    {
        ReverbEffectFragment fragment = new ReverbEffectFragment();
        return fragment;
    }

    //lChannel
    DetailedSeekBar sb_reverbfInGain, sb_reverbfReverbMix, sb_reverbfReverbTime, sb_reverbfHighFreqRTRatio;
    private int reverb;
    BASS.BASS_DX8_REVERB bass_bfx_reverb;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.effect_reverb_fragment, container, false);

        sb_reverbfInGain = (DetailedSeekBar) root.findViewById(R.id.reverbfInGainSeekBar);
        sb_reverbfReverbMix = (DetailedSeekBar) root.findViewById(R.id.reverbfReverbMixSeekBar);
        sb_reverbfReverbTime = (DetailedSeekBar) root.findViewById(R.id.reverbfReverbTimeSeekBar);
        sb_reverbfHighFreqRTRatio = (DetailedSeekBar) root.findViewById(R.id.reverbfHighFreqRTRatioSeekBar);

        sb_reverbfInGain.setListener(this);
        sb_reverbfReverbMix.setListener(this);
        sb_reverbfReverbTime.setListener(this);
        sb_reverbfHighFreqRTRatio.setListener(this);

        return root;
    }

    @Override
    public void setChannel(int chan)
    {
        if(this.chan == chan) return;

        this.chan = chan;

        if(bass_bfx_reverb == null)
            setEffect();
        else
        {
            reverb = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_REVERB, 0);
            BASS.BASS_FXSetParameters(reverb, bass_bfx_reverb);
        }
    }

    public boolean cancel() //при закрытии окна
    {
        BASS.BASS_ChannelRemoveFX(chan, reverb);
        return true;
    }

    public void setEffect()
    {
        reverb = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_REVERB, 0);
        bass_bfx_reverb =new BASS.BASS_DX8_REVERB();
        bass_bfx_reverb.fInGain = 0;
        bass_bfx_reverb.fReverbMix = 0;
        bass_bfx_reverb.fReverbTime = 1000;
        bass_bfx_reverb.fHighFreqRTRatio = (float) 0.001;
        BASS.BASS_FXSetParameters(reverb, bass_bfx_reverb);
    }

    @Override
    public void onChange(DetailedSeekBar seekBar, float selectedValue)
    {
        double res = (double)selectedValue;

        int id = seekBar.getId();

        ///////////////start FLANGER///////////

        if(id ==  R.id.reverbfInGainSeekBar)
        {
            bass_bfx_reverb.fInGain = (float) res;
            BASS.BASS_FXSetParameters(reverb, bass_bfx_reverb);
        }
        else if(id ==  R.id.reverbfReverbMixSeekBar)
        {
            bass_bfx_reverb.fReverbMix = (float) res;
            BASS.BASS_FXSetParameters(reverb, bass_bfx_reverb);
        }
        else if(id ==  R.id.reverbfReverbTimeSeekBar)
        {
            bass_bfx_reverb.fReverbTime = (float) res;
            BASS.BASS_FXSetParameters(reverb, bass_bfx_reverb);
        }
        else if(id ==  R.id.reverbfHighFreqRTRatioSeekBar)
        {
            bass_bfx_reverb.fHighFreqRTRatio = (float) res;
            BASS.BASS_FXSetParameters(reverb, bass_bfx_reverb);
        }
    }
}
