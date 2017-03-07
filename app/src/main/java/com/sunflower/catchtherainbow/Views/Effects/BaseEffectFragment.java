package com.sunflower.catchtherainbow.Views.Effects;

import android.support.v4.app.Fragment;

/**
 * Created by Alexandr on 28.02.2017.
 */

public abstract class BaseEffectFragment extends Fragment
{
    protected int chan = 0;
    public BaseEffectFragment() {/*Required empty public constructor*/}

    public void setChannel(int chan)
    {
        this.chan = chan;
    }

    public void setEffect()
    {

    }

    public boolean cancel()
    {
        return false;
    }
}
