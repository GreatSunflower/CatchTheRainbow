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
import android.widget.AbsListView;
import android.widget.ListView;

import com.sunflower.catchtherainbow.Adapters.SimpleAdapterAudioFiles;
import com.sunflower.catchtherainbow.AudioClasses.AudioFile;
import com.sunflower.catchtherainbow.Helper;
import com.sunflower.catchtherainbow.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by filip on 8/21/2015.
 */
public class FragTabAudioFiles extends Fragment
{
    private List<AudioFile> dudes = new ArrayList<>();
    private SimpleAdapterAudioFiles adapter;
    private ListView superListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View resView = inflater.inflate(R.layout.frag_tab_audio_files,container,false);

        // --------------------------------ADAPTER----------------------------------
        Cursor cursor = Helper.getSongsAudioCursor(getActivity(), "");// sqlHelper.fetchPeople("");
        String[] columns = new String[] {
             /*   SuperDatabaseHelper.KEY_PERSON_FIRST_NAME,
                SuperDatabaseHelper.KEY_PERSON_LAST_NAME,
                SuperDatabaseHelper.KEY_PERSON_AGE,
                SuperDatabaseHelper.KEY_PERSON_ADDRESS,
                SuperDatabaseHelper.KEY_PERSON_IMAGE*/
        };
        int[] to = new int[]{
                R.id.imageView,
                R.id.tvName,
                R.id.tvArtist,
                R.id.tvTimes
        };
        adapter = new SimpleAdapterAudioFiles(getActivity(), cursor, 0);
        superListView = (ListView)resView.findViewById(R.id.listViewAudioFiles);
        superListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        superListView.setAdapter(adapter);
        // --------------------------------ADAPTER-END----------------------------------

        return resView;
    }
}
