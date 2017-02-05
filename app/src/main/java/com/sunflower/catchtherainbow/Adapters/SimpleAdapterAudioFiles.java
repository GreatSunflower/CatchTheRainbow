package com.sunflower.catchtherainbow.Adapters;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunflower.catchtherainbow.AudioClasses.AudioFile;
import com.sunflower.catchtherainbow.Helper;
import com.sunflower.catchtherainbow.R;

import java.util.HashMap;

/**
 * Created by Alexandr on 05.02.2017.
 */

public class SimpleAdapterAudioFiles extends CursorAdapter
{
    private final Activity context;
    private HashMap<Long, Boolean> selection = new HashMap<>();
    private String filter = "";

    private LayoutInflater inflater;

    public SimpleAdapterAudioFiles(final Activity context, Cursor c, int flags)
    {
        super(context, c, flags);

        this.context = context;
        this.setFilterQueryProvider(new FilterQueryProvider()
        {
            @Override
            public Cursor runQuery(CharSequence charSequence)
            {
                return Helper.getSongsAudioCursor(context, charSequence.toString());
            }
        });

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public AudioFile getPersonFromPosition(int position)
    {
        Cursor cur = getCursor();
        cur.moveToPosition(position);
        return Helper.getAudioFileById(cur.getLong(cur.getColumnIndex(MediaStore.Audio.Media._ID)), cur);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        return inflater.inflate(R.layout.child_music, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        View rowView;
        final AudioFile file = getPersonFromPosition(cursor.getPosition());
        // if the view isn't generated
        if(view == null)
        {
            rowView = inflater.inflate(R.layout.child_music, null, true);
        }
        else rowView = view;

        //View itemView = rowView.findViewById(R.id.itemVIew);

        // ----- Tint -----
        /*int color = R.color.oddListViewItemColor;
        if(position % 2 != 0) color = R.color.evenListViewItemColor;

        itemView.getBackground().setColorFilter(context.getResources().getColor(color), PorterDuff.Mode.SRC_ATOP);
        // -----Tint-end------
        */

        ImageView albumImage = (ImageView)rowView.findViewById(R.id.imageView);
        TextView nameLayout = (TextView)rowView.findViewById(R.id.tvName);
        TextView artistLayout = (TextView)rowView.findViewById(R.id.tvArtist);
        TextView timesLayout = (TextView)rowView.findViewById(R.id.tvTimes);

        /*if(file.image != null) personImage.setImageBitmap(file.getBitmapImage());
        else personImage.setImageResource(R.drawable.ic_person_black_24dp);*/

        nameLayout.setText(file.getTitle());
        artistLayout.setText(file.getArtist());
        timesLayout.setText(Helper.secondToString(0));
    }

   /* public void refeshData()
    {
        updateFilter(filter);
        getFilter().filter(filter);
    }

    public void updateFilter(String newFilter)
    {
        if(newFilter == null || newFilter.equals(filter)) return;

        filter = newFilter;
        //changeCursor(sqlHelper.fetchPeople(filter));

        notifyDataSetChanged();
    }

    public void invertSelection(Long id)
    {
        if(selection.containsKey(id))
        {
            boolean sel = selection.get(id);
            selection.put(id, !sel);
        }
        else selection.put(id, true);

        notifyDataSetChanged();
    }

    public void invertAll()
    {
        Cursor cursor = getCursor();
        if(!cursor.moveToPosition(0)) return;
        do
        {
            invertSelection(cursor.getLong(cursor.getColumnIndex(SuperDatabaseHelper.KEY_PERSON_ID)));
        }
        while(cursor.moveToNext());
    }

    public void setNewSelection(Long id, boolean value)
    {
        selection.put(id, value);
        notifyDataSetChanged();
    }

    public boolean isPersonChecked(Long person)
    {
        Boolean result = selection.get(person);
        return result == null ? false : result;
    }

    public void selectAll()
    {
        Cursor cursor = getCursor();

        for(int i = 0; i < cursor.getCount(); i++)
        {
            cursor.moveToPosition(i);
            Long id = cursor.getLong(cursor.getColumnIndex(SuperDatabaseHelper.KEY_PERSON_ID));
            setNewSelection(id, true);
        }
    }

    public ArrayList<AudioFile> getSelectedDudes(boolean includeDisplayedOnly)
    {
        ArrayList <AudioFile> res = new ArrayList<>();
        for(Long n : selection.keySet())
        {
            // get selection by ids
        }
        return res;
    }

    public ArrayList<Long> getSelectedIds()
    {
        ArrayList <Long> res = new ArrayList<>();
        for(Long n : selection.keySet())
        {
            if(selection.get(n) == true)
                res.add(n);
        }
        return res;
    }

    public void clearSelection()
    {
        selection = new HashMap<>();
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged()
    {
        super.notifyDataSetChanged();
    }

    public AudioFile getPersonFromPosition(int position)
    {
        Cursor cur = getCursor();
        cur.moveToPosition(position);
        return sqlHelper.getPersonById(cur.getLong(cur.getColumnIndex(SuperDatabaseHelper.KEY_PERSON_ID)));
    }

    /*public ArrayList<Long> getDisplayedDudesIds()
    {
        ArrayList<Long> res = new ArrayList<>();
        Cursor cursor = getCursor();
        cursor.moveToPosition(0);
        do
        {
            res.add(cursor.getLong(cursor.getColumnIndex(SuperDatabaseHelper.KEY_PERSON_ID)));
        }
        while(cursor.moveToNext());
        return res;
    }*/
}
