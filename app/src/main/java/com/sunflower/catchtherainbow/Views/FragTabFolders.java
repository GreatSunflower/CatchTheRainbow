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
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

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
public class FragTabFolders extends Fragment implements View.OnClickListener,
        SearchView.OnQueryTextListener
{
    private FoldersAdapter foldersAdapter;
    private ListView folderListView;
    private TextView selectedCount;
    private SearchView searchViewAudio;
    private View resView;
    private Spinner spinFilter;
    String[] items_array = {"", "Sort alphabetically", "Artists"};
    ArrayAdapter<String> spinFilterAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {


        resView = inflater.inflate(R.layout.frag_tab_folders,container,false);
        searchViewAudio = (SearchView)resView.findViewById(R.id.searchViewFolders);
        searchViewAudio.setOnQueryTextListener(this);
        // --------------------------------ADAPTER----------------------------------
        selectedCount = (TextView)resView.findViewById(R.id.tvSelected);

        folderListView = (ListView) resView.findViewById(R.id.listViewFolders);
        folderListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        Button ok = (Button)resView.findViewById(R.id.bOk);
        Button cancel = (Button)resView.findViewById(R.id.bCancel);
        ok.setOnClickListener(this);
        cancel.setOnClickListener(this);


        //Создание потока
        Thread myThready = new Thread(new Runnable()
        {
            public void run() //Этот метод будет выполняться в побочном потоке
            {
                //HashMap<Folder, AudioFile> folders = new HashMap<>();
                ArrayList<Folder> fol = new ArrayList<Folder>();
                ArrayList<AudioFile> audioFiles = Helper.getAllAudioOnDevice(getContext());
                ArrayList<String> pathOfFolders = new ArrayList<String>();
                for(AudioFile audioFile : audioFiles)
                {
                    try
                    {
                        //audioFile.getPath();
                        File folderPath = new File(audioFile.getPath());
                        File folderName = new File(folderPath.getParent() + "//");

                        if (!pathOfFolders.contains(folderName.getName()))
                        {
                            //long size = Folder.getFolderSize(new File(folderPath.getParent()));
                            fol.add(new Folder(folderName.getName(), folderPath.getParent(),
                                    "", folderName.list().length));
                            //folders.put(folder, audioFile);
                            pathOfFolders.add(folderName.getName());
                        }
                    }
                    catch (Exception ex){}
                }
                foldersAdapter = new FoldersAdapter(getActivity(), R.layout.frag_tab_folders, fol);
                folderListView.setAdapter(foldersAdapter);
            }
        });
        myThready.start();	//Запуск потока


        folderListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                foldersAdapter.setNewSelection((Integer) position);
                //кол-во выбранных элементов
                if(foldersAdapter.getSelectedCount() > 0)
                    selectedCount.setText(Integer.toString(foldersAdapter.getSelectedCount()));
                else selectedCount.setText("");
            }
        });

        try { myThready.join(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        return resView;

        // --------------------------------ADAPTER-END----------------------------------

        //------------------------------spinner Filter-----------------

        //---------------------------------end spinnerFilter-----------------------
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
        //foldersAdapter.filterAllAudioFiles(query);
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.bOk:
            {
                for(FragFoldersListener listener : folderFragListeners)
                {
                    //listener.onOk(foldersAdapter.getSelectionAudioFiles());
                }
                break;
            }
            case R.id.bCancel:
            {
                for(FragFoldersListener listener : folderFragListeners)
                {
                    listener.onCancel();
                }
                break;
            }
        }
    }

    private ArrayList<FragFoldersListener> folderFragListeners = new ArrayList<>();

    public ArrayList<FragTabFolders.FragFoldersListener> getFolderFragListeners()
    {
        return folderFragListeners;
    }

    public void addFolderListener(FragFoldersListener folderListener)
    {
        folderFragListeners.add(folderListener);
    }

    public interface FragFoldersListener
    {
        void onOk(ArrayList<AudioFile> selectedFolder);
        void onCancel();
    }
}
