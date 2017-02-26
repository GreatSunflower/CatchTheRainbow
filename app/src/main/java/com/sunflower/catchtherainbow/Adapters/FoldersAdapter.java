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

/**
 * Created by Alexandr on 08.02.2017.
 */

public class FoldersAdapter extends ArrayAdapter<Folder>
{
    Context context;
    private int selectedCount = 0;
    private ArrayList<Folder> folders;
    private int res;

    public FoldersAdapter(Context context, int resource, ArrayList<Folder> folders)
    {
        super(context, resource, folders);
        this.context = context;
        this.res = resource;
        this.folders = folders;
    }

    public void updateData(ArrayList<Folder> newlist)
    {
        folders = newlist;
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

    public int getSelectedCount()
    {
        return selectedCount;
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
        itemView.setBackgroundColor(context.getResources().getColor(R.color.backgroundListView));

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
