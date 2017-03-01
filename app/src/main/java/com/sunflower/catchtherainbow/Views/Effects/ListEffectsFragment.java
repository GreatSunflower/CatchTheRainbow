package com.sunflower.catchtherainbow.Views.Effects;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sunflower.catchtherainbow.Adapters.EffectsAdapter;
import com.sunflower.catchtherainbow.Adapters.ItemEffect;
import com.sunflower.catchtherainbow.R;

import java.util.ArrayList;

/**
 * Created by SuperComputer on 2/7/2017.
 */

public class ListEffectsFragment extends BaseEffectFragment implements AdapterView.OnItemClickListener
{
    public ListEffectsFragment()
    {
        // Required empty public constructor
    }

    public static ListEffectsFragment newInstance()
    {
        ListEffectsFragment fragment = new ListEffectsFragment();
        return fragment;
    }

    private EffectsAdapter effectsAdapter;
    private ListView effectListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.list_effects_fragment, container, false);

        ArrayList<ItemEffect> items = new ArrayList<ItemEffect>();
        items.add(new ItemEffect(getResources().getString(R.string.echo), EchoEffectFragment.class));
        items.add(new ItemEffect(getResources().getString(R.string.flanger), FlangerEffectFragment.class));
        items.add(new ItemEffect(getResources().getString(R.string.chorus), ChoruseEfectFragment.class));
        items.add(new ItemEffect(getResources().getString(R.string.reverb), ReverbEffectFragment.class));
        items.add(new ItemEffect(getResources().getString(R.string.phaser), PhaserEffectFragment.class));

        // --------------------------------ADAPTER----------------------------------
        effectListView = (ListView) root.findViewById(R.id.lv_effects);
        effectsAdapter = new EffectsAdapter(getActivity(), R.layout.list_effects_fragment, items);
        effectListView.setAdapter(effectsAdapter);
        effectListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        effectListView.setOnItemClickListener(this);
        // --------------------------------END ADAPTER----------------------------------

        return root;
    }

    BaseEffectFragment newFragment = null;
    public boolean cancel()
    {
        if(newFragment!=null)
        {
            boolean res = newFragment.cancel();
            if(res)
            {
                getFragmentManager().beginTransaction().remove(newFragment).commit();
                newFragment = null;
            }
            return res;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
    {
        ItemEffect itemEffect = effectsAdapter.getItem(position);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        try
        {
            newFragment = itemEffect.getFragment().newInstance();
            newFragment.setChannel(chan);
            newFragment.setEffect();

            fragmentTransaction.add(R.id.effectsFragment, newFragment);
            fragmentTransaction.commit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /*// create new stream - decoded & reversed
    // 2 seconds decoding block as a decoding channel
    if ((chan=BASS_FX.BASS_FX_ReverseCreate(chan, 2, BASS.BASS_STREAM_DECODE|BASS_FX.BASS_FX_FREESOURCE))==0) {
        Error("Couldnt create a reversed stream!");
        BASS.BASS_StreamFree(chan);
        return;
    }*/
}
