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
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.sunflower.catchtherainbow.Adapters.AudioFilesAdapter;
import com.sunflower.catchtherainbow.AudioClasses.AudioFile;
import com.sunflower.catchtherainbow.Helper;
import com.sunflower.catchtherainbow.R;

import java.util.ArrayList;

/**
 * Created by filip on 8/21/2015.
 */
public class FragTabAudioFiles extends Fragment implements View.OnClickListener,
        SearchView.OnQueryTextListener
{
    private AudioFilesAdapter audioFilesAdapter;
    private ListView superListView;
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
        resView = inflater.inflate(R.layout.frag_tab_audio_files,container,false);
        searchViewAudio = (SearchView)resView.findViewById(R.id.searchViewAudio);
        searchViewAudio.setOnQueryTextListener(this);
        selectedCount = (TextView)resView.findViewById(R.id.tvSelected);
        // --------------------------------ADAPTER----------------------------------
        //Создание потока
        Thread myThready = new Thread(new Runnable()
        {
            public void run() //Этот метод будет выполняться в побочном потоке
            {
                Cursor cursor = Helper.getSongsAudioCursor(getActivity(), "");
                audioFilesAdapter = new AudioFilesAdapter(getActivity(), cursor, 0);
                superListView.setAdapter(audioFilesAdapter);
            }
        });
        myThready.start();	//Запуск потока
        superListView = (ListView)resView.findViewById(R.id.listViewAudioFiles);
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
                    selectedCount.setText(Integer.toString(audioFilesAdapter.getSelectedCount()));
                else selectedCount.setText("");
            }
        });


        //------------------------------spinner Filter-----------------

        /*ArrayList<String>  items = new ArrayList<String>();
        Collections.addAll(items, items_array);

        spinFilter = (Spinner) resView.findViewById(R.id.spinnerFilter);
        // 2 - шаблон дл показа выбранного пункта в выпадающем списке
        spinFilterAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, items);
        // задание шаблона для выпадающих пунктов списка
        spinFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinFilter.setAdapter(spinFilterAdapter);
        spinFilter.setPrompt("Title");
        // ѕрограммный выбор пункта выпадающего списка
        //spinner.setSelection(2);
        spinFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                // TODO Auto-generated method stub

            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });*/

        //---------------------------------end spinnerFilter-----------------------

        Button ok = (Button)resView.findViewById(R.id.bOk);
        Button cancel = (Button)resView.findViewById(R.id.bCancel);

        ok.setOnClickListener(this);
        cancel.setOnClickListener(this);


        try { myThready.join(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return resView;
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
        audioFilesAdapter.filterAllAudioFiles(query);
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
                    listener.onOk(audioFilesAdapter.getSelectionAudioFiles());
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
