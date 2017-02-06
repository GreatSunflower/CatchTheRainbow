package com.sunflower.catchtherainbow.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by SuperComputer on 2/6/2017.
 */

public class AudioVisualizerView extends View
{
    // keeps wave data in normalized form(from -1 to 1)
    private float[] floatBuffer;

    // actual points to draw
    private float[] mPoints;

    private Rect mRect = new Rect();
    private Paint mForePaint = new Paint();

    public AudioVisualizerView(Context context)
    {
        super(context);
        init();
    }

    public AudioVisualizerView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public AudioVisualizerView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        floatBuffer = null;
        // make ihe wave beautiful(or not)
        mForePaint.setStrokeWidth(1.5f);
        mForePaint.setAntiAlias(true);
        mForePaint.setStrokeJoin(Paint.Join.ROUND);
        mForePaint.setStrokeCap(Paint.Cap.ROUND);
        mForePaint.setColor(Color.rgb(255, 255, 255));
    }

    // actual method that updates the view
    public void updateVisualizer(float[] newFloatBuffer)
    {
        floatBuffer = newFloatBuffer;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        // no need to update yet
        if (floatBuffer == null)
        {
            return;
        }

        // if there are no points or they are less than buffer size
        if (mPoints == null || mPoints.length < floatBuffer.length * 4)
        {
            mPoints = new float[floatBuffer.length * 4];
        }

        // size of the view
        mRect.set(0, 0, getWidth(), getHeight());

        for (int i = 0; i < floatBuffer.length - 1; i++)
        {
            //to set two points of the line we need to fill 4 cells of the array

            mPoints[i * 4] = mRect.width() * i / (floatBuffer.length - 1);
            mPoints[i * 4 + 1] = mRect.height() / 2  + floatBuffer[i] * (mRect.height() / 2);
            mPoints[i * 4 + 2] = mRect.width() * (i + 1) / (floatBuffer.length - 1);
            mPoints[i * 4 + 3] = mRect.height() / 2 + floatBuffer[i + 1] * (mRect.height() / 2);
        }
        canvas.drawLines(mPoints, mForePaint);

    }

}