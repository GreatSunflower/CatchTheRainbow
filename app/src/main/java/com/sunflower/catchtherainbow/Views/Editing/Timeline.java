package com.sunflower.catchtherainbow.Views.Editing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.icu.util.IslamicCalendar;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.sunflower.catchtherainbow.AudioClasses.AudioHelper;
import com.sunflower.catchtherainbow.AudioClasses.Project;
import com.sunflower.catchtherainbow.AudioClasses.WaveTrack;
import com.sunflower.catchtherainbow.Helper;
import com.sunflower.catchtherainbow.R;

/**
 * Created by SuperComputer on 3/29/2017.
 */

public class Timeline extends View
{
    private long offset;
    private int samplesPerPixel = 1;

    protected Paint linePaint;
    protected Paint lestTextPaint, rightTextPaint;

    protected Project project;

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

        lestTextPaint = new Paint();
        lestTextPaint.setTextSize(18);
        lestTextPaint.setShadowLayer(2, 1, 1, Color.BLACK);
        lestTextPaint.setColor(Color.WHITE);

        rightTextPaint = new Paint();
        rightTextPaint.setTextSize(18);
        rightTextPaint.setShadowLayer(2, 1, 1, Color.BLACK);
        rightTextPaint.setColor(Color.WHITE);
        rightTextPaint.setTextAlign(Paint.Align.RIGHT);
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        int width = getWidth();
        int height = getHeight();

        //int sampleRate = 44100;
        //(double) samplesPerPixel / (sampleRate * offset);

        for(int i = 0; i < width; i+=16)
        {
            canvas.drawLine(i, height/2, i, height - 3, linePaint);
        }

        // time offsets
        if(project != null)
        {
            double time = AudioHelper.samplesToTime(offset, project.getProjectAudioInfo());
            canvas.drawText(Helper.round(time, 2)+"", 5, height/2-5, lestTextPaint);

            // right
            time = AudioHelper.samplesToTime(offset+width*samplesPerPixel, project.getProjectAudioInfo());
            canvas.drawText(Helper.round(time, 2)+"", width - 5, height/2-5, rightTextPaint);
        }

        // left
        canvas.drawLine(0, 1.5f, 0, height - 1, linePaint);

        // center
        canvas.drawLine(width / 2, 1.5f, width / 2, height - 1, linePaint);

        // right
        canvas.drawLine(width - 2, 1.5f, width - 2, height - 1, linePaint);

    }

    public Project getProject()
    {
        return project;
    }

    public void setProject(Project project)
    {
        this.project = project;
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
