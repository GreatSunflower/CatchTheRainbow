package com.sunflower.catchtherainbow.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunflower.catchtherainbow.AudioClasses.Folder;
import com.sunflower.catchtherainbow.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Alexandr on 08.02.2017.
 */

public class FoldersAdapter extends ArrayAdapter<Folder>
{
    Context context;
    //private HashMap<String, String> folders = new HashMap<>();
    private HashMap<Integer, Boolean> selection = new HashMap<>();
    private int selectedCount = 0;
    int res;

    public FoldersAdapter(Context context, int resource, ArrayList<Folder> folders)
    {
        super(context, resource, folders);
        this.context = context;
        this.res = resource;
    }

    public int getSelectedCount()
    {
        return selectedCount;
    }

    public void setNewSelection(Integer id)
    {
        if(isFolderChecked(id))
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

    public boolean isFolderChecked(Integer row)
    {
        Boolean result = selection.get(row);
        return result == null ? false : result;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Folder folder = getItem(position);

        if(convertView==null)
        {
            LayoutInflater inflator = LayoutInflater.from(context);
            convertView=inflator.inflate(R.layout.child_folder,parent,false);
        }

        View itemView = convertView.findViewById(R.id.folders_layout);
        // ----- Tint -----
        if (isFolderChecked(position))
            itemView.setBackgroundColor(context.getResources().getColor(R.color.selectedListItem));
        else
            itemView.setBackgroundColor(context.getResources().getColor(R.color.backgroundListView));
        // -----Tint-end------

        TextView tvTitle = (TextView)convertView.findViewById(R.id.tvTitleFolder);
        tvTitle.setText(folder.getTitle());

        TextView tvPath = (TextView)convertView.findViewById(R.id.tvPathFolder);
        tvPath.setText(folder.getPath());

        TextView tVCountMusics = (TextView)convertView.findViewById(R.id.tVCountMusics);
        tVCountMusics.setText(folder.getCountSong() + "");

        ImageView tvImage = (ImageView)convertView.findViewById(R.id.imageViewFolder);
        tvImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_folder));

        return convertView;
    }
}
