package com.sunflower.catchtherainbow.Adapters;

/**
 * Created by Alexandr on 05.02.2017.
 */

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.sunflower.catchtherainbow.R;
import com.sunflower.catchtherainbow.Views.FragTabAudioFiles;
import com.sunflower.catchtherainbow.Views.FragTabFolders;

public class CustomAdapterFragPager extends FragmentPagerAdapter
{
    private String fragments [] = {"Audio Files", "Folders"};
    private TabLayout tabLayout;
    private FragTabAudioFiles.FragAudioListener  frAudioLis;
    public CustomAdapterFragPager(FragmentManager supportFragmentManager, Context applicationContext, TabLayout tabLayout, FragTabAudioFiles.FragAudioListener frAudioLis)
    {
        super(supportFragmentManager);
        this.frAudioLis = frAudioLis;
        this.tabLayout = tabLayout;
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                FragTabAudioFiles fr = new FragTabAudioFiles();
                fr.addAudioListener(frAudioLis);
                return fr;
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
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_folder);
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
