package com.sunflower.catchtherainbow.Views.Editing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.icu.util.IslamicCalendar;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.sunflower.catchtherainbow.AudioClasses.WaveTrack;
import com.sunflower.catchtherainbow.R;

/**
 * Created by SuperComputer on 3/29/2017.
 */

public class Timeline extends View
{
    private long offset;
    private int samplesPerPixel;

    protected Paint linePaint;

    public Timeline(Context context)
    {
        super(context);

        init();
    }

    public Timeline(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);

        init();
    }

    private void init()
    {
        // setBackgroundResource(R.color.colorAccent);

        linePaint = new Paint();
        linePaint.setColor(Color.WHITE);
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        int width = getWidth();
        int height = getHeight();

        //int sampleRate = 44100;
        //(double) samplesPerPixel / (sampleRate * offset);

        for(int i = 0; i < width; i+=15)
        {
            canvas.drawLine(i, 1, i, height-1, linePaint);
        }
    }

    public void setSamplesPerPixel(int samplesPerPixel)
    {
        this.samplesPerPixel = samplesPerPixel;

        invalidate();
    }

    public int getSamplesPerPixel()
    {
        return samplesPerPixel;
    }

    public float getOffset()
    {
        return offset;
    }

    // offset in samples
    public void setOffset(long offset)
    {
        this.offset = offset;

        invalidate();
    }

}
