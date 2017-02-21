package com.sunflower.catchtherainbow.Views;

/**
 * Created by Alexandr on 05.02.2017.
 */

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.sunflower.catchtherainbow.Adapters.AudioFilesAdapter;
import com.sunflower.catchtherainbow.Helper;
import com.sunflower.catchtherainbow.R;

public class FragTabAudioFiles extends Fragment
{
    private AudioFilesAdapter audioFilesAdapter;
    private ListView superListView;
    private View resView;
    private Spinner spinFilter;
    private String[] items_array = {"TITLE", "ARTIST", "DURATION", "DATA", "ALBUM"};
    private ArrayAdapter<String> spinFilterAdapter;
    private AudioChooserFragment audioChooserFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        resView = inflater.inflate(R.layout.frag_tab_audio_files,container,false);
        // --------------------------------ADAPTER----------------------------------
        //Создание потока

        //Cursor cursor = Helper.getSongsAudioCursor(getContext(), "", "");
        superListView = (ListView)resView.findViewById(R.id.listViewAudioFiles);

        audioFilesAdapter = new AudioFilesAdapter(getActivity(), AudioChooserFragment.SONGS_LOADER_ID, 0);
        superListView.setAdapter(audioFilesAdapter);

        superListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        // --------------------------------ADAPTER-END----------------------------------

        superListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                audioFilesAdapter.setNewSelection((long)position);
                //кол-во выбранных элементов
                if(audioFilesAdapter.getSelectedCount() > 0)
                    audioChooserFragment.SetSelectedCount(audioFilesAdapter.getSelectedCount());
                else audioChooserFragment.SetSelectedCount(0);
            }
        });

        return resView;
    }

    public void AddLinkAudioChooserFragment(AudioChooserFragment audioChooserFragment)
    {
        this.audioChooserFragment = audioChooserFragment;
    }

    public AudioFilesAdapter GetAudioFilesAdapter()
    {
        return audioFilesAdapter;
    }

    // Filter Class
    public void Search(String query)
    {
        audioFilesAdapter.filterAllAudioFiles(query);
    }
}
