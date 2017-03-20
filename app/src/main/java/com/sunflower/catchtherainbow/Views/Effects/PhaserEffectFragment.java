package com.sunflower.catchtherainbow.Views.Effects;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sunflower.catchtherainbow.R;
import com.sunflower.catchtherainbow.Views.Helpful.DetailedSeekBar;
import com.un4seen.bass.BASS;
import com.un4seen.bass.BASS_FX;


public class PhaserEffectFragment extends BaseEffectFragment implements DetailedSeekBar.OnSuperSeekBarListener
{
    public PhaserEffectFragment()
    {
        // Required empty public constructor
    }

    public static PhaserEffectFragment newInstance()
    {
        PhaserEffectFragment fragment = new PhaserEffectFragment();
        return fragment;
    }

    /////////////////////////////////////start PHASER//////////////////////////////////////
    //lChannel
    DetailedSeekBar sb_phaserfDryMix, sb_phaserfWetMix, sb_phaserfFeedback, sb_phaserfRate, sb_phaserfRange, sb_phaserfFreq;
    private int phaser;
    BASS_FX.BASS_BFX_PHASER bass_bfx_phaser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.effect_phaser_fragment, container, false);

        /////////////////////////////////////start PHASER//////////////////////////////////////

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

        return root;
    }

    public boolean cancel() //при закрытии окна
    {
        BASS.BASS_ChannelRemoveFX(chan, phaser);
        return true;
        // close only this fragment!
        //getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    public void setEffect()
    {
        phaser = BASS.BASS_ChannelSetFX(chan, BASS_FX.BASS_FX_BFX_PHASER, 1);
        bass_bfx_phaser =new BASS_FX.BASS_BFX_PHASER();
        BASS.BASS_FXSetParameters(phaser, bass_bfx_phaser);

        //BASS.BASS_ChannelRemoveFX(chan, phaser); //при закрытии окна
    }

    @Override
    public void onChange(DetailedSeekBar seekBar, float selectedValue)
    {
        double res = (double)selectedValue;

        int id = seekBar.getId();

        /////////////////start PHASER///////////////

        if(id ==  R.id.phaserfDryMixSeekBar)
        {
            bass_bfx_phaser.fDryMix = (float) res;
            BASS.BASS_FXSetParameters(phaser, bass_bfx_phaser);
            int t = BASS.BASS_ErrorGetCode();
            Toast.makeText(getContext(),  t + "", Toast.LENGTH_SHORT).show();
        }
        else if(id ==  R.id.phaserfWetMixSeekBar)
        {
            bass_bfx_phaser.fWetMix = (float) res;
            BASS.BASS_FXSetParameters(phaser, bass_bfx_phaser);
        }
        else if(id ==  R.id.phaserfFeedbackSeekBar)
        {
            bass_bfx_phaser.fFeedback = (float) res;
            BASS.BASS_FXSetParameters(phaser, bass_bfx_phaser);
        }
        else if(id ==  R.id.phaserfRateSeekBar)
        {
            bass_bfx_phaser.fRate = (float) res;
            BASS.BASS_FXSetParameters(phaser, bass_bfx_phaser);
        }
        else if(id ==  R.id.phaserfRangeSeekBar)
        {
            bass_bfx_phaser.fRange = (float) res;
            BASS.BASS_FXSetParameters(phaser, bass_bfx_phaser);
        }
        else if(id ==  R.id.phaserfFreqSeekBar)
        {
            bass_bfx_phaser.fFreq = (float) res;
            BASS.BASS_FXSetParameters(phaser, bass_bfx_phaser);
        }
    }
}
