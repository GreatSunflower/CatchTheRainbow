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

public class LanguageAdapter extends ArrayAdapter<SupportedLanguages>
{
    Context context;
    private ArrayList<SupportedLanguages> languages;
    private SupportedLanguages currentLanguage = SupportedLanguages.English;
    int res;

    public LanguageAdapter(Context context, int resource, ArrayList<SupportedLanguages> languages)
    {
        super(context, resource, languages);
        this.context = context;
        this.res = resource;
        this.languages = languages;
    }

    public void setCurrentLanguage(SupportedLanguages currentLanguage)
    {
        this.currentLanguage = currentLanguage;
        notifyDataSetChanged();
    }

    public SupportedLanguages getCurrentLanguage()
    {
        return currentLanguage;
    }

    public List<SupportedLanguages> getlanguages()
    {
        return languages;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        SupportedLanguages language = getItem(position);

        if(convertView==null)
        {
            LayoutInflater inflator = LayoutInflater.from(context);
            convertView=inflator.inflate(R.layout.item_language,parent,false);
        }

        View itemView = convertView.findViewById(R.id.language_layout);

        //if(!language.equals(currentLanguage)) itemView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.background_fragment));
        if(!language.equals(currentLanguage)) itemView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryDark));
        else itemView.setBackgroundColor(context.getResources().getColor(R.color.selectedListItem));

        TextView tvTitle = (TextView)convertView.findViewById(R.id.tv_language);
        tvTitle.setText(language.toString());

        ImageView tvImage = (ImageView)convertView.findViewById(R.id.im_language);
        Drawable dr = null;

        if(language.equals(SupportedLanguages.English)) dr = context.getResources().getDrawable(R.drawable.flag_of_the_united_kingdom);
        if(language.equals(SupportedLanguages.Українська)) dr = context.getResources().getDrawable(R.drawable.flag_of_ukraine);
        if(language.equals(SupportedLanguages.Русский)) dr = context.getResources().getDrawable(R.drawable.flag_of_russia);

            tvImage.setImageDrawable(dr);
        return convertView;
    }
}
