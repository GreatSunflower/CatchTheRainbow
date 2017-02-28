package com.sunflower.catchtherainbow.Adapters;

import com.sunflower.catchtherainbow.Views.Effects.BaseEffectFragment;

/**
 * Created by Alexandr on 27.02.2017.
 */
public class ItemEffect
{
    private Class<? extends BaseEffectFragment> fragment;
    private String title;
    public ItemEffect(String title, Class<? extends BaseEffectFragment> fragment)
    {
        this.title = title;
        this.fragment = fragment;
    }

    public String getTitle()
    {
        return title;
    }

    public Class<? extends BaseEffectFragment> getFragment()
    {
        return fragment;
    }
}
