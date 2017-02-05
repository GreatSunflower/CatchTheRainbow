package com.sunflower.catchtherainbow.Adapters;

/**
 * Created by Alexandr on 05.02.2017.
 */

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.sunflower.catchtherainbow.R;
import com.sunflower.catchtherainbow.Views.FragTabAudioFiles;
import com.sunflower.catchtherainbow.Views.FragTabFolders;

public class CustomAdapterFragPager extends FragmentPagerAdapter
{
    private String fragments [] = {"Audio Files", "Folders"};
    private TabLayout tabLayout;

    public CustomAdapterFragPager(FragmentManager supportFragmentManager, Context applicationContext, TabLayout tabLayout)
    {
        super(supportFragmentManager);

        this.tabLayout = tabLayout;
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                return new FragTabAudioFiles();
            case 1:
                return new FragTabFolders();
            default:
                return null;
        }
    }

    @Override
    public void startUpdate(ViewGroup container)
    {
        super.startUpdate(container);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_music_note);
    }

    @Override
    public int getCount()
    {
        return fragments.length;
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
       // tabLayout.getTabAt(0).setIcon(ICONS[0]);
        return fragments[position];
    }
}
