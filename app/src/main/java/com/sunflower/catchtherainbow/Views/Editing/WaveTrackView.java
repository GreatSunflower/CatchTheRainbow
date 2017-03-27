package com.sunflower.catchtherainbow.Views.Editing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.sunflower.catchtherainbow.AudioClasses.WaveTrack;
import com.un4seen.bass.BASS;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by SuperComputer on 3/25/2017.
 *
 */

// Draws clips of the track
public class WaveTrackView extends View
{
    protected int samplesPerPixel = 1;

    protected WaveTrack track;

    protected Paint wavePaint = new Paint();

    private Rect rect = new Rect();

    protected GestureDetector gestureDetector;
    protected ScaleGestureDetector scaleGestureDetector;
    protected float initialScaleSpan;

    protected float offset = 0;

    protected WaveTrackViewListener listener;

    // should be created from code
    public WaveTrackView(Context context, WaveTrack track, int samplesPerPixel)
    {
        super(context);

        this.track = track;
        this.samplesPerPixel = samplesPerPixel;

        wavePaint.setColor(Color.WHITE);
        wavePaint.setStyle(Paint.Style.STROKE);
        //setBackgroundColor(Color.WHITE);

        gestureDetector = new GestureDetector(
                context,
                new GestureDetector.SimpleOnGestureListener()
                {
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy)
                    {
                        if(listener != null)
                            listener.fling(vx);
                        return true;
                    }
                });

        scaleGestureDetector = new ScaleGestureDetector(context,
                new ScaleGestureDetector.SimpleOnScaleGestureListener()
                {
                    public boolean onScaleBegin(ScaleGestureDetector d)
                    {
                        initialScaleSpan = Math.abs(d.getCurrentSpanX());
                        return true;
                    }
                    public boolean onScale(ScaleGestureDetector d)
                    {
                        float scale = Math.abs(d.getCurrentSpanX());
                        if (scale - initialScaleSpan > 40)
                        {
                            if(listener != null)
                                listener.zoomIn();
                            initialScaleSpan = scale;
                        }
                        if (scale - initialScaleSpan < -40)
                        {
                            if(listener != null)
                                listener.zoomOut();
                            initialScaleSpan = scale;
                        }
                        return true;
                    }
                });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        scaleGestureDetector.onTouchEvent(event);
        if (gestureDetector.onTouchEvent(event))
        {
            return true;
        }
        if(listener == null)
            return true;

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                listener.touchStart(event.getX());
                break;
            case MotionEvent.ACTION_MOVE:
                listener.touchMove(event.getX());
                break;
            case MotionEvent.ACTION_UP:
                listener.touchEnd();
                break;
        }
        return true;
    }

    float []points;
    Path path = new Path();

    @Override
    protected void onDraw(Canvas canvas)
    {
        rect.set(0, 0, getWidth(), getHeight());


        ByteBuffer buffer = ByteBuffer.allocateDirect(rect.width()*4);
        track.get(buffer, (int) offset, rect.width());

        float []floatBuffer = new float[rect.width()];
        buffer.rewind();
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.asFloatBuffer().get(floatBuffer);

        // if there are no points or they are less than buffer size
        if (points == null || points.length < points.length * 4)
        {
            points = new float[floatBuffer.length * 4];
        }

        path.reset();
        path.moveTo(0, rect.height()/2);
        for (int i = 0; i < floatBuffer.length - 1; i++)
        {
            float x1 = rect.width() * i / (floatBuffer.length - 1);
            float y1 = rect.height() / 2 + floatBuffer[i] * (rect.height() / 2);

            float x2 = rect.width() * (i + 1) / (floatBuffer.length - 1);
            float y2 = rect.height() / 2 + floatBuffer[i + 1] * (rect.height() / 2);

            points[i * 4] = x1;
            points[i * 4 + 1] = y1 ;
            points[i * 4 + 2] = x2;
            points[i * 4 + 3] = y2;

            path.lineTo(x1, y1);
            path.lineTo(x2, y2);
        }
        canvas.drawLines(points, wavePaint);
        //canvas.drawPath(path, wavePaint);
    }

    public WaveTrackViewListener getListener()
    {
        return listener;
    }

    public void setListener(WaveTrackViewListener listener)
    {
        this.listener = listener;
    }

    public float getOffset()
    {
        return offset;
    }

    public void setOffset(float offset)
    {
        this.offset = offset;
        invalidate();
    }



    public interface WaveTrackViewListener
    {
        void touchStart(float x);
        void touchMove(float x);
        void touchEnd();
        void fling(float x);
        void draw();
        void zoomIn();
        void zoomOut();
    }
}
