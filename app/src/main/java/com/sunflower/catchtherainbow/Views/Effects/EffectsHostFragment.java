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

import com.sunflower.catchtherainbow.R;

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
        // Required empty public constructor
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_effects_host, container, false);

        Button okButt = (Button) root.findViewById(R.id.bOk);
        Button cancelButt = (Button) root.findViewById(R.id.bCancel);

        okButt.setOnClickListener(this);
        cancelButt.setOnClickListener(this);

        return root;
    }

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
            effectsFragment.cancel();
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
