package com.sunflower.catchtherainbow.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunflower.catchtherainbow.AudioClasses.AudioFile;
import com.sunflower.catchtherainbow.Helper;
import com.sunflower.catchtherainbow.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alexandr on 26.02.2017.
 */

public class SongsOfFolderAdapter extends ArrayAdapter<AudioFile>
{
    Context context;
    private HashMap<Integer, Boolean> selection = new HashMap<>();
    private int selectedCount = 0;
    private ArrayList<AudioFile> superSongs;
    private int res;

    public SongsOfFolderAdapter(Context context, int resource, ArrayList<AudioFile> superSongs)
    {
        super(context, resource, superSongs);
        this.context = context;
        this.res = resource;
        this.superSongs = superSongs;
    }

    public void updateData(ArrayList<AudioFile> newlist)
    {
        superSongs = newlist;
    }

    public void SetSortOrder(String sortOrder)
    {

    }

    public void SetSelectAll(boolean value)
    {
        HashMap<Integer, Boolean> newSelection = new HashMap<>();
        selectedCount = 0;
        selection = newSelection;
        notifyDataSetChanged();
    }

    public ArrayList<AudioFile> filterAudio(String filter, ArrayList<AudioFile> allSongs)
    {
        SetSelectAll(false); //снять выделение
        ArrayList<AudioFile> files = new ArrayList<AudioFile>();
        for(AudioFile audioFile : allSongs)
        {
            if(audioFile.getTitle().toLowerCase().contains(filter.toLowerCase())
                    || audioFile.getArtist().toLowerCase().contains(filter.toLowerCase()))
            {
                files.add(audioFile);
            }
        }
        return files;
    }

    public ArrayList<AudioFile> getSelectionAudioFiles()
    {
        ArrayList<AudioFile> selectedAudio = new ArrayList<>();
        for (Map.Entry<Integer, Boolean> entry : selection.entrySet())
        {
            Integer key = entry.getKey();
            Boolean value = entry.getValue();
            if (value == true)
            {
                selectedAudio.add(getItem(key.intValue()));
            }
        }
        return selectedAudio;
    }

    public int getSelectedCount()
    {
        return selectedCount;
    }

    public void setNewSelection(Integer id)
    {
        if(isAudioChecked(id))
        {
            selection.put(id, false);
            selectedCount--;
        }
        else
        {
            selection.put(id, true);
            selectedCount++;
        }
        notifyDataSetChanged();
    }

    public boolean isAudioChecked(Integer row)
    {
        Boolean result = selection.get(row);
        return result == null ? false : result;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        AudioFile audioFile = getItem(position);

        if (convertView == null)
        {
            LayoutInflater inflator = LayoutInflater.from(context);
            convertView = inflator.inflate(R.layout.item_audio, null, true);
        }

        View itemView = convertView.findViewById(R.id.music_layout);

        // ----- Tint -----
        if (isAudioChecked(position))
            itemView.setBackgroundColor(context.getResources().getColor(R.color.selectedListItem));
        else
            itemView.setBackgroundColor(context.getResources().getColor(R.color.backgroundListView));
        // -----Tint-end------

        ImageView albumImage = (ImageView) convertView.findViewById(R.id.imageView);
        TextView nameLayout = (TextView) convertView.findViewById(R.id.tvName);
        TextView artistLayout = (TextView) convertView.findViewById(R.id.tvArtist);
        TextView timesLayout = (TextView) convertView.findViewById(R.id.tvTimes);

        if (audioFile.getBitmapImage() != null)
            albumImage.setImageBitmap(audioFile.getBitmapImage());

        nameLayout.setText(audioFile.getTitle());
        artistLayout.setText(audioFile.getArtist());
        timesLayout.setText(Helper.secondToString(audioFile.getDuration()));

        return convertView;
    }
}