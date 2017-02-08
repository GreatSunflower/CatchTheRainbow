package com.sunflower.catchtherainbow.Views.Helpful;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.AbsSeekBar;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.sunflower.catchtherainbow.R;

/**
 *  A seek bar than accepts float values and has minimum
 */

public class SuperSeekBar extends SeekBar implements SeekBar.OnSeekBarChangeListener
{
    private float MaxValue ;
    private float MinValue;

    //private int mCurrentValue;

    public SuperSeekBar(Context context)
    {
        super(context);
        super.setOnSeekBarChangeListener(this);
    }

    public SuperSeekBar(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        applyAttrs(attrs);
        super.setOnSeekBarChangeListener(this);
    }

    private void applyAttrs(AttributeSet attrs)
    {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RangedParameters);
        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i)
        {
            int attr = a.getIndex(i);
            switch (attr)
            {
                case R.styleable.RangedParameters_max:
                    setMaxValue(a.getFloat(attr, 1.0f));
                    break;
                case R.styleable.RangedParameters_min:
                    setMinValue(a.getFloat(attr, 0.0f));
                    break;
            }
        }
        a.recycle();
    }


    public float getCurrentValue()
    {
        return (MaxValue - MinValue) * ((float) getProgress() / (float) getMax()) + MinValue;
    }

    public void setCurrentValue(float value)
    {
        setProgress((int) ((value - MinValue) / (MaxValue - MinValue) * getMax()));
    }


    public float getMaxValue()
    {
        return MaxValue;
    }

    public void setMaxValue(float maxValue)
    {
        if(maxValue < getCurrentValue())
            setCurrentValue(maxValue);

        MaxValue = maxValue;
    }

    public float getMinValue()
    {
        return MinValue;
    }

    public void setMinValue(float minValue)
    {
        if(minValue > getCurrentValue())
            setCurrentValue(minValue);

        MinValue = minValue;
    }

    // Listeners
    private OnSuperSeekBarChangeListener OnSeekBarChangeListener;

    public void setOnSuperSeekBarChangeListener(OnSuperSeekBarChangeListener l)
    {
        OnSeekBarChangeListener = l;
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        //setCurrentValue(progress);
        if(OnSeekBarChangeListener != null)
            OnSeekBarChangeListener.onProgressChanged(this, getCurrentValue(), fromUser);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {
        if(OnSeekBarChangeListener != null)
            OnSeekBarChangeListener.onStartTrackingTouch(this);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {
        if(OnSeekBarChangeListener != null)
            OnSeekBarChangeListener.onStopTrackingTouch(this);
    }

    public interface OnSuperSeekBarChangeListener
    {

        void onProgressChanged(SuperSeekBar seekBar, float progress, boolean fromUser);

        void onStartTrackingTouch(SuperSeekBar seekBar);

        void onStopTrackingTouch(SuperSeekBar seekBar);
    }
}
