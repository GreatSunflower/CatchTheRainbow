package com.sunflower.catchtherainbow.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.Arrays;

/**
 * Created by SuperComputer on 2/6/2017.
 */

public class AudioVisualizerView extends View
{
    private byte[] mBytes;
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
        mBytes = null;
        mForePaint.setStrokeWidth(1f);
        mForePaint.setAntiAlias(true);
        mForePaint.setColor(Color.rgb(255, 255, 255));
    }

    public void updateVisualizer(byte[] bytes)
    {
        mBytes = bytes;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if (mBytes == null)
        {
            return;
        }

        int dividerFactor = 50;

        if (mPoints == null || mPoints.length < mBytes.length/dividerFactor * 4)
        {
            mPoints = new float[mBytes.length/dividerFactor * 4];
        }
        mRect.set(0, 0, getWidth(), getHeight());
        for (int i = 0; i < mBytes.length/dividerFactor - 1; i++)
        {
            mPoints[i * 4] = mRect.width() * i / (mBytes.length/dividerFactor - 1);
            mPoints[i * 4 + 1] = mRect.height() / 2  + ((byte) (mBytes[i] + 128)) * (mRect.height() / 2) / 128;
            mPoints[i * 4 + 2] = mRect.width() * (i + 1) / (mBytes.length/dividerFactor - 1);
            mPoints[i * 4 + 3] = mRect.height() / 2 + ((byte) (mBytes[i + 1] + 128)) * (mRect.height() / 2) / 128;
        }
        canvas.drawLines(mPoints, mForePaint);

    }

}