package com.sunflower.catchtherainbow.AudioClasses;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTimestamp;
import android.media.AudioTrack;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.sunflower.catchtherainbow.Helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioGenerator;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.DetermineDurationProcessor;
import be.tarsos.dsp.io.PipeDecoder;
import be.tarsos.dsp.io.PipedAudioStream;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.TarsosDSPAudioInputStream;
import be.tarsos.dsp.io.UniversalAudioInputStream;
import be.tarsos.dsp.io.android.AndroidAudioInputStream;
import be.tarsos.dsp.io.android.AndroidAudioPlayer;
import be.tarsos.dsp.io.android.AndroidFFMPEGLocator;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.util.AudioResourceUtils;

/**
 * Created by SuperComputer on 1/31/2017.
 */

public class SuperAudioPlayer implements AudioProcessor
{
    protected Context context;

    private ArrayList<AudioPlayerListener> playerListeners = new ArrayList<>();
    private PlayerState state = PlayerState.NO_FILE_LOADED;
    private File loadedFile;

    private AndroidAudioPlayer audioPlayer;

    private double durationInSeconds;
    private double currentTime;
    private double pausedAt;

    AudioDispatcher dispatcher;

    public SuperAudioPlayer(Context context/*, TarsosDSPAudioFormat audioFormat, int bufferSizeInSamples, int streamType*/)
    {
        //super(audioFormat, bufferSizeInSamples, streamType);
        this.context = context;
        state = PlayerState.NO_FILE_LOADED;
    }

    int sampleRate = 22050;
    public void load(File file) throws Exception
    {
        if(file == null) throw new NullPointerException("File can't be null !");

        if(state != PlayerState.NO_FILE_LOADED)
        {
            eject();
        }
        loadedFile = file;

        if(file.exists() == false) throw new Exception("File does not exist!");

        /*AudioDispatcher audioDispatcher = AudioDispatcherFactory.fromPipe(file.getAbsolutePath(), sampleRate, 2048, 0);
        DetermineDurationProcessor ddp = new DetermineDurationProcessor();
        audioDispatcher.addAudioProcessor(ddp);
        audioDispatcher.run();*/
        //audioDispatcher.getFormat().properties()

        durationInSeconds = new PipeDecoder().getDuration(AudioResourceUtils.sanitizeResource(file.getAbsolutePath()));// ddp.getDurationInSeconds();
        Log.d("aaa", durationInSeconds+"");

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(file.getAbsolutePath());
        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        mmr.release();

        durationInSeconds = Helper.millisecondsToSeconds(Double.parseDouble(duration));
        Log.d("aaa", "New duration: " +  durationInSeconds+"");

        pausedAt = 0;
        currentTime = 0;
        setState(PlayerState.FILE_LOADED);

        for (AudioPlayerListener listener : playerListeners)
            listener.OnInitialized();
    }

    public void eject()
    {
        loadedFile = null;
        stop();
        setState(PlayerState.NO_FILE_LOADED);
    }

    public void play()
    {
        if(state == PlayerState.NO_FILE_LOADED)
        {
            throw new IllegalStateException("Cannot play when no file is loaded");
        }
        else if(state == PlayerState.PAUZED)
        {
            play(pausedAt);
        }
        else
        {
            play(0);
        }
    }

    public void play(double startTime)
    {
        if(state == PlayerState.NO_FILE_LOADED)
        {
            throw new IllegalStateException("Can not play when no file is loaded");
        }
        else
        {
            if(state == PlayerState.PLAYING) stop();

            dispatcher = AudioDispatcherFactory.fromPipe(loadedFile.getAbsolutePath(), sampleRate, 1024, 0);
            dispatcher.addAudioProcessor(new AndroidAudioPlayer(dispatcher.getFormat()));
            dispatcher.addAudioProcessor(this);
            dispatcher.skip(startTime);
            new Thread(dispatcher, "Audio Player Thread").start();
            state = PlayerState.PLAYING;
        }
    }

    public void stop()
    {
        if(state == PlayerState.PLAYING || state == PlayerState.PAUZED)
        {
            dispatcher.stop();
            state = PlayerState.STOPPED;
        }
        else if(state != PlayerState.STOPPED)
        {
            throw new IllegalStateException("Can not Stop Playing when nothing is playing. Crap(");
        }

    }

    @Override
    public boolean process(AudioEvent audioEvent)
    {
        currentTime = audioEvent.getTimeStamp();
        for(AudioPlayerListener listener: playerListeners)
            listener.OnUpdate(audioEvent);

        return true;
    }

    @Override
    public void processingFinished()
    {
        if(state != PlayerState.STOPPED)
        {
            state = PlayerState.STOPPED;
        }
        for (AudioPlayerListener listener : playerListeners)
            listener.OnFinish();
    }

    private void setState(PlayerState newState)
    {
        PlayerState oldState = state;
        state = newState;
    }

    public PlayerState getState() {

        return state;
    }


    public ArrayList<AudioPlayerListener> getPlayerListeners()
    {
        return playerListeners;
    }

    public void addPlayerListener(AudioPlayerListener playerListener)
    {
        playerListeners.add(playerListener);
    }

    public double getDurationInSeconds()
    {
        return durationInSeconds;
    }

    public String getDurationString()
    {
        Date date = new Date((long)(durationInSeconds*1000));

        String formattedDate = new SimpleDateFormat("mm:ss").format(date);
        return formattedDate;
    }

    public int getDurationInMilliseconds()
    {
        long millis = TimeUnit.SECONDS.toMillis((long) durationInSeconds);

        return (int)millis;
    }

    public int getCurrentTimeInMilliseconds()
    {
        long millis = TimeUnit.SECONDS.toMillis((long) currentTime);

        return (int)millis;
    }

    public double getCurrentTime()
    {
        return currentTime;
    }

    public String getCurrentTimeString()
    {
        Date date = new Date((long)(currentTime*1000));

        String formattedDate = new SimpleDateFormat("mm:ss").format(date);
        return formattedDate;
    }

    public double getPausedAt()
    {
        return pausedAt;
    }

    public interface AudioPlayerListener
    {
        void OnInitialized();
        void OnUpdate(AudioEvent audioEvent);
        void OnFinish();
    }

    /**
     * Defines the state of the audio player.
     * @author Joren Six
     */
    public enum PlayerState
    {
        /**
         * No file is loaded.
         */
        NO_FILE_LOADED,
        /**
         * A file is loaded and ready to be played.
         */
        FILE_LOADED,
        /**
         * The file is playing
         */
        PLAYING,
        /**
         * Audio play back is paused.
         */
        PAUZED,
        /**
         * Audio play back is stopped.
         */
        STOPPED
    }

}
