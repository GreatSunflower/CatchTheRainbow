package com.sunflower.catchtherainbow.Views.Editing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.support.v4.view.VelocityTrackerCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.TextureView;
import android.view.VelocityTracker;
import android.view.View;

import com.sunflower.catchtherainbow.AudioClasses.Clip;
import com.sunflower.catchtherainbow.AudioClasses.WaveData;
import com.sunflower.catchtherainbow.AudioClasses.WaveTrack;
import com.sunflower.catchtherainbow.R;

import java.io.IOException;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by SuperComputer on 3/25/2017.
 *
 */

// Draws clips of the track
public class WaveTrackView extends TextureView implements TextureView.SurfaceTextureListener
{
    public static final int MAX_SAMPLES_PER_PIXEL = 393216/108;

    protected int samplesPerPixel = 1;
    protected int zoomFactor = 2;

    protected WaveTrack track;

    private Rect rect = new Rect();

    private Boolean isScaling = false;

    protected GestureDetector gestureDetector;
    protected ScaleGestureDetector scaleGestureDetector;
    protected float initialScaleSpan;
    protected WaveData data;

    protected Paint wavePaint = new Paint();
    protected Paint selectionPaint = new Paint();

    // should update data
    //private boolean isDirty = true;
    private Queue<Boolean> updateQueue = new LinkedList<>();

    // used for pausing drawing
    private boolean isSuspended = false;

    protected int backgroundColor;

    private float x1, y1, x2, y2;

    // selection range
    protected MainAreaFragment.SampleRange selection = new MainAreaFragment.SampleRange();

    protected long offset = 0;

    protected WaveTrackViewListener listener;

    // should be created from code
    public WaveTrackView(Context context, WaveTrack track, int samplesPerPixel)
    {
        super(context);

        this.track = track;
        this.samplesPerPixel = samplesPerPixel;

        // gesture detectors
        scaleGestureDetector = new ScaleGestureDetector(context, scaleGestureListener);

        setFocusable(true);

        setSurfaceTextureListener(this);

        // Drawing
        backgroundColor = context.getResources().getColor(R.color.colorPrimary);

        wavePaint.setStrokeWidth(1.1f/*1.4f*/);
        wavePaint.setColor(Color.WHITE);
        wavePaint.setStyle(Paint.Style.STROKE);
        wavePaint.setAntiAlias(true);
        //wavePaint.setDither(true);
        wavePaint.setStrokeCap(Paint.Cap.ROUND);
        wavePaint.setStrokeJoin(Paint.Join.ROUND);

        selectionPaint.setColor(context.getResources().getColor(R.color.colorAccent));
        selectionPaint.setAlpha(50);

        // make view to gather the data
        demandFullUpdate();
    }

