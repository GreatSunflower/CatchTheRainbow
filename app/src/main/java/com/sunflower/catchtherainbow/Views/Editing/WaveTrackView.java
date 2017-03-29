package com.sunflower.catchtherainbow.Views.Editing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.sunflower.catchtherainbow.AudioClasses.Clip;
import com.sunflower.catchtherainbow.AudioClasses.WaveData;
import com.sunflower.catchtherainbow.AudioClasses.WaveTrack;
import com.sunflower.catchtherainbow.R;

/**
 * Created by SuperComputer on 3/25/2017.
 *
 */

// Draws clips of the track
public class WaveTrackView extends SurfaceView implements SurfaceHolder.Callback
{
    public static final int MAX_SAMPLES_PER_PIXEL = 393216/108;

    protected int samplesPerPixel = 1;
    protected int zoomFactor = 2;

    protected WaveTrack track;

    protected Paint wavePaint = new Paint();

    private Rect rect = new Rect();

    private CDrawThread drawThread;
    private SurfaceHolder holder;

    private Boolean isCreated = false;

    protected GestureDetector gestureDetector;
    protected ScaleGestureDetector scaleGestureDetector;
    protected float initialScaleSpan;
    protected WaveData data;

    protected float offset = 0;

    protected WaveTrackViewListener listener;

    // should be created from code
    public WaveTrackView(Context context, WaveTrack track, int samplesPerPixel)
    {
        super(context);

        this.track = track;
        this.samplesPerPixel = samplesPerPixel;

        wavePaint.setStrokeWidth(1.4f);
        wavePaint.setColor(Color.WHITE);
        wavePaint.setStyle(Paint.Style.STROKE);
        wavePaint.setAntiAlias(true);
        //wavePaint.setDither(true);
        wavePaint.setStrokeCap(Paint.Cap.ROUND);
        wavePaint.setStrokeJoin(Paint.Join.ROUND);
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

        holder = getHolder();
        holder.addCallback(this);
        drawThread = new CDrawThread(holder, context);
        drawThread.setName("" + System.currentTimeMillis());
        drawThread.setPriority(Thread.MAX_PRIORITY);
        setFocusable(true);

        holder.setFormat(PixelFormat.TRANSLUCENT);

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

    public void demandUpdate()
    {
        drawThread.demandUpdate();
    }

    public void zoomIn()
    {
        if(samplesPerPixel-zoomFactor < 1) return;

        samplesPerPixel -= zoomFactor;
        drawThread.demandUpdate();
    }
    public void zoomOut()
    {
        if(samplesPerPixel + zoomFactor > MAX_SAMPLES_PER_PIXEL) return;

        samplesPerPixel += zoomFactor;

        drawThread.demandUpdate();
    }

    public void setSamplesPerPixel(int samplesPerPixel)
    {
        if(samplesPerPixel < 1 || samplesPerPixel > WaveTrackView.MAX_SAMPLES_PER_PIXEL)
            return;

        this.samplesPerPixel = samplesPerPixel;

        drawThread.demandUpdate();
    }

    float []points;
    Path path = new Path();

    Paint textPaint = new Paint();
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

    public float getOffset()
    {
        return offset;
    }

    public void setOffset(float offset)
    {
        this.offset = offset;
        drawThread.updateData();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        if(!drawThread.isRunning)
            drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        rect.set(0, 0, width, height);
        //drawThread.setSurfaceSize(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
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
        }
    }

    class CDrawThread extends Thread
    {
        private Paint mBackPaint;
        private Bitmap mBackgroundImage;
        private int mCanvasHeight = 1;
        private int mCanvasWidth = 1;
        private Paint mLinePaint;
        private final SurfaceHolder mSurfaceHolder;
        private boolean isDirty = false;

        private boolean isRunning = false;

        /**
         * Instanciate the Thread
         * All the parameters i handled by the cDrawer class
         * @param paramContext
         * @param paramHandler
         */
        public CDrawThread(SurfaceHolder paramContext, Context paramHandler)
        {
            mSurfaceHolder = paramContext;
            mLinePaint = new Paint();
            mLinePaint.setAntiAlias(true);
            mLinePaint.setARGB(255, 255, 0, 0);
            mLinePaint = new Paint();
            mLinePaint.setAntiAlias(true);
            mLinePaint.setARGB(255, 255, 0, 255);
            mBackPaint = new Paint();
            mBackPaint.setAntiAlias(true);
            mBackPaint.setARGB(255, 0, 0, 0);
            mBackgroundImage = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

        }

