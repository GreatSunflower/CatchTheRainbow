package com.sunflower.catchtherainbow.AudioClasses;

import android.app.Activity;
import android.content.Context;

import com.un4seen.bass.BASS;

import java.util.ArrayList;

/**
 * Created by SuperComputer on 3/20/2017.
 * Manages audio chunks.
 */

public abstract class BasePlayer
{
    Activity context;
    public BasePlayer(Activity context)
    {
        this.context = context;
    }

    public boolean isPlaying()
    {
        return false;
    }

    public boolean isInitialized()
    {
        return false;
    }

    public double getPercentage()
    {
        if(getDuration() == 0)
            return 0;
        return (100 * getProgress()) / getDuration();
    }

    public double getProgress(){ return 0.0; }

    public double getDuration(){ return 0.0; }

    public void initialize(boolean autoPlay){}

    public void play(){}

    public void pause(){}

    public void stop(){}

    public void setPosition(double time){}

    public void eject(){ }

    // Player Listeners
    protected ArrayList<AudioPlayerListener> audioPlayerListeners = new ArrayList<>();

    public void addPlayerListener(AudioPlayerListener listener)
    {
        audioPlayerListeners.add(listener);
    }
    public void removePayerListener(AudioPlayerListener listener)
    {
        audioPlayerListeners.remove(listener);
    }

    public ArrayList<AudioPlayerListener> getAudioPlayerListeners()
    {
        return audioPlayerListeners;
    }

    public interface AudioPlayerListener
    {
        void onInitialized(float totalTime);
        void onPlay();
        void onPause();
        void onStop();
        void onCompleted();
    }

    enum PlayerState
    {
        NotInitialized,
        Playing,
        Paused,
        Stopped
    }
}
