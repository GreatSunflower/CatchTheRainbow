package com.sunflower.catchtherainbow.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sunflower.catchtherainbow.R;

import java.util.ArrayList;

public class EffectsAdapter extends ArrayAdapter<ItemEffect>
{
    Context context;
    private ArrayList<ItemEffect> itemEffect;
    private int res;

    public EffectsAdapter(Context context, int resource, ArrayList<ItemEffect> itemEffect)
    {
        super(context, resource, itemEffect);
        this.context = context;
        this.res = resource;
        this.itemEffect = itemEffect;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ItemEffect title = getItem(position);

        if(convertView==null)
        {
            LayoutInflater inflator = LayoutInflater.from(context);
            convertView=inflator.inflate(R.layout.item_effect,parent,false);
        }

        TextView tvTitle = (TextView)convertView.findViewById(R.id.tv_titleEffect);
        tvTitle.setText(title.getTitle());

        return convertView;
    }
}