        public void demandUpdate()
        {
            isDirty = true;
        }

        /**
         * Calculate and draws the line
         * @param canvas to draw on, handled by cDrawer class
         */
        public void doDraw(Canvas canvas)
        {
       /* textPaint.setColor(Color.RED);
        textPaint.setFakeBoldText(true);
        textPaint.setTextSize(rect.height()/3f);
        //textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Samples: " + samplesPerPixel, 15, rect.height()/2, textPaint);*/
            //path.reset();

            /*Clip startingClip = track.getClipAtSample((int) (offset*samplesPerPixel));
            if(startingClip == null) return;

            WaveData data = new WaveData(frames);

            // if there are no points or they are less than buffer size
            if (points == null || points.length < frames * 4)
            {
                points = new float[frames * 4];
            }

            int startSample = (int) (offset*samplesPerPixel);
            int len = rect.width()*samplesPerPixel;
            startingClip.getWaveData(startSample, len, samplesPerPixel, data);*/
            canvas.drawColor(getContext().getResources().getColor(R.color.colorPrimary));

            int frames = rect.width();

            if(data.max == null || data.max.length < frames) return;

            // canvas.drawBitmap(mBackgroundImage, 0, 0, mBackPaint);

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

               // canvas.drawLine(x1, y1, x2, y2, wavePaint);
                //path.quadTo(x1, y1, x2, y2);
            }
            canvas.drawLines(points, wavePaint);
        }

        //Frame speed
        long timeNow;
        long timePrev = 0;
        long timePrevFrame = 0;
        long timeDelta;

        /**
         * Updated the Surface and redraws the new audio-data
         */
        public void run()
        {
            isRunning = true;
            updateData();

            while (isRunning)
            {
                //limit frame rate to max 60fps
                timeNow = System.currentTimeMillis();
                timeDelta = timeNow - timePrevFrame;
                if ( timeDelta < 16)
                {
                    try
                    {
                        Thread.sleep(16 - timeDelta);
                    }
                    catch(InterruptedException e) {

                    }
                }
                timePrevFrame = System.currentTimeMillis();

                Canvas localCanvas = null;
                try
                {
                    localCanvas = mSurfaceHolder.lockCanvas(null);
                    synchronized (mSurfaceHolder)
                    {
                        int frames = rect.width();

                        if(isDirty) // data needs to be updated
                        {
                            updateData();
                            isDirty = false;
                        }
                        else if(frames != data.max.length)
                        {
                            updateData();
                        }

                        if (localCanvas != null)
                            doDraw(localCanvas);
                    }
                }
                finally
                {
                    if (localCanvas != null)
                        mSurfaceHolder.unlockCanvasAndPost(localCanvas);
                }
            }
        }

        public void updateData()
        {
            int normalizedOffset = (int) (offset * samplesPerPixel /*/** zoomFactor*/);

            Clip startingClip = track.getClipAtSample((int) (normalizedOffset));
            if(startingClip == null) return;

            int frames = rect.width();
            data = new WaveData(frames);

            // if there are no points or they are less than buffer size
            if (points == null || points.length < frames * 4)
            {
                points = new float[frames * 4];
            }

            long startSample = (long) (normalizedOffset);
            //long len = (long) (samplesPerPixel*rect.width());

            startingClip.getWaveData(startSample, rect.width(), samplesPerPixel, data);
            Log.e("Wave", "samples per frame: " + samplesPerPixel + ", offset: " + normalizedOffset);
        }
        public boolean isRunning()
        {
            return isRunning;
        }

        public void setRunning(boolean running)
        {
            isRunning = running;
        }

        public void setSurfaceSize(int paramInt1, int paramInt2)
        {
            //synchronized (mSurfaceHolder)
            {
                mCanvasWidth = paramInt1;
                mCanvasHeight = paramInt2;
                //mBackgroundImage = Bitmap.createScaledBitmap(mBackgroundImage, paramInt1, paramInt2, true);
            }
        }
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
