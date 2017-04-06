package com.sunflower.catchtherainbow.Views.StartedApp;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

import com.sunflower.catchtherainbow.Adapters.StartProjectPagerAdapter;
import com.sunflower.catchtherainbow.Adapters.SupportedLanguages;
import com.sunflower.catchtherainbow.Helper;
import com.sunflower.catchtherainbow.R;

import java.io.File;
import java.util.Locale;

public class ProjectStartActivity extends AppCompatActivity
{
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private StartProjectPagerAdapter pagerAdapter;

    public void setLanguage(SupportedLanguages enamLanguag)
    {
        Locale locale = new Locale("en");
        if(enamLanguag.equals(SupportedLanguages.Русский)) locale = new Locale("ru");
        if(enamLanguag.equals(SupportedLanguages.Українська)) locale = new Locale("uk");
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
        setLanguage(SupportedLanguages.valueOf(language));

        setContentView(R.layout.activity_project_start);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                //To check the availability of projects
                //if true: Touch doesn't work
                File file = new File(Helper.getPathOfProject());
                if(file.listFiles() != null && file.listFiles().length == 0) return true;
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

        ////////////////////////////////////////////////////permissions//////////////////////////
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
        {
            int PERMISSION_ALL = 1;
            String[] PERMISSIONS = new String[0];

            PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

            if (!hasPermissions(this, PERMISSIONS))
            {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);

                viewPager.setCurrentItem(0);
            }
            else
            {
                File file = new File(Helper.getPathOfProject());
                if (!file.exists() || file.listFiles() == null || file.listFiles().length == 0)
                    viewPager.setCurrentItem(0);
                else viewPager.setCurrentItem(1);
            }
        }
        else
        {
            File file = new File(Helper.getPathOfProject());
            if (!file.exists() || file.listFiles() == null || file.listFiles().length == 0)
                viewPager.setCurrentItem(0);
            else viewPager.setCurrentItem(1);
        }
        //////////////////////////////////////////////////////////end permissions///////////////////////

    }

    // ----- permissions ----------
    public boolean hasPermissions(Context context, String... permissions)
    {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null)
        {
            for (String permission : permissions)
            {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                {
                    reportFullyDrawn();
                    //finish();
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case 1:
            {
                for(int i = 0; i < permissions.length; i++)
                {
                    if(permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    {
                        // If request is cancelled, the result arrays are empty.
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
                        {
                            // permission was granted, yay!
                        }
                        else
                        {
                            finish();
                            // permission denied, boo! Disable the functionality that depends on this permission.
                        }
                    }
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
