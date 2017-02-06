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
import android.widget.Button;
import android.widget.ListView;

import com.sunflower.catchtherainbow.Adapters.SimpleAdapterAudioFiles;
import com.sunflower.catchtherainbow.AudioClasses.AudioFile;
import com.sunflower.catchtherainbow.Helper;
import com.sunflower.catchtherainbow.R;

import java.util.ArrayList;

/**
 * Created by filip on 8/21/2015.
 */
public class FragTabAudioFiles extends Fragment implements View.OnClickListener
{
    private SimpleAdapterAudioFiles adapter;
    private ListView superListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View resView = inflater.inflate(R.layout.frag_tab_audio_files,container,false);

        // --------------------------------ADAPTER----------------------------------
        Cursor cursor = Helper.getSongsAudioCursor(getActivity(), "");

        adapter = new SimpleAdapterAudioFiles(getActivity(), cursor, 0);
        superListView = (ListView)resView.findViewById(R.id.listViewAudioFiles);
        superListView.setAdapter(adapter);
        superListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        // --------------------------------ADAPTER-END----------------------------------

        superListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                adapter.setNewSelection((long)position);
            }
        });

        Button ok = (Button)resView.findViewById(R.id.bOk);
        Button cancel = (Button)resView.findViewById(R.id.bCancel);

        ok.setOnClickListener(this);
        cancel.setOnClickListener(this);

        return resView;
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.bOk:
            {
                for(FragAudioListener listener : audioFragListeners)
                {
                    listener.onOk(adapter.getSelectionAudioFiles());
                }
                break;
            }
            case R.id.bCancel:
            {
                for(FragAudioListener listener : audioFragListeners)
                {
                    listener.onCancel();
                }
                break;
            }
        }
    }

    private ArrayList<FragAudioListener> audioFragListeners = new ArrayList<>();

    public ArrayList<FragAudioListener> getAudioFragListeners()
    {
        return audioFragListeners;
    }

    public void addAudioListener(FragAudioListener audioListener)
    {
        audioFragListeners.add(audioListener);
    }

    public interface FragAudioListener
    {
        void onOk(ArrayList<AudioFile> selectedAudioFiles);
        void onCancel();
    }
}
