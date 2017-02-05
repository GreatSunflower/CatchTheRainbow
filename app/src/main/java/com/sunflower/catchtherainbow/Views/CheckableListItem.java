package com.sunflower.catchtherainbow.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.RelativeLayout;

/**
 * Created by SuperComputer on 11/22/2016.
 */

public class CheckableListItem extends RelativeLayout implements Checkable
{

    public CheckableListItem(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    private boolean checked;

    @Override
    public boolean isChecked()
    {
        return checked;
    }

    @Override
    public void setChecked(boolean checked)
    {
        this.checked = checked;

        //CheckBox iv = (CheckBox) findViewById(R.id.check);
        //iv.setChecked(checked);
    }

    @Override
    public void toggle()
    {
        setChecked(!this.checked);
    }
}