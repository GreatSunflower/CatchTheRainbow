package com.sunflower.catchtherainbow.Views.StartedApp;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

import com.sunflower.catchtherainbow.Adapters.EnamLanguages;
import com.sunflower.catchtherainbow.Adapters.StartProjectPagerAdapter;
import com.sunflower.catchtherainbow.Helper;
import com.sunflower.catchtherainbow.R;

import java.io.File;
import java.util.Locale;

public class ProjectStartActivity extends AppCompatActivity
{
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private StartProjectPagerAdapter pagerAdapter;

    public void setLanguage(EnamLanguages enamLanguag)
    {
        Locale locale = new Locale("en");
        if(enamLanguag.equals(EnamLanguages.Русский)) locale = new Locale("ru");
        if(enamLanguag.equals(EnamLanguages.Українська)) locale = new Locale("uk");
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, null);

        // выводим английский текст на русской локали устройства
        setTitle(R.string.app_name);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        SharedPreferences shared = getSharedPreferences("info",MODE_PRIVATE);
        //Using getXXX- with XX is type date you wrote to file "name_file"
        String language = "English";
        if(shared != null) language = shared.getString("language", "English");
        setLanguage(EnamLanguages.valueOf(language));

        setContentView(R.layout.activity_project_start);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                //To check the availability of projects
                //if true: Touch doesn't work
                if(new File(Helper.getPathOfProject()).listFiles().length == 0) return true;
                else return false;
            }
        });

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        pagerAdapter = new StartProjectPagerAdapter(getSupportFragmentManager(), tabLayout, this);
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        /////Tabs/////

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
        });

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(viewPager != null)
        {
            if (new File(Helper.getPathOfProject()).listFiles().length == 0)
                viewPager.setCurrentItem(0);
            else viewPager.setCurrentItem(1);
        }
    }
}
