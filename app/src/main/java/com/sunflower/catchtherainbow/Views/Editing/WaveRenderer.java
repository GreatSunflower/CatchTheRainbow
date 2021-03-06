package com.sunflower.catchtherainbow.Views.Editing;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by SuperComputer on 4/2/2017.
 */

// Thread which performs drawing
class WaveRenderer extends Thread
{
    private Context context;

    private boolean isRunning = false;

    private ArrayList<WaveTrackView> tracks = new ArrayList<>();

    /**
     * Instantiate the Thread
     */
    public WaveRenderer(Context context)
    {
        this.context = context;
    }

    //Frame speed
    long timeNow;
    long timePrev = 0;
    long timePrevFrame = 0;
    long timeDelta;
    private static final int INTERVAL = 16;
    private final Object lock = new Object();

    /**
     * Updated the Surface and redraws the new audio-data
     */
    public void run()
    {
        isRunning = true;

        while (isRunning)
        {
            //---------------limit frame rate to max 60fps----------------------
            timeNow = System.currentTimeMillis();
            timeDelta = timeNow - timePrevFrame;
            if ( timeDelta < INTERVAL)
            {
                try
                {
                    Thread.sleep(INTERVAL - timeDelta);
                }
                catch(InterruptedException ex) { }
            }
            timePrevFrame = System.currentTimeMillis();

            // drawing and so on
            Canvas localCanvas = null;

            for(int i = 0; i < tracks.size(); i++)
            {
                WaveTrackView track = tracks.get(i);

                if(track.isSuspended()) continue;

                // if nothing is in update queue, leave it
                if(track.getUpdateQueue().isEmpty()) continue;

                try
                {
                    Boolean res = track.getUpdateQueue().poll();

                    if(res == null)
                    {
                        res = true;
                    }
                    else if(!res)
                    {
                        for(Boolean b: track.getUpdateQueue())
                        {
                            if(b)
                            {
                                res = true;
                                break;
                            }
                        }
                    }
                    track.getUpdateQueue().clear();

                    // needs to be updated
                    if(res)
                        track.updateData();

                    localCanvas = track.lockCanvas(null);
                    synchronized (lock)
                    {
                        if (localCanvas != null)
                            track.doDraw(localCanvas);
                    }
                }
                catch (Exception ex)
                {
                    Log.e("Renderer", ex.getMessage());
                }
                finally
                {
                    if (localCanvas != null)
                        track.unlockCanvasAndPost(localCanvas);
                }
            } // for
        } // while running
    }

    public void addTrack(WaveTrackView textureView)
    {
        if(textureView != null)
        {
            tracks.add(textureView);
            textureView.demandFullUpdate();
        }
    }

    public void removeTrack(WaveTrackView textureView)
    {
        if(textureView != null)
            tracks.remove(textureView);
    }

    public ArrayList<WaveTrackView> getTracks()
    {
        return tracks;
    }


    public boolean isRunning()
    {
        return isRunning;
    }

    public void setRunning(boolean running)
    {
        isRunning = running;
    }
} // Thread class
