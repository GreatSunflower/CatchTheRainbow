package com.sunflower.catchtherainbow.Views.Effects;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.sunflower.catchtherainbow.R;
import com.sunflower.catchtherainbow.Views.Helpful.DetailedSeekBar;
import com.sunflower.catchtherainbow.Views.Helpful.NDSpinner;
import com.un4seen.bass.BASS;

import java.util.ArrayList;
import java.util.Collections;

public class EqualizerEffectFragment extends BaseEffectFragment implements DetailedSeekBar.OnSuperSeekBarListener
{
    public EqualizerEffectFragment()
    {
        // Required empty public constructor
    }

    public static EqualizerEffectFragment newInstance(String param1, String param2)
    {
        EqualizerEffectFragment fragment = new EqualizerEffectFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    private DetailedSeekBar fGainEq, _32db, _64db, _125db, _250db, _500db, _1k, _2k, _4k, _8k;

    private NDSpinner spinFilter;
    private String[] spinner_array;
    private ArrayAdapter<String> spinFilterAdapter;

    int equalizer;
    BASS.BASS_DX8_PARAMEQ bass_dx8_equalizer;
    int[] fx = new int[9];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.effect_equalizer_fragment, container, false);

        spinner_array = getResources().getStringArray(R.array.spinner_array);
        //------------------------------spinner Filter-----------------

        ArrayList<String> items = new ArrayList<String>();
        Collections.addAll(items, spinner_array);

        spinFilter = (NDSpinner) root.findViewById(R.id.spinnerEqualizer);
        // 2 - шаблон дл показа выбранного пункта в выпадающем списке
        spinFilterAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, items);
        // задание шаблона для выпадающих пунктов списка
        spinFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinFilter.setAdapter(spinFilterAdapter);
        // ѕрограммный выбор пункта выпадающего списка
        //spinner.setSelection(2);
        spinFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id)
            {
                //spinFilter.getSelectedItem().toString().toLowerCase();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        //---------------------------------end spinnerFilter-----------------------

        fGainEq = (DetailedSeekBar) root.findViewById(R.id.fGain_equalizer);
        _32db = (DetailedSeekBar) root.findViewById(R.id._32db);
        _64db = (DetailedSeekBar) root.findViewById(R.id._64db);
        _125db = (DetailedSeekBar) root.findViewById(R.id._125db);
        _250db = (DetailedSeekBar) root.findViewById(R.id._250db);
        _500db = (DetailedSeekBar) root.findViewById(R.id._500db);
        _1k = (DetailedSeekBar) root.findViewById(R.id._1k);
        _2k = (DetailedSeekBar) root.findViewById(R.id._2k);
        _4k = (DetailedSeekBar) root.findViewById(R.id._4k);
        _8k = (DetailedSeekBar) root.findViewById(R.id._8k);


        fGainEq.setListener(this);
        _32db.setListener(this);
        _64db.setListener(this);
        _125db.setListener(this);
        _250db.setListener(this);
        _500db.setListener(this);
        _1k.setListener(this);
        _2k.setListener(this);
        _4k.setListener(this);
        _8k.setListener(this);

        int db = 32;
        for(int i = 0; i < fx.length; i++)
        {
            fx[i] = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_PARAMEQ, 0);

            BASS.BASS_DX8_PARAMEQ p = new BASS.BASS_DX8_PARAMEQ();
            //p.fBandwidth = 18;
            p.fGain = 0;
            p.fCenter = db;
            BASS.BASS_FXSetParameters(fx[i], p);
            db *= 2;
            if(db == 128) db -= 3;
        }
        return root;
    }

    public void setEffect()
    {
        equalizer = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_PARAMEQ, 0);
        bass_dx8_equalizer = new BASS.BASS_DX8_PARAMEQ();
        BASS.BASS_FXSetParameters(equalizer, bass_dx8_equalizer);
    }

    public boolean cancel() //при закрытии окна
    {
        BASS.BASS_ChannelRemoveFX(chan, equalizer);
        return true;
    }

    @Override
    public void onChange(DetailedSeekBar seekBar, float selectedValue)
    {
        double res = (double)selectedValue;

        int id = seekBar.getId();

        if(id == R.id.fGain_equalizer)
        {
            bass_dx8_equalizer.fGain = (float) res;
            BASS.BASS_FXSetParameters(equalizer, bass_dx8_equalizer);
        }
        else
        {
            int n = Integer.parseInt((String) seekBar.getTag());
            BASS.BASS_DX8_PARAMEQ p = new BASS.BASS_DX8_PARAMEQ();
            p.fGain = (float) res;
            BASS.BASS_FXGetParameters(fx[n], p);
        }
    }
}
