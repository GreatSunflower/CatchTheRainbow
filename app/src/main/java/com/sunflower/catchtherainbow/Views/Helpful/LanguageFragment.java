package com.sunflower.catchtherainbow.Views.Helpful;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sunflower.catchtherainbow.Adapters.SupportedLanguages;
import com.sunflower.catchtherainbow.Adapters.LanguageAdapter;
import com.sunflower.catchtherainbow.ProjectActivity;
import com.sunflower.catchtherainbow.R;

import java.util.ArrayList;

public class LanguageFragment extends DialogFragment implements AdapterView.OnItemClickListener
{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_CURRENT_LANGUAGE = "English";

    // TODO: Rename and change types of parameters
    private String mlanguage = "English";

    public LanguageFragment()
    {
        // Required empty public constructor
    }

    private ProjectActivity projectActivity;
    public void setFragmentOwner(ProjectActivity projectActivity)
    {
        this.projectActivity = projectActivity;
    }

    // TODO: Rename and change types and number of parameters
    public static LanguageFragment newInstance(String mlanguage)
    {
        LanguageFragment fragment = new LanguageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CURRENT_LANGUAGE, mlanguage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mlanguage = getArguments().getString(ARG_CURRENT_LANGUAGE);
        }
    }

    private ListView listView;
    private LanguageAdapter adapter;
    ArrayList<SupportedLanguages> languages =null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View resView = inflater.inflate(R.layout.language_fragment, container, false);

        languages = new ArrayList<SupportedLanguages>();
        languages.add(SupportedLanguages.English);
        languages.add(SupportedLanguages.Українська);
        languages.add(SupportedLanguages.Русский);
        listView = (ListView) resView.findViewById(R.id.listViewLanguage);
        // ”казать xml-файл с шаблоном пункта
        adapter = new LanguageAdapter(getContext(), R.layout.language_fragment, languages);
        adapter.setCurrentLanguage(SupportedLanguages.valueOf(mlanguage));
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(this);

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return resView;
    }

    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
    {
        SupportedLanguages item = adapter.getlanguages().get(position);
        if(adapter.getCurrentLanguage().equals(item))  dismiss();
        adapter.setCurrentLanguage(item);
        projectActivity.setLanguage(item);
    }

}
