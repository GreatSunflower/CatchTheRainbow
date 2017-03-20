package com.sunflower.catchtherainbow.Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunflower.catchtherainbow.R;

import java.util.ArrayList;
import java.util.List;

public class LanguageAdapter extends ArrayAdapter<EnamLanguages>
{
    Context context;
    private ArrayList<EnamLanguages> languages;
    private EnamLanguages currentLanguage = EnamLanguages.English;
    int res;

    public LanguageAdapter(Context context, int resource, ArrayList<EnamLanguages> languages)
    {
        super(context, resource, languages);
        this.context = context;
        this.res = resource;
        this.languages = languages;
    }

    public void setCurrentLanguage(EnamLanguages currentLanguage)
    {
        this.currentLanguage = currentLanguage;
        notifyDataSetChanged();
    }

    public EnamLanguages getCurrentLanguage()
    {
        return currentLanguage;
    }

    public List<EnamLanguages> getlanguages()
    {
        return languages;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        EnamLanguages language = getItem(position);

        if(convertView==null)
        {
            LayoutInflater inflator = LayoutInflater.from(context);
            convertView=inflator.inflate(R.layout.language_item,parent,false);
        }

        View itemView = convertView.findViewById(R.id.language_layout);

        //if(!language.equals(currentLanguage)) itemView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.background_fragment));
        if(!language.equals(currentLanguage)) itemView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryDark));
        else itemView.setBackgroundColor(context.getResources().getColor(R.color.selectedListItem));

        TextView tvTitle = (TextView)convertView.findViewById(R.id.tv_language);
        tvTitle.setText(language.toString());

        ImageView tvImage = (ImageView)convertView.findViewById(R.id.im_language);
        Drawable dr = null;

        if(language.equals(EnamLanguages.English)) dr = context.getResources().getDrawable(R.drawable.flag_of_the_united_kingdom);
        if(language.equals(EnamLanguages.Українська)) dr = context.getResources().getDrawable(R.drawable.flag_of_ukraine);
        if(language.equals(EnamLanguages.Русский)) dr = context.getResources().getDrawable(R.drawable.flag_of_russia);

            tvImage.setImageDrawable(dr);
        return convertView;
    }
}