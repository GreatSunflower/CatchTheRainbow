package com.sunflower.catchtherainbow.Views.Effects;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.sunflower.catchtherainbow.R;
import com.sunflower.catchtherainbow.Views.Helpful.DetailedSeekBar;
import com.un4seen.bass.BASS;

public class EchoEffectFragment extends BaseEffectFragment implements DetailedSeekBar.OnSuperSeekBarListener, CompoundButton.OnCheckedChangeListener
{
    public EchoEffectFragment() {/*Required empty public constructor*/}

    // TODO: Rename and change types and number of parameters
    public static EchoEffectFragment newInstance()
    {
        EchoEffectFragment fragment = new EchoEffectFragment();
        return fragment;
    }

    /////////////////////////////////////ECHO//////////////////////////////////////

    DetailedSeekBar sb_echofWetDryMix, sb_echofFeedback, sb_echofLeftDelay, sb_echofRightDelay;
    Switch sw_echoIPanDelay;
    private int echo;
    BASS.BASS_DX8_ECHO bass_dx8_echo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_echo_effect, container, false);

        /////////////////////////////////////start ECHO//////////////////////////////////////

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
        return root;
    }

    public void setEffect()
    {
        echo = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_ECHO, 0);
        bass_dx8_echo =new BASS.BASS_DX8_ECHO();
        bass_dx8_echo.fWetDryMix = 0;
        bass_dx8_echo.fFeedback = 0;
        bass_dx8_echo.fLeftDelay = 500;
        bass_dx8_echo.fRightDelay = 500;
        BASS.BASS_FXSetParameters(echo, bass_dx8_echo);
    }

    public void cancel() //при закрытии окна
    {
        BASS.BASS_ChannelRemoveFX(chan, echo);
        // close only this fragment!
        //getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

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
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked)
    {
        if (isChecked) {bass_dx8_echo.lPanDelay = true;}
        else {bass_dx8_echo.lPanDelay = false;}

        BASS.BASS_FXSetParameters(echo, bass_dx8_echo);
    }
}
