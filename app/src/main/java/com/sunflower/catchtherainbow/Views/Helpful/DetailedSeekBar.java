package com.sunflower.catchtherainbow.Views.Helpful;

/**
 * Created by SuperComputer on 1/25/2017.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunflower.catchtherainbow.Helper;
import com.sunflower.catchtherainbow.R;

public final class DetailedSeekBar extends RelativeLayout implements SuperSeekBar.OnSuperSeekBarChangeListener
{
    private static final String ATTR_MIN_VALUE = "min";
    private static final String ATTR_MAX_VALUE = "max";
    private static final String ATTR_DEFAULT_VALUE = "def";



    // ---- Listener
    private OnSuperSeekBarListener mListener;


    public void setListener(OnSuperSeekBarListener mListener)
    {
        this.mListener = mListener;
    }
    // ---listener end

    private float mDefaultValue;
    private float mMaxValue;
    private float mMinValue;

    private float mCurrentValue;

    private SuperSeekBar mSeekBar;
    private TextView mValueText, tv_MinValue, tv_MaxValue;

    public DetailedSeekBar(Context context)
    {
        super(context);
        init();
    }

    public DetailedSeekBar(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();

        //get the attributes specified in attrs.xml using the name we included
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RangedParameters, 0, 0);
        try
        {
            //get the text and colors specified using the names in attrs.xml
            setMaxValue(a.getFloat(R.styleable.RangedParameters_max, getMaxValue()));
            setMinValue(a.getFloat(R.styleable.RangedParameters_min, getMinValue()));
            setDefaultValue(a.getFloat(R.styleable.RangedParameters_current, getDefaultValue()));
        }
        catch (Exception e){}
        finally
        {
            a.recycle();
        }
    }

    void init()
    {
        inflate(getContext(), R.layout.super_slider, this);

        this.mValueText = (TextView) findViewById(R.id.current_value);
        this.tv_MinValue =  ((TextView) findViewById(R.id.min_value));
        this.tv_MaxValue = (TextView) findViewById(R.id.max_value);

        mSeekBar = (SuperSeekBar) findViewById(R.id.seek_bar);

        setMinValue(0.f);
        setMaxValue(1.f);
        setDefaultValue(1.f);

        mCurrentValue = getDefaultValue();

        mSeekBar.setMaxValue(getMaxValue());
        mSeekBar.setCurrentValue(mCurrentValue);
        mSeekBar.setOnSuperSeekBarChangeListener(this);
    }

    public OnSuperSeekBarListener getListener()
    {
        return mListener;
    }

    public void onProgressChanged(SuperSeekBar seek, float value, boolean fromTouch)
    {
        mCurrentValue = value;

        mValueText.setText(Helper.round(mCurrentValue, 2)+"");

        if(mListener != null)
            mListener.onChange(this, mCurrentValue);
    }

    @Override
    public void onStartTrackingTouch(SuperSeekBar seekBar) {  }

    @Override
    public void onStopTrackingTouch(SuperSeekBar seekBar) {  }

    public float getDefaultValue()
    {
        return mDefaultValue;
    }

    // getters & setters
    public void setDefaultValue(float DefaultValue)
    {
        if(DefaultValue < mMinValue || DefaultValue > mMaxValue) return;

        this.mDefaultValue = DefaultValue;
        mValueText.setText(Helper.round(mDefaultValue, 2)+"");
        mSeekBar.setCurrentValue(DefaultValue);
    }

    public float getMaxValue()
    {
        return mMaxValue;
    }

    public void setMaxValue(float MaxValue)
    {
        if(MaxValue < mMinValue) return;
        this.mMaxValue = MaxValue;

        mSeekBar.setMaxValue(MaxValue);
        tv_MaxValue.setText(mMaxValue+"");
    }

    public float getMinValue()
    {
        return mMinValue;
    }

    public void setMinValue(float MinValue)
    {
        if(MinValue > mMaxValue) return;
        this.mMinValue = MinValue;

        mSeekBar.setMinValue(MinValue);
        tv_MinValue.setText(MinValue+"");
    }
    // getters & setters end


    public interface OnSuperSeekBarListener
    {
        void onChange(DetailedSeekBar seekBar, float selectedValue);
    }
}