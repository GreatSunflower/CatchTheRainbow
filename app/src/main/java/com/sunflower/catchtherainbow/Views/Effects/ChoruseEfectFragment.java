package com.sunflower.catchtherainbow.Views.Effects;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sunflower.catchtherainbow.R;
import com.sunflower.catchtherainbow.Views.Helpful.DetailedSeekBar;
import com.sunflower.catchtherainbow.Views.NDSpinner;
import com.un4seen.bass.BASS;

import java.util.ArrayList;
import java.util.Collections;

public class ChoruseEfectFragment extends BaseEffectFragment
        implements DetailedSeekBar.OnSuperSeekBarListener, AdapterView.OnItemSelectedListener
{
    public ChoruseEfectFragment()
    {
        // Required empty public constructor
    }

    public static ChoruseEfectFragment newInstance()
    {
        ChoruseEfectFragment fragment = new ChoruseEfectFragment();
        return fragment;
    }

    DetailedSeekBar sb_chorusefWetDryMix, sb_chorusefDepth, sb_chorusefFeedback, sb_chorusefFrequency, sb_chorusefDelay;

    private NDSpinner ndSpinlWaveform; // 0=triangle, 1=sine
    private String[] spinner_array;
    private ArrayAdapter<String> spinIPanDelayAdapter;

    private int choruse;
    BASS.BASS_DX8_CHORUS bass_dx8_chorus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_choruse_efect, container, false);

        sb_chorusefWetDryMix = (DetailedSeekBar) root.findViewById(R.id.chorusefWetDryMixSeekBar);
        sb_chorusefDepth = (DetailedSeekBar) root.findViewById(R.id.chorusefDepthSeekBar);
        sb_chorusefFeedback = (DetailedSeekBar) root.findViewById(R.id.chorusefFeedbackSeekBar);
        sb_chorusefFrequency = (DetailedSeekBar) root.findViewById(R.id.chorusefFrequencySeekBar);
        sb_chorusefDelay = (DetailedSeekBar) root.findViewById(R.id.chorusefDelaySeekBar);

        //------------------------------spinner-----------------

        spinner_array = getResources().getStringArray(R.array.lwaveform_array);
        ndSpinlWaveform = (NDSpinner) root.findViewById(R.id.spinnerlWaveform);

        ArrayList<String> items = new ArrayList<String>();
        Collections.addAll(items, spinner_array);

        ndSpinlWaveform = (NDSpinner) root.findViewById(R.id.spinnerlWaveform);
        spinIPanDelayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, items);
        spinIPanDelayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ndSpinlWaveform.setAdapter(spinIPanDelayAdapter);

        //---------------------------------end spinner-----------------------

        sb_chorusefWetDryMix.setListener(this);
        sb_chorusefDepth.setListener(this);
        sb_chorusefFeedback.setListener(this);
        sb_chorusefFrequency.setListener(this);
        sb_chorusefDelay.setListener(this);
        ndSpinlWaveform.setOnItemSelectedListener(this);

        return root;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View arg1, int position, long id)
    {
        //ndSpinlWaveform.getSelectedItem().toString().toLowerCase();
        if(position == 0) bass_dx8_chorus.lWaveform = 0;
        else bass_dx8_chorus.lWaveform = 1;

        ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.colorForeground));
        ((TextView) parent.getChildAt(0)).setTextSize(18);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView){}

    public void setEffect()
    {
        choruse = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_ECHO, 0);
        bass_dx8_chorus =new BASS.BASS_DX8_CHORUS();
        BASS.BASS_FXSetParameters(choruse, bass_dx8_chorus);
    }

    public boolean cancel() //при закрытии окна
    {
        BASS.BASS_ChannelRemoveFX(chan, choruse);
        return true;
        // close only this fragment!
        //getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public void onChange(DetailedSeekBar seekBar, float selectedValue)
    {
        double res = (double)selectedValue;

        int id = seekBar.getId();

        ////////////////start ECHO//////////////////

        if(id == R.id.chorusefWetDryMixSeekBar)
        {
            bass_dx8_chorus.fWetDryMix = (float) res;
            BASS.BASS_FXSetParameters(choruse, bass_dx8_chorus);
        }
        if(id == R.id.chorusefDepthSeekBar)
        {
            bass_dx8_chorus.fDepth = (float) res;
            BASS.BASS_FXSetParameters(choruse, bass_dx8_chorus);
        }
        else if(id ==  R.id.chorusefFeedbackSeekBar)
        {
            bass_dx8_chorus.fFeedback = (float) res;
            BASS.BASS_FXSetParameters(choruse, bass_dx8_chorus);
        }
        else if(id ==  R.id.chorusefFrequencySeekBar)
        {
            bass_dx8_chorus.fFrequency = (float) res;
            BASS.BASS_FXSetParameters(choruse, bass_dx8_chorus);
        }
        else if(id ==  R.id.chorusefDelaySeekBar)
        {
            bass_dx8_chorus.fDelay = (float) res;
            BASS.BASS_FXSetParameters(choruse, bass_dx8_chorus);
        }
    }
}
