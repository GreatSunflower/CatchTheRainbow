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

import com.sunflower.catchtherainbow.Adapters.CustomAdapterFragPager;
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
public class AudioChooserFragment extends DialogFragment  implements FragTabAudioFiles.FragAudioListener
{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public AudioChooserFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     //* @param param1 Parameter 1.
     //* @param param2 Parameter 2.
     //* @return A new instance of fragment AudioChooserFragment.
     */
    // TODO: Rename and change types and number of parameters
    /*public static AudioChooserFragment newInstance(String param1, String param2)
    {
        AudioChooserFragment fragment = new AudioChooserFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }*/

    public static AudioChooserFragment newInstance()
    {
        AudioChooserFragment fragment = new AudioChooserFragment();
        return fragment;
    }

  /*  @Override
    public void onActivityCreated(Bundle arg0)
    {
        super.onActivityCreated(arg0);
        getDialog().getWindow()
                .getAttributes().windowAnimations = R.style.MyAnimation_Window;
    }*/


    @Override
    public int getTheme()
    {
        return R.style.MyAnimation_Window;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /*if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }*/
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

        //viewPager.setAdapter(new CustomAdapterFragPager(getSupportFragmentManager(), getApplicationContext()));
        viewPager.setAdapter(new CustomAdapterFragPager(getChildFragmentManager(), getActivity(), tabLayout, this));

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
                    + " must implement OnFragmentInteractionListener");
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener
    {
        void onOk(ArrayList<AudioFile> selectedAudioFiles);
        void onCancel();
    }
}