    protected ScaleGestureDetector.SimpleOnScaleGestureListener scaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener()
    {
        public boolean onScaleBegin(ScaleGestureDetector d)
        {
            initialScaleSpan = Math.abs(d.getCurrentSpanX());
            isScaling = true;
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
        public void onScaleEnd(ScaleGestureDetector detector)
        {
            isScaling = false;
        }
    };

    private VelocityTracker velocityTracker = null;
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        scaleGestureDetector.onTouchEvent(event);
        if (/*gestureDetector.onTouchEvent(event) || */isScaling)
        {
            return true;
        }
        if(listener == null)
            return true;

        int index = event.getActionIndex();
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(index);

        getParent().requestDisallowInterceptTouchEvent(true);

        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                /*if(velocityTracker == null)
                {*/
                    // Retrieve a new VelocityTracker object to watch the velocity of a motion.
                    velocityTracker = VelocityTracker.obtain();
                /*}
                else
                {
                    // Reset the velocity tracker back to its initial state.
                    velocityTracker.clear();
                }*/
                // Add a user's movement to the tracker.
                velocityTracker.addMovement(event);

                listener.touchStart(event.getX());
                break;
            case MotionEvent.ACTION_MOVE:

                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(1000);

                float velX = VelocityTrackerCompat.getXVelocity(velocityTracker, pointerId);
                float velY = VelocityTrackerCompat.getYVelocity(velocityTracker, pointerId);

                // Y velocity input should be limited
                if(Math.abs(velY) > 300)
                {
                    //velocityTracker.recycle();
                    //listener.touchEnd();
                    //event.setAction(MotionEvent.ACTION_CANCEL);
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return false;
                }

                // focus row
                ((View)getTag()).requestFocus();

                // to avoid weird-looking jumps
                if(Math.abs(velX) < 20)
                {
                    return true;
                }

                listener.touchMove(event.getX());
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                velocityTracker.recycle();
                listener.touchEnd();
                break;
        }
        return true;
    }

    public void demandFullUpdate()
    {
        //isDirty = true;
        updateQueue.add(Boolean.TRUE);
    }

    // doesn't update data, only redraws
    public void demandDrawUpdate()
    {
        //isDirty = true;
        updateQueue.add(Boolean.FALSE);
    }

    public void setSamplesPerPixel(int samplesPerPixel)
    {
        if(samplesPerPixel < 1 || samplesPerPixel > WaveTrackView.MAX_SAMPLES_PER_PIXEL)
            return;

        this.samplesPerPixel = samplesPerPixel;

        demandFullUpdate();
    }

   /* @Override
    protected void onDraw(Canvas canvas)
    {
        rect.set(0, 0, getWidth(), getHeight());

       /* textPaint.setColor(Color.RED);
        textPaint.setFakeBoldText(true);
        textPaint.setTextSize(rect.height()/3f);
        //textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Samples: " + samplesPerPixel, 15, rect.height()/2, textPaint);*/
        //path.reset();

      /*  Clip startingClip = track.getClipAtSample((int) (offset*samplesPerPixel));
        if(startingClip == null) return;

        int frames = rect.width();
        WaveData data = new WaveData(frames);

        // if there are no points or they are less than buffer size
        if (points == null || points.length < frames * 4)
        {
            points = new float[frames * 4];
        }

        int startSample = (int) (offset*samplesPerPixel);
        int len = rect.width()*samplesPerPixel;

        startingClip.getWaveData(startSample, len, samplesPerPixel, data);

        if(data.max.length > 0) path.moveTo(data.min[0], data.max[0]);
        for(int i = 0; i < frames; i++)
        {
            float x1 = i ;
            float y1 = rect.height() / 2 + data.min[i] * (rect.height() / 2);

            float x2 = i ;
            float y2 = rect.height() / 2 + data.max[i] * (rect.height() / 2);

            points[i * 4] = x1;
            points[i * 4 + 1] = y1 ;
            points[i * 4 + 2] = x2;
            points[i * 4 + 3] = y2;

            canvas.drawLine(x1, y1, x2, y2, wavePaint);

            //path.quadTo(x1, y1, x2, y2);
        }
        //canvas.drawLines(points, wavePaint);
        //canvas.drawPath(path, wavePaint);


        /*ByteBuffer buffer = ByteBuffer.allocateDirect(rect.width()*4);
        track.get(buffer, (int) offset*samplesPerPixel, rect.width()/samplesPerPixel);

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
        for (int i = 0; i < floatBuffer.length - 6; i+=6)// skip second channel
        {
            float x1 = rect.width() * i / (floatBuffer.length - 1);
            float y1 = rect.height() / 2 + floatBuffer[i] * (rect.height() / 2);

            float x2 = rect.width() * (i + 1) / (floatBuffer.length - 1);
            float y2 = rect.height() / 2 + floatBuffer[i + 2] * (rect.height() / 2);

            float x3 = rect.width() * (i + 2) / (floatBuffer.length - 1);
            float y3 = rect.height() / 2 + floatBuffer[i + 4] * (rect.height() / 2);

            points[i * 4] = x1;
            points[i * 4 + 1] = y1 ;
            points[i * 4 + 2] = x2;
            points[i * 4 + 3] = y2;

           // canvas.drawLine(x1, y1, x2, y2, wavePaint);

            path.cubicTo(x1, y1, x2, y2, x3, y3);
           // path.lineTo(x2, y2);
        }
       // canvas.drawLines(points, wavePaint);
        canvas.drawPath(path, wavePaint);*/
    /*}*/

    public WaveTrackViewListener getListener()
    {
        return listener;
    }

    public void setListener(WaveTrackViewListener listener)
    {
        this.listener = listener;
    }

    public long getOffset()
    {
        return offset;
    }

    public void setOffset(long offset)
    {
        this.offset = offset;
        demandFullUpdate();
    }

    public boolean isSuspended()
    {
        return isSuspended;
    }

    public void setSuspended(boolean suspended)
    {
        isSuspended = suspended;
    }

    public MainAreaFragment.SampleRange getSelection()
    {
        return selection;
    }

    public void setSelection(MainAreaFragment.SampleRange selection)
    {
        this.selection = selection;
        demandDrawUpdate();
    }

    public Queue<Boolean> getUpdateQueue()
    {
        return updateQueue;
    }

    // -------------------------------------------------------Drawing stuff-----------------------------------------------

    public void startDrawing()
    {
        isSuspended = false;
      /*  stopDrawing();

        drawThread = new CDrawThread(getContext());
        drawThread.setName("" + System.currentTimeMillis());
        drawThread.start();*/
        //drawThread.setPriority(Thread.MAX_PRIORITY);
    }

    public void stopDrawing()
    {
        isSuspended = true;
        /*if(drawThread == null) return;

        boolean retry = true;
        drawThread.setRunning(false);
        while (retry)
        {
            try
            {
                drawThread.join();
                retry = false;
            }
            catch (InterruptedException e) {}
        }*/
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height)
    {
        rect.set(0, 0, width, height);
        startDrawing();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height)
    {
        rect.set(0, 0, width, height);
        demandFullUpdate();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface)
    {
        stopDrawing();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface){}

    public void drawSelection(Canvas canvas, float width, float height)
    {
        if(selection != null)
        {
            long startPos = selection.startingSample - offset;

            long endPos = 1;

            if(selection.endSample > selection.startingSample)
                endPos = startPos + selection.getLen();

            canvas.drawRect(startPos / samplesPerPixel, 0, endPos/ samplesPerPixel, height, selectionPaint);
        }
    }

    /**
     * draw waveform
     */
    public void doDraw(Canvas canvas)
    {
        canvas.drawColor(backgroundColor);

        int frames = rect.width();

        /*if(isDirty) // data needs to be updated
        {
            updateData();
            isDirty = false;
        }
        else */
        // not enough data!!!
        if(data == null || data.max == null || data.max.length < frames)
        {
            updateData();
        }

        float width = frames;
        float height = rect.height();
        float centerY = height / 2;

        // update selection
        drawSelection(canvas, width, height);

        boolean showSamples = data.individualSamples;

        for(int i = 0; i < frames-1; i++)
        {
            if(showSamples) // read data sequentially
            {
                x1 = i ;
                y1 = centerY + data.min[i] * (centerY);

                x2 = i ;
                y2 = centerY + data.min[i+1] * (centerY);
            }
            else // read min/max
            {
                // here we calculate symmetrical lines
                x1 = i ;
                y1 = centerY - data.min[i] * (centerY);

                x2 = i ;
                y2 = centerY + data.min[i] * (centerY);
            }

            if(y1 == y2)
                y2 += 1;

            canvas.drawLine(x1, y1, x2, y2, wavePaint);

            // canvas.drawLine(x1, y1, x2, y2, wavePaint);
            //path.quadTo(x1, y1, x2, y2);
        }
        //canvas.drawPoints(points, wavePaint);
        //canvas.drawLines(points, wavePaint);
    }

    public void updateData()
    {
        //int normalizedOffset = (int) (offset * samplesPerPixel /*/** zoomFactor*/);
        int frames = rect.width()+2;
        data = new WaveData(frames);

        Clip startingClip = track.getClipAtSample((int) (offset));

        if(startingClip == null)
        {
            for (int i = 0; i < frames; i++) // fill with silence
            {
                data.max[i] = 0 ;
                data.min[i] = 0 ;
            }
            return;
        }

        long startSample = offset;
        //long len = (long) (samplesPerPixel*rect.width());

        try
        {
            startingClip.getWaveData(startSample, frames, samplesPerPixel, data);
        }
        catch (IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        //Log.e(track.getName(), "Samples per frame: " + samplesPerPixel + ", offset: " + offset);
    }


    public interface WaveTrackViewListener
    {
        void touchStart(float x);
        void touchMove(float x);
        void touchEnd();
        void draw();
        void zoomIn();
        void zoomOut();
    }
}


