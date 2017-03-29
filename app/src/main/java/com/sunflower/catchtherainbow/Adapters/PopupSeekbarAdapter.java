package com.sunflower.catchtherainbow.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sunflower.catchtherainbow.R;

import java.util.ArrayList;

/**
 * Created by SuperComputer on 3/28/2017.
 */

public class PopupSeekbarAdapter
{
    private SeekBarListener mListener;

    public interface SeekBarListener{
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser, int positionInList);
        public void onStartTrackingTouch(SeekBar seekBar, int positionInList);
        public void onStopTrackingTouch(SeekBar seekBar, int positionInList);
    }

    public listAdapter getAdapter(Context context, ArrayList<String> list, ArrayList<Integer> positions, String title)
    {
        return new listAdapter(context, list, positions, title);
    }

    public void setSeekBarListener(SeekBarListener listener)
    {
        mListener = listener;
    }

    public class listAdapter extends BaseAdapter implements ListAdapter
    {
        private LayoutInflater mInflater;
        private onSeekbarChange mSeekListener;
        private ArrayList<String> itemsList;
        private ArrayList<Integer> positionsList;
        private String title;

        public listAdapter(Context context, ArrayList<String> list, ArrayList<Integer> positions, String title)
        {
            //super(context, 0);
            mInflater = LayoutInflater.from(context);
            if(mSeekListener == null)
            {
                mSeekListener = new onSeekbarChange();
            }
            this.itemsList = list;
            this.positionsList = positions;
            this.title = title;
        }

        @Override
        public int getCount()
        {
            return itemsList.size();
        }

        @Override
        public String getItem(int position)
        {
            return itemsList.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder;

            if(convertView == null)
            {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.base_adapter_dropdown, null);
                holder.text = (TextView)convertView.findViewById(R.id.textView1);
                holder.seekbar = (SeekBar)convertView.findViewById(R.id.seekBar1);
                convertView.setTag(R.layout.base_adapter_dropdown, holder);

                holder.seekbar.setMax(10000);
                holder.seekbar.setProgress(positionsList.get(position));
            }
            else
            {
                holder = (ViewHolder)convertView.getTag(R.layout.base_adapter_dropdown);
            }
            holder.text.setText(itemsList.get(position));
            holder.seekbar.setOnSeekBarChangeListener(mSeekListener);
            holder.seekbar.setTag(position);
           /*ViewHolder2 holder;

            if(convertView == null){
                holder = new ViewHolder2();
                convertView = mInflater.inflate(R.layout.base_adapter_layout, null);
                holder.text_title = (TextView)convertView.findViewById(R.id.textView);
                convertView.setTag(R.layout.base_adapter_layout, holder);
            } else {
                holder = (ViewHolder2)convertView.getTag(R.layout.base_adapter_layout);
            }
            holder.text_title.setText(title);*/
            return convertView;
        }


        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder;

            if(convertView == null)
            {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.base_adapter_dropdown, null);
                holder.text = (TextView)convertView.findViewById(R.id.textView1);
                holder.seekbar = (SeekBar)convertView.findViewById(R.id.seekBar1);
                convertView.setTag(R.layout.base_adapter_dropdown, holder);
            }
            else
            {
                holder = (ViewHolder)convertView.getTag(R.layout.base_adapter_dropdown);
            }
            holder.text.setText(itemsList.get(position));
            holder.seekbar.setOnSeekBarChangeListener(mSeekListener);
            holder.seekbar.setTag(position);
            return convertView;

        }

    }

    static class ViewHolder {
        TextView text;
        SeekBar seekbar;
    }

    static class ViewHolder2 {
        TextView text_title;
    }


    public class onSeekbarChange implements SeekBar.OnSeekBarChangeListener
    {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
            int position = (Integer) seekBar.getTag();
            if (mListener != null)
            {
                mListener.onProgressChanged(seekBar, progress, fromUser, position);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar)
        {
            int position = (Integer) seekBar.getTag();
            if (mListener != null)
            {
                mListener.onStartTrackingTouch(seekBar, position);
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar)
        {
            int position = (Integer) seekBar.getTag();
            if (mListener != null)
            {
                mListener.onStopTrackingTouch(seekBar, position);
            }
        }
    }
}
