package com.sunflower.catchtherainbow.Views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.sunflower.catchtherainbow.Adapters.FragPagerAdapter;
import com.sunflower.catchtherainbow.AudioClasses.AudioFile;
import com.sunflower.catchtherainbow.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AudioChooserFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AudioChooserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class AudioChooserFragment extends DialogFragment
        implements FragTabAudioFiles.FragAudioListener, FragTabFolders.FragFoldersListener
{
    private OnFragmentInteractionListener mListener;

    public static AudioChooserFragment newInstance()
    {
        AudioChooserFragment fragment = new AudioChooserFragment();
        return fragment;
    }

    @Override
    public int getTheme()
    {
        return R.style.MyAnimation_Window;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        // Inflate the layout for this fragment
        View resView = inflater.inflate(R.layout.fragment_audio_chooser, container, false);

        viewPager = (ViewPager) resView.findViewById(R.id.viewPager);
        tabLayout = (TabLayout) resView.findViewById(R.id.tabLayout);

        viewPager.setAdapter(new FragPagerAdapter(getChildFragmentManager(), getActivity(), tabLayout, this, this));

        tabLayout.setupWithViewPager(viewPager);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
        });

        return resView;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener)
        {
            mListener = (OnFragmentInteractionListener) context;
        } else
        {
            throw new RuntimeException(context.toString()
                    + " must implement OnEffectsHostListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onOk(ArrayList<AudioFile> selectedAudioFiles)
    {
        if(mListener != null) mListener.onOk(selectedAudioFiles);
        // Закрытие текущего фрагмента
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public void onCancel()
    {
        if(mListener != null) mListener.onCancel();
        // Закрытие текущего фрагмента
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    public interface OnFragmentInteractionListener
    {
        void onOk(ArrayList<AudioFile> selectedAudioFiles);
        void onCancel();
    }
}
