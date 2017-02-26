package com.sunflower.catchtherainbow.Views;

/**
 * Created by Alexandr on 05.02.2017.
 */

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
import com.sunflower.catchtherainbow.Adapters.SongsOfFolderAdapter;
import com.sunflower.catchtherainbow.AudioClasses.AudioFile;
import com.sunflower.catchtherainbow.AudioClasses.Folder;
import com.sunflower.catchtherainbow.Helper;
import com.sunflower.catchtherainbow.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by filip on 8/21/2015.
 */


public class FragTabFolders extends Fragment implements AudioChooserFragment.SongsSelectable, AdapterView.OnItemClickListener
{
    public enum Mode {AudioFiles, Folders;}
    private Mode mode = Mode.Folders;
    private FoldersAdapter foldersAdapter;
    private SongsOfFolderAdapter songsOfFolderAdapter;
    private ListView folderListView;
    private View resView;
    ArrayList<Folder> folders = new ArrayList<Folder>();
    ArrayList<AudioFile> currentAudioFiles;
    private Spinner spinFilter;
    ArrayAdapter<String> spinFilterAdapter;
    private AudioChooserFragment audioChooserFragment;
    private AudioFilesAdapter audioFilesAdapter;
    private HashMap<String, ArrayList<AudioFile>> songsOfFolders = new HashMap<String, ArrayList<AudioFile>>();

    public void SetMode(Mode newMode)
    {
        mode = newMode;
    }

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

        folderListView.setOnItemClickListener(this);
        // --------------------------------END ADAPTER----------------------------------
        return resView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        if(mode == Mode.Folders)
        {
            Folder folder = foldersAdapter.getItem(position);
            currentAudioFiles = new ArrayList<AudioFile>();
            // --------------------------------ADAPTER----------------------------------
            for (Map.Entry entry: songsOfFolders.entrySet())
            {
                String key = (String) entry.getKey();
                AudioFile value;
                if(key.equals(folder.getPath()))
                {
                    currentAudioFiles = (ArrayList<AudioFile>) entry.getValue();
                    break;
                }
                //действия с ключом и значением
            }

            songsOfFolderAdapter = new SongsOfFolderAdapter(getActivity(),  R.layout.frag_tab_folders, currentAudioFiles);
            folderListView.setAdapter(songsOfFolderAdapter);
            //audioChooserFragment.HideElementsFromTab(0);
            mode = Mode.AudioFiles;
        }
        else if(mode == Mode.AudioFiles)
        {
            songsOfFolderAdapter.setNewSelection(position);
            //кол-во выбранных элементов
            if(songsOfFolderAdapter.getSelectedCount() > 0)
                audioChooserFragment.SetSelectedCount(songsOfFolderAdapter.getSelectedCount());
            else audioChooserFragment.SetSelectedCount(0);
        }
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

                //привязать все песни к папкам
                if(songsOfFolders.get(folderPath.getParent()) != null) songsOfFolders.get(folderPath.getParent()).add(audioFile);
                else
                {
                    ArrayList<AudioFile> g = new ArrayList<AudioFile>();
                    g.add(audioFile);
                    songsOfFolders.put(folderPath.getParent(), new ArrayList<AudioFile>(g));
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

    public SongsOfFolderAdapter GetAudioFilesAdapter()
    {
        return songsOfFolderAdapter;
    }

    @Override
    public void search(String query)
    {
        if(mode == Mode.Folders)
        {
            foldersAdapter = new FoldersAdapter(getActivity(), R.layout.frag_tab_folders, filterFolders(query));
            folderListView.setAdapter(foldersAdapter);
        }
        else if(mode == Mode.AudioFiles)
        {
            songsOfFolderAdapter = new SongsOfFolderAdapter(getActivity(),
                    R.layout.frag_tab_folders, songsOfFolderAdapter.filterAudio(query, currentAudioFiles));
            folderListView.setAdapter(songsOfFolderAdapter);
        }
    }

    @Override
    public ArrayList<AudioFile> getSelectionAudioFiles()
    {
        ArrayList<AudioFile> res = songsOfFolderAdapter.getSelectionAudioFiles();
        if(res != null) return res;
        else return null;
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
