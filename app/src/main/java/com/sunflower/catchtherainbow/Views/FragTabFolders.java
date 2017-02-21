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
import com.sunflower.catchtherainbow.Adapters.FoldersAdapter;
import com.sunflower.catchtherainbow.AudioClasses.AudioFile;
import com.sunflower.catchtherainbow.AudioClasses.Folder;
import com.sunflower.catchtherainbow.Helper;
import com.sunflower.catchtherainbow.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by filip on 8/21/2015.
 */
public class FragTabFolders extends Fragment
{
    private FoldersAdapter foldersAdapter;
    private ListView folderListView;
    private View resView;
    ArrayList<Folder> folders = new ArrayList<Folder>();
    private Spinner spinFilter;
    String[] items_array = {"", "Sort alphabetically", "Artists"};
    ArrayAdapter<String> spinFilterAdapter;
    private AudioChooserFragment audioChooserFragment;
    private AudioFilesAdapter audioFilesAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        resView = inflater.inflate(R.layout.frag_tab_folders,container,false);
        folderListView = (ListView) resView.findViewById(R.id.listViewFolders);
        folderListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // --------------------------------ADAPTER----------------------------------
        folders = GetAllFolders();
        foldersAdapter = new FoldersAdapter(getActivity(), R.layout.frag_tab_folders, folders);
        folderListView.setAdapter(foldersAdapter);

        folderListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // --------------------------------ADAPTER----------------------------------
                audioFilesAdapter = new AudioFilesAdapter(getActivity(), AudioChooserFragment.SUB_SONGS_LOADER_ID, 0);
                folderListView.setAdapter(audioFilesAdapter);

                // --------------------------------ADAPTER-END----------------------------------

                folderListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
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

                //foldersAdapter.setNewSelection((Integer) position);
                /*//кол-во выбранных элементов
                if(foldersAdapter.getSelectedCount() > 0)
                    audioChooserFragment.SetSelectedCount(foldersAdapter.getSelectedCount());
                else audioChooserFragment.SetSelectedCount(0);*/
            }
        });
        // --------------------------------ADAPTER-END----------------------------------

        return resView;
    }

    public ArrayList<Folder> GetAllFolders()
    {
        ArrayList<Folder> fol = new ArrayList<Folder>();
        // speed this up!!!!
        ArrayList<AudioFile> audioFiles = Helper.getAllAudioOnDevice(getContext());
        ArrayList<String> pathOfFolders = new ArrayList<String>();
        for(AudioFile audioFile : audioFiles)
        {
            try
            {
                File folderPath = new File(audioFile.getPath());
                File folderName = new File(folderPath.getParent() + "//");

                if (!pathOfFolders.contains(folderName.getName()))
                {
                    //long size = Folder.getFolderSize(new File(folderPath.getParent()));
                    fol.add(new Folder(folderName.getName(), folderPath.getParent(),
                            "", folderName.list().length));
                    pathOfFolders.add(folderName.getName());
                }
            }
            catch (Exception ex){}
        }
        return fol;
    }

    public void AddLinkAudioChooserFragment(AudioChooserFragment audioChooserFragment)
    {
        this.audioChooserFragment = audioChooserFragment;
    }

    public void Search(String query)
    {
        foldersAdapter = new FoldersAdapter(getActivity(), R.layout.frag_tab_folders, filterFolders(query));
        folderListView.setAdapter(foldersAdapter);
    }

    public ArrayList<Folder> filterFolders(String filter)
    {
        ArrayList<Folder> foldersfilter = new ArrayList<Folder>();
        for(Folder folder : folders)
        {
            if(folder.getTitle().toLowerCase().contains(filter.toLowerCase()))
            {
                foldersfilter.add(folder);
            }
        }
        return foldersfilter;
    }
}
