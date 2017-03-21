package com.sunflower.catchtherainbow.Views.Effects;


import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sunflower.catchtherainbow.R;
import com.sunflower.catchtherainbow.Views.Helpful.DetailedSeekBar;
import com.sunflower.catchtherainbow.Views.Helpful.NDSpinner;
import com.un4seen.bass.BASS;

import java.util.ArrayList;
import java.util.Collections;

public class EqualizerEffectFragment extends BaseEffectFragment implements DetailedSeekBar.OnSuperSeekBarListener
{
    private DetailedSeekBar fGainEq;// _80hz, _160hz, _320hz, _640hz, _1khz, _2khz, _4khz, _8khz, _16khz;

    private int []freq = new int[]{80, 160, 320, 640, 1000, 2000, 4000, 8000, 16000};

    private NDSpinner spinFilter;
    private String[] spinner_array;
    private ArrayAdapter<String> spinFilterAdapter;

    int equalizer;
    BASS.BASS_DX8_PARAMEQ bass_dx8_equalizer;
    int[] fx = new int[9];

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
        spinFilterAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, items);
        spinFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinFilter.setAdapter(spinFilterAdapter);
        // ѕрограммный выбор пункта выпадающего списка
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

        LinearLayout freqContainer = (LinearLayout)root.findViewById(R.id.container);

        for(int i = 0; i < fx.length; i++)
        {
            fx[i] = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_PARAMEQ, 0);

            BASS.BASS_DX8_PARAMEQ p = new BASS.BASS_DX8_PARAMEQ();
            //p.fBandwidth = 18;
            p.fGain = 0;
            p.fCenter = freq[i];
            BASS.BASS_FXSetParameters(fx[i], p);

            LinearLayout layout = new LinearLayout(getActivity());
            layout.setOrientation(LinearLayout.HORIZONTAL);

            // band level string. clever progression. LOL! =D
            String res = freq[i] >= 1000? (freq[i] / 1000)+"Khz": freq[i] + "Hz";

            // band text view
            TextView freqText = new TextView(getActivity());

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.weight = 0.15f;
            freqText.setLayoutParams(layoutParams);

            freqText.setTextColor(getResources().getColor(R.color.colorForeground));
            freqText.setText(res);
            freqText.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
            freqText.setPadding(2,2,2,2);

            // seekbar to tweak value
            DetailedSeekBar freqSlider = new DetailedSeekBar(getActivity());
            freqSlider.setMinValue(-15);
            freqSlider.setMaxValue(15);
            freqSlider.setDefaultValue(0);
            freqSlider.setTag(i);

            layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.weight = 0.85f;
            freqSlider.setLayoutParams(layoutParams);
            freqSlider.setGravity(Gravity.CENTER);

            // add view to the container
            layout.addView(freqText);
            layout.addView(freqSlider);

            freqContainer.addView(layout);
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
