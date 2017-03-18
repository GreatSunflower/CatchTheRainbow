package com.sunflower.catchtherainbow.Adapters;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

import com.sunflower.catchtherainbow.Views.StartedApp.FragTabCreateProject;
import com.sunflower.catchtherainbow.Views.StartedApp.FragTabOpenProject;
import com.sunflower.catchtherainbow.Views.StartedApp.ProjectStartActivity;

/**
 * Created by Alexandr on 17.03.2017.
 */

public class StartProjectPagerAdapter extends FragmentPagerAdapter
{
    private String fragments [] = {"Create Project", "Open Project"};
    private TabLayout tabLayout;
    private FragTabCreateProject fragTabCreateProject;
    private FragTabOpenProject fragTabOpenProject;
    private ProjectStartActivity projectStartActivity;

    public StartProjectPagerAdapter(FragmentManager supportFragmentManager, TabLayout tabLayout, ProjectStartActivity projectStartActivity)
    {
        super(supportFragmentManager);
        this.tabLayout = tabLayout;
        this.tabLayout.setVisibility(View.GONE);
        this.projectStartActivity = projectStartActivity;
    }

    public FragTabCreateProject GetFragTabCreateProject()
    {
        return fragTabCreateProject;
    }

    public FragTabOpenProject GetFragTabOpenProject()
    {
        return fragTabOpenProject;
    }

    public Fragment GetFragmentByPosition(int position)
    {
        Fragment frag = new Fragment();
        if(position == 0) frag = fragTabCreateProject;
        if(position == 1) frag = fragTabOpenProject;
        return frag;
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                fragTabCreateProject = new FragTabCreateProject();
                fragTabCreateProject.AddLinkProjectStartActivity(projectStartActivity);
                return fragTabCreateProject;
            case 1:
                fragTabOpenProject = new FragTabOpenProject();
                fragTabOpenProject.AddLinkProjectStartActivity(projectStartActivity);
                return fragTabOpenProject;
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

