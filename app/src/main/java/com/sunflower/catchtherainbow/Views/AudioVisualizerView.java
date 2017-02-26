package com.sunflower.catchtherainbow.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by SuperComputer on 2/6/2017.
 */

public class AudioVisualizerView extends View
{
    // keeps wave data in normalized form(from -1 to 1)
    private short[] shortBuffer;

    // actual points to draw
    private float[] mPoints;

    private Rect mRect = new Rect();
    private Paint mForePaint = new Paint();

    private DrawingKind drawingKind = DrawingKind.Points;

    //private SuperGLRenderer mRenderer;


    public AudioVisualizerView(Context context, DrawingKind kind)
    {
        super(context);
        this.drawingKind = kind;
        init();
    }

    public AudioVisualizerView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    private void init()
    {
        //floatBuffer = null;
        shortBuffer = null;

        // Create an OpenGL ES 2.0 context
        //   setEGLContextClientVersion(2);

        //     mRenderer = new SuperGLRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        //  setRenderer(mRenderer);

        // make ihe wave beautiful(or not)
        mForePaint.setStrokeWidth(1.5f);
        mForePaint.setAntiAlias(true);
        mForePaint.setStyle(Paint.Style.STROKE);
        mForePaint.setStrokeJoin(Paint.Join.ROUND);
        mForePaint.setStrokeCap(Paint.Cap.ROUND);
        mForePaint.setColor(Color.rgb(255, 255, 255));
    }

    // actual method that updates the view
    public void updateVisualizer(short[] newByteBuffer)
    {
        shortBuffer = newByteBuffer;
        invalidate();
    }

    // For bezier!!!
    private Path path = new Path();
    private float x1, y1, x2, y2, x3, y3;


    @Override
    protected void onDraw(final Canvas canvas)
    {
        super.onDraw(canvas);

        // no need to update yet
        if (shortBuffer == null)
        {
            return;
        }
        canvas.drawColor(Color.TRANSPARENT);

        // size of the view
        mRect.set(0, 0, getWidth(), getHeight());

        if(drawingKind == DrawingKind.Points)
        {
            // if there are no points or they are less than buffer size
            if (mPoints == null || mPoints.length < shortBuffer.length * 4)
            {
                mPoints = new float[shortBuffer.length * 4];
            }

            for (int i = 0; i < shortBuffer.length - 1; i++)
            {
                int x1 = mRect.width() * i / (shortBuffer.length - 1);
                int y1 = (32767 - shortBuffer[i]) * mRect.height() / 65536;

                int x2 = mRect.width() * (i + 1) / (shortBuffer.length - 1);
                int y2 = (32767 - shortBuffer[i + 1]) * mRect.height() / 65536;

                mPoints[i * 4] = x1;
                mPoints[i * 4 + 1] = y1;
                mPoints[i * 4 + 2] = x2;
                mPoints[i * 4 + 3] = y2;
            }
            canvas.drawLines(mPoints, mForePaint);
        }
        else
        {
            // clear the path
            path.reset();

            if(drawingKind == DrawingKind.Points)
            {
                for (int i = 0; i < shortBuffer.length; i++)
                {
                    int value = (32767 - shortBuffer[i]) * mRect.height() / 65536;

                    float x = mRect.width() * i / (shortBuffer.length - 1);
                    float y = value;

                    if (i == 0)
                    {
                        path.moveTo(x, y);
                    }
                    else
                    {
                        path.lineTo(x, y);
                    }
                }
            }
            else if(drawingKind == DrawingKind.Bezier)
            {
                path.reset();

                for (int i = 0; i < shortBuffer.length - 2; i += 2)
                {
                    x1 = mRect.width() * i / (shortBuffer.length - 1);
                    y1 = (32767 - shortBuffer[i]) * mRect.height() / 65536;

                    x2 = mRect.width() * (i + 1) / (shortBuffer.length - 1);
                    y2 = (32767 - shortBuffer[i + 1]) * mRect.height() / 65536;

                    x3 = mRect.width() * (i + 2) / (shortBuffer.length - 1);
                    y3 = (32767 - shortBuffer[i + 2]) * mRect.height() / 65536;

                    path.moveTo(x1, y1);
                    //path.cubicTo(x2, y2, x3, y3, x4, y4);
                    path.quadTo(x2, y2, x3, y3);
                    path.moveTo(x3, y3);
                }
            }
            canvas.drawPath(path, mForePaint);
        }


        // Bezier drawing is much, much slower!!!
       /* mRect.set(0, 0, getWidth(), getHeight());

        path.reset();

        for (int i = 0; i < floatBuffer.length - 2; i += 2)
        {
            x1 = mRect.width() * i / (floatBuffer.length - 1);
            y1 = mRect.height() / 2 + floatBuffer[i] * (mRect.height() / 2);

            x2 = mRect.width() * (i + 1) / (floatBuffer.length - 1);
            y2 = mRect.height() / 2 + floatBuffer[i + 1] * (mRect.height() / 2);

            x3 = mRect.width() * (i + 2) / (floatBuffer.length - 1);
            y3 = mRect.height() / 2 + floatBuffer[i + 2] * (mRect.height() / 2);

            path.moveTo(x1, y1);
            //path.cubicTo(x2, y2, x3, y3, x4, y4);
            path.quadTo(x2, y2, x3, y3);
            path.moveTo(x3, y3);
        }

        canvas.drawPath(path, mForePaint);*/
    }

    public DrawingKind getDrawingKind()
    {
        return drawingKind;
    }

    public void setDrawingKind(DrawingKind drawingKind)
    {
        this.drawingKind = drawingKind;
    }

    public enum DrawingKind
    {
        Points, /* The fastest */
        Path,
        Bezier/* The slowest */
    }

}
