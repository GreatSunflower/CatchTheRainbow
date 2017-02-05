package com.sunflower.catchtherainbow.Adapters;

/**
 * Created by Alexandr on 05.02.2017.
 */

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.sunflower.catchtherainbow.Views.FragTabAudioFiles;
import com.sunflower.catchtherainbow.Views.FragTabFolders;

public class CustomAdapterFragPager extends FragmentPagerAdapter
{
    private String fragments [] = {"Audio Files","Folders"};

    public CustomAdapterFragPager(FragmentManager supportFragmentManager, Context applicationContext)
    {
        super(supportFragmentManager);
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position){
            case 0:
                return new FragTabAudioFiles();
            case 1:
                return new FragTabFolders();
            default:
                return null;
        }
    }

    @Override
    public int getCount()
    {
        return fragments.length;
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        return fragments[position];
    }
}
