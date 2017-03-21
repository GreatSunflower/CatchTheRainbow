package com.sunflower.catchtherainbow.Views.Effects;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.sunflower.catchtherainbow.AudioClasses.AudioIO;
import com.sunflower.catchtherainbow.AudioClasses.BasePlayer;
import com.sunflower.catchtherainbow.AudioClasses.Project;
import com.sunflower.catchtherainbow.R;
import com.sunflower.catchtherainbow.Views.AudioProgressView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnEffectsHostListener} interface
 * to handle interaction events.
 * Use the {@link EffectsHostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EffectsHostFragment extends DialogFragment implements View.OnClickListener
{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    private OnEffectsHostListener mListener;

    public EffectsHostFragment()
    {

    }

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters.
     * @return A new instance of fragment EffectsHostFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EffectsHostFragment newInstance()
    {
        //EffectsHostFragment fragment = new EffectsHostFragment();
       // Bundle args = new Bundle();
        //fragment.setArguments(args);
        return new EffectsHostFragment();
    }

    private BaseEffectFragment effectsFragment = ListEffectsFragment.newInstance();
    private int chan;
    // pass null to remove it
    public void setChannel(int chan)
    {
        this.chan = chan;
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //if (getArguments() != null){}
    }

    @Override
    public int getTheme()
    {
        return R.style.MyAnimation_Window;
    }

    AudioProgressView progressView;
    ImageButton playStopButt;
    AudioIO player;
    private Project Project;
    private boolean isDragging = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.effects_host_fragment, container, false);

        Button okButt = (Button) root.findViewById(R.id.bOk);
        Button cancelButt = (Button) root.findViewById(R.id.bCancel);

        okButt.setOnClickListener(this);
        cancelButt.setOnClickListener(this);

        //////////////////////////
        /*player = new AudioIO(EffectsHostFragment.this.getActivity(), Project);
        player.addPlayerListener(playerListener);
        player.setTracks(Project.getTracks());*/

        playStopButt = (ImageButton) root.findViewById(R.id.bPlayEffect);
        // play stop handler
        playStopButt.setOnClickListener(this);

        ///////////////////////
        progressView = (AudioProgressView) root.findViewById(R.id.audioProgressEffect);
        progressView.setMax(1.f);
        progressView.setCurrent(0);
        progressView.setOnSeekBar(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b)
            {
                if (player != null)
                    progressView.setCurrent(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
                isDragging = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                if (player != null)
                {
                    player.setPosition(seekBar.getProgress());
                }
                isDragging = false;
            }
        });

        ///////////////////////////

        return root;
    }

    BasePlayer.AudioPlayerListener playerListener = new BasePlayer.AudioPlayerListener()
    {
        @Override
        public void onInitialized(float totalTime/*final File file*/)
        {
            progressView.setMax(totalTime);
            progressView.setCurrent(0);
        }

        @Override
        public void onPlay()
        {
            playStopButt.setImageResource(R.drawable.ic_pause);
        }

        @Override
        public void onPause()
        {
            playStopButt.setImageResource(R.drawable.ic_play);
        }

        @Override
        public void onStop()
        {
            playStopButt.setImageResource(R.drawable.ic_play);
        }

        @Override
        public void onCompleted()
        {
            progressView.setCurrent(0);
            playStopButt.setImageResource(R.drawable.ic_play);
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        // set the fragment
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.effectsFragment, effectsFragment);
        effectsFragment.setChannel(chan);
        fragmentTransaction.commit();

    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnEffectsHostListener)
        {
            mListener = (OnEffectsHostListener) context;
        }
        else throw new RuntimeException(context.toString() + " must implement OnEffectsHostListener");
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    // ok | cancel
    @Override
    public void onClick(View view)
    {
        if(view.getId() == R.id.bOk)
        {

        }
        else if(view.getId() == R.id.bCancel)
        {
            boolean res = effectsFragment.cancel();
            if(res) return;
        }
        else if(view.getId() == R.id.bPlayEffect)
        {
            if(player.isPlaying()) player.pause();
            else player.play();
            return;
        }
        if (mListener != null)
        {
            mListener.onEffectsConfirmed();
        }

        // close it!
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <bass_dx8_echo>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnEffectsHostListener
    {
        void onEffectsConfirmed();
        void onEffectsCancelled();
    }
}
