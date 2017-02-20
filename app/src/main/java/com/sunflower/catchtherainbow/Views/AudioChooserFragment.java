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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sunflower.catchtherainbow.Adapters.AudioFilesAdapter;
import com.sunflower.catchtherainbow.Adapters.FragPagerAdapter;
import com.sunflower.catchtherainbow.AudioClasses.AudioFile;
import com.sunflower.catchtherainbow.R;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AudioChooserFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AudioChooserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class AudioChooserFragment extends DialogFragment
        implements SearchView.OnQueryTextListener, View.OnClickListener
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
    private TextView tv_selectedCount;
    private SearchView searchViewAudio;
    private FragPagerAdapter fragPagerAdapter;
    private CheckBox isAllSelected;
    private Spinner spinFilter;
    private String[] items_array = {"TITLE", "ARTIST", "DURATION", "DATA", "ALBUM"};
    private ArrayAdapter<String> spinFilterAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        // Inflate the layout for this fragment
        View resView = inflater.inflate(R.layout.fragment_audio_chooser, container, false);

        searchViewAudio = (SearchView)resView.findViewById(R.id.searchViewAudio);
        searchViewAudio.setOnQueryTextListener(this);
        tv_selectedCount = (TextView)resView.findViewById(R.id.tvSelected);

        viewPager = (ViewPager) resView.findViewById(R.id.viewPager);
        tabLayout = (TabLayout) resView.findViewById(R.id.tabLayout);


        fragPagerAdapter = new FragPagerAdapter(getChildFragmentManager(), getActivity(), tabLayout, this);

        viewPager.setAdapter(fragPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                viewPager.setCurrentItem(tab.getPosition());
                Search(searchViewAudio.getQuery().toString());
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

        isAllSelected = (CheckBox)resView.findViewById(R.id.cB_isAllSelected);
        isAllSelected.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //is isAllSelected checked?
                AudioFilesAdapter audioFilesAdapter = fragPagerAdapter.GetFragTabAudioFiles().GetAudioFilesAdapter();
                if (((CheckBox) v).isChecked())
                {
                    if(viewPager.getCurrentItem() == 0)
                    {
                        audioFilesAdapter.SetSelectAll(true);
                        SetSelectedCount(audioFilesAdapter.getSelectedCount());
                    }
                    else
                    {
                        //fragPagerAdapter.GetFragTabFolders().Search(query);
                    }
                    Toast.makeText(getActivity(), tv_selectedCount.getText() + " Selected", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if(viewPager.getCurrentItem() == 0)
                    {
                        audioFilesAdapter.SetSelectAll(false);
                        SetSelectedCount(audioFilesAdapter.getSelectedCount());
                    }
                    else
                    {
                        //fragPagerAdapter.GetFragTabFolders().Search(query);
                    }
                }
            }
        });

        //------------------------------spinner Filter-----------------

        ArrayList<String> items = new ArrayList<String>();
        Collections.addAll(items, items_array);

        spinFilter = (Spinner) resView.findViewById(R.id.spinnerFilter);
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
                // TODO Auto-generated method stub
                spinFilter.setSelection(position);

                if(viewPager.getCurrentItem() == 0)
                {
                    AudioFilesAdapter audioFilesAdapter = fragPagerAdapter.GetFragTabAudioFiles().GetAudioFilesAdapter();
                    audioFilesAdapter.SetSortOrder(GetSortOrder());
                }
                else
                {
                    //fragPagerAdapter.GetFragTabFolders().Search(query);
                }
            }
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        //---------------------------------end spinnerFilter-----------------------

        Button ok = (Button)resView.findViewById(R.id.bOk);
        Button cancel = (Button)resView.findViewById(R.id.bCancel);
        ok.setOnClickListener(this);
        cancel.setOnClickListener(this);

        return resView;
    }

    public String GetSortOrder()
    {
        //Toast.makeText(getActivity(), spinFilter.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
        return spinFilter.getSelectedItem().toString().toLowerCase();
    }

    public void SetSelectedCount(int count)
    {
        if(count!=0) tv_selectedCount.setText(count + "");
        else tv_selectedCount.setText("");
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        Search(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query)
    {
        Search(query);
        return true;
    }

    // Filter Class
    public void Search(String query)
    {
        if(viewPager.getCurrentItem() == 0)
        {
            fragPagerAdapter.GetFragTabAudioFiles().Search(query);
        }
        else
        {
            fragPagerAdapter.GetFragTabFolders().Search(query);
        }
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
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.bOk:
            {
                if(viewPager.getCurrentItem() == 0)
                {
                    if(mListener != null) mListener.onOk(fragPagerAdapter.GetFragTabAudioFiles().GetAudioFilesAdapter().getSelectionAudioFiles());
                }
                else
                {
                    if(mListener != null) mListener.onOk(fragPagerAdapter.GetFragTabAudioFiles().GetAudioFilesAdapter().getSelectionAudioFiles());
                }
                // Закрытие текущего фрагмента
                getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
                break;
            }
            case R.id.bCancel:
            {
                if(mListener != null) mListener.onCancel();
                // Закрытие текущего фрагмента
                getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
                break;
            }
        }
    }

    public interface OnFragmentInteractionListener
    {
        void onOk(ArrayList<AudioFile> selectedAudioFiles);
        void onCancel();
    }
}
