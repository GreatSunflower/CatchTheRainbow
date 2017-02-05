package com.sunflower.catchtherainbow.Adapters;

/**
 * Created by Alexandr on 05.02.2017.
 */
/*
public class SimpleAdapterAudioFiles extends SimpleCursorAdapter
{
    private final Activity context;
    private HashMap<Long, Boolean> selection = new HashMap<>();
    private String filter = "";

    public SimpleAdapterAudioFiles(Activity context, int layout, Cursor c,
                             String[] from, int[] to, int flags)
    {
        super(context, layout, c, from, to, flags);

        sqlHelper = ((SuperApplication)context.getApplication()).helper;

        this.context = context;
        this.setFilterQueryProvider(new FilterQueryProvider()
        {
            @Override
            public Cursor runQuery(CharSequence charSequence)
            {
                return sqlHelper.fetchPeople(charSequence.toString());
            }
        });
    }

    public void refeshData()
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

    public void add(AudioFile p)
    {
        sqlHelper.addPerson(p);
        // Not selected
        selection.put(p.id, false);
        // Refresh the list
        refeshData();
        //notifyDataSetChanged();
    }

    public int remove(Long p, boolean shouldUpdate)
    {
        int res = sqlHelper.removePerson(p);
        // remove from selection map
        selection.remove(p);
        // Refresh the list
        if(shouldUpdate)
            refeshData();

        return res;
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

    @NonNull
    public View getView(int position, View view, ViewGroup parent)
    {
        //return super.getView(position, view, parent);
        View rowView;
        final AudioFile file = getPersonFromPosition(position);
        // if the view isn't generated
        if(view == null)
        {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.child_music, null, true);
        }
        else rowView = view;

        View itemView = rowView.findViewById(R.id.itemVIew);

        // ----- Tint -----
        /*int color = R.color.oddListViewItemColor;
        if(position % 2 != 0) color = R.color.evenListViewItemColor;

        itemView.getBackground().setColorFilter(context.getResources().getColor(color), PorterDuff.Mode.SRC_ATOP);
        // -----Tint-end------
        */

        /*ImageView albumImage = (ImageView)rowView.findViewById(R.id.imageView);
        TextView nameLayout = (TextView)rowView.findViewById(R.id.tvName);
        TextView artistLayout = (TextView)rowView.findViewById(R.id.tvArtist);
        TextView timesLayout = (TextView)rowView.findViewById(R.id.tvTimes);

        /*if(file.image != null) personImage.setImageBitmap(file.getBitmapImage());
        else personImage.setImageResource(R.drawable.ic_person_black_24dp);*/

        /*nameLayout.setText(file. + " " + file.lastName);
        artistLayout.setText(file.age+"");
        timesLayout.setText(file.address);*/

        /*return rowView;
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
//}
