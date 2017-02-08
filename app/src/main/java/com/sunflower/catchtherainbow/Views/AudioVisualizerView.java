package com.sunflower.catchtherainbow.Views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.opengl.EGLConfig;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.View;

import com.sunflower.catchtherainbow.AudioClasses.SuperAudioPlayer;

import javax.microedition.khronos.opengles.GL10;

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

    //private SuperGLRenderer mRenderer;


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

    private void init()
    {
        floatBuffer = null;

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
    public void updateVisualizer(float[] newFloatBuffer)
    {
        floatBuffer = newFloatBuffer;
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
}
