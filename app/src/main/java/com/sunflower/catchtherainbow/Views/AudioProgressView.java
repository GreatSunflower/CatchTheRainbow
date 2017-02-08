package com.sunflower.catchtherainbow.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sunflower.catchtherainbow.Helper;
import com.sunflower.catchtherainbow.R;

/**
 * Created by SuperComputer on 2/5/2017.
 */

public class AudioProgressView extends RelativeLayout
{
    private SeekBar audioProgressBar;
    private TextView curTimeTextView, audioDurationTextView;

    // Time Variables
    private float max = 0.f;
    private float current = 0.f;

    public AudioProgressView(Context context)
    {
        super(context);
        init();
    }

    public AudioProgressView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();

        //get the attributes specified in attrs.xml using the name we included
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RangedParameters, 0, 0);
        try
        {
            //get the text and colors specified using the names in attrs.xml
            setMax((int)a.getFloat(R.styleable.RangedParameters_max, 0.f));
            setCurrent((int)a.getFloat(R.styleable.RangedParameters_current, 0.f));
        }
        catch (Exception e){}
        finally
        {
            a.recycle();
        }
    }

    private void init()
    {
       // ViewGroup root = new RelativeLayout(getContext());
       // super.getRootView().
        inflate(getContext(), R.layout.view_audio_progress, this);
        this.curTimeTextView = (TextView)findViewById(R.id.currentTime);
        this.audioDurationTextView = (TextView)findViewById(R.id.totalDuration);
        this.audioProgressBar = (SeekBar) findViewById(R.id.audioSeekBar);
    }

    public float getMax()
    {
        return max;
    }

    public void reset()
    {
        setMax(0);
    }

    public void setMax(float max)
    {
        this.max = max;
        audioProgressBar.setMax((int) max);
        curTimeTextView.setText("00:00");
        audioDurationTextView.setText(Helper.secondToString(max));
    }

    public float getCurrent()
    {
        return current;
    }

    public void setCurrent(float current)
    {
        this.current = current;
        audioProgressBar.setProgress((int) current);
        curTimeTextView.setText(Helper.secondToString(current));
    }

    public void setOnSeekBar(SeekBar.OnSeekBarChangeListener listener)
    {
        audioProgressBar.setOnSeekBarChangeListener(listener);
    }
}
