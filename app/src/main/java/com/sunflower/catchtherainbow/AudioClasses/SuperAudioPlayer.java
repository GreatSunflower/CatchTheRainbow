package com.sunflower.catchtherainbow.AudioClasses;

import android.app.Activity;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.sunflower.catchtherainbow.Helper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.DetermineDurationProcessor;
import be.tarsos.dsp.io.PipedAudioStream;
import be.tarsos.dsp.io.TarsosDSPAudioInputStream;
import be.tarsos.dsp.io.android.AndroidAudioPlayer;


/**
 * Super audio player
 */

public class SuperAudioPlayer implements AudioProcessor
{
    private  String logTag = "Super Audio Player";

    protected Activity context;

    protected ArrayList<AudioPlayerListener> playerListeners = new ArrayList<>();
    protected PlayerState state = PlayerState.NO_FILE_LOADED;
    protected File loadedFile;

    protected AndroidAudioPlayer audioPlayer;
    protected DetermineDurationProcessor durationProc;

    protected double durationInSeconds;
    protected double currentTime;
    protected double pausedAt;

    AudioDispatcher dispatcher;

    int sampleRate = 44100;

    public SuperAudioPlayer(Activity context/*, TarsosDSPAudioFormat audioFormat, int bufferSizeInSamples, int streamType*/)
    {
        //super(audioFormat, bufferSizeInSamples, streamType);
        this.context = context;
        state = PlayerState.NO_FILE_LOADED;
    }

    public void load(File file) throws Exception
    {
        if(file == null) throw new NullPointerException("File can't be null !");

        if(state != PlayerState.NO_FILE_LOADED)
        {
            eject();
        }
        loadedFile = file;

        if(!file.exists()) throw new Exception("File does not exist!");

        /*AudioDispatcher audioDispatcher = AudioDispatcherFactory.fromPipe(file.getAbsolutePath(), sampleRate, 2048, 0);
        DetermineDurationProcessor ddp = new DetermineDurationProcessor();
        audioDispatcher.addAudioProcessor(ddp);
        audioDispatcher.run();*/

        // does not work at all (returns -1)
        //durationInSeconds = new PipeDecoder().getDuration(AudioResourceUtils.sanitizeResource(file.getAbsolutePath()));// ddp.getDurationInSeconds();
       // Log.d("aaa", durationInSeconds+"");

        // retrieves duration
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(file.getAbsolutePath());
        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        mmr.release();

        durationInSeconds =  Helper.millisecondsToSeconds(Double.parseDouble(duration));

        Log.d("aaa", "New duration: " +  durationInSeconds+"");

        pausedAt = 0;
        currentTime = 0;
        setState(PlayerState.FILE_LOADED);

        for (AudioPlayerListener listener : playerListeners)
            listener.OnInitialized(loadedFile);
    }

    // stops the dispatcher and unloads the file
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
            if (state == PlayerState.PLAYING) stop();

            pausedAt = startTime;

            // read data from a file
            PipedAudioStream file = new PipedAudioStream(loadedFile.getPath());
            TarsosDSPAudioInputStream stream = file.getMonoStream(sampleRate, startTime);

            // buffers size really maters!!! A lesser value will result in more frequent updated which is very useful(e.g. for visualization)
            dispatcher = new AudioDispatcher(stream, 4096, 2048); //AudioDispatcherFactory.fromPipe(loadedFile.getAbsolutePath(), sampleRate, 4096, 2048);
            //dispatcher.skip(startTime);

            // initialize the processors
            audioPlayer = new AndroidAudioPlayer(dispatcher.getFormat());
            durationProc = new DetermineDurationProcessor();

            // add them to the dispatcher
            dispatcher.addAudioProcessor(durationProc);
            dispatcher.addAudioProcessor(this);
            dispatcher.addAudioProcessor(audioPlayer);

            // start the dispatcher in a new thread
            Thread th = new Thread(dispatcher, "Audio Player Thread");
            th.setUncaughtExceptionHandler(dispatcherExceptionHandler);
            th.start();
            state = PlayerState.PLAYING;

        }
    }

    public void pause()
    {
        pause(currentTime);
    }

    public void pause(double pauseAt)
    {
        if(state == PlayerState.PLAYING || state == PlayerState.PAUZED)
        {
            // TODO: Clean this up
            if(dispatcher != null)
            {
                dispatcher.removeAudioProcessor(this);
                dispatcher.removeAudioProcessor(audioPlayer);
                dispatcher.stop();

                dispatcher = null;
            }
            setState(PlayerState.PAUZED);
            pausedAt = pauseAt;
        }
        else
        {
            throw new IllegalStateException("Can not pause when nothing is playing");
        }
    }

    public void stop()
    {
        if(state == PlayerState.PLAYING || state == PlayerState.PAUZED)
        {
            // TODO: Clean this up
            dispatcher.removeAudioProcessor(this);
            dispatcher.removeAudioProcessor(audioPlayer);
            dispatcher = null;
            state = PlayerState.STOPPED;
        }
        else if(state != PlayerState.STOPPED)
        {
            //throw new IllegalStateException("Can not Stop Playing when nothing is playing. Crap(");
            return;
        }

    }

    @Override
    public boolean process(final AudioEvent audioEvent)
    {
        // TODO: Find a better way to represent paused value
        // update time
        currentTime = audioEvent.getTimeStamp() + pausedAt;

        // notify all of the subscribers about update
        context.runOnUiThread(new Runnable()
        {
            public void run()
            {
                for(AudioPlayerListener listener: playerListeners)
                    listener.OnUpdate(audioEvent, currentTime);
            }
        });

        return true;
    }

    @Override
    public void processingFinished()
    {
        if(state != PlayerState.STOPPED)
        {
            state = PlayerState.STOPPED;
        }

        // notify all of the subscribers about finish
        context.runOnUiThread(new Runnable()
        {
            public void run()
            {
                for (AudioPlayerListener listener : playerListeners)
                    listener.OnFinish();
            }
        });

    }

    // handles dispatcher thread exceptions.
    // They often occur when it's frequently being stopped and some processors are not destroyed.
    Thread.UncaughtExceptionHandler dispatcherExceptionHandler = new Thread.UncaughtExceptionHandler()
    {
        @Override
        public void uncaughtException(Thread thread, Throwable throwable)
        {
            Log.e(logTag, throwable.getLocalizedMessage());
        }
    };

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
        long millis = TimeUnit.SECONDS.toMillis((long) getCurrentTime());

        return (int)millis;
    }

    public void setCurrentTime(double currentTime, boolean updatePlayback)
    {
        if(state == PlayerState.FILE_LOADED)
        {
            return;
        }

        if(state == PlayerState.PAUZED)
            pause(currentTime);
        else if(state == PlayerState.PLAYING && updatePlayback)
            play(currentTime);

    }

    public double getCurrentTime()
    {
        return currentTime;
    }

    public String getCurrentTimeString()
    {
        Date date = new Date((long)(getCurrentTime() *1000));

        String formattedDate = new SimpleDateFormat("mm:ss").format(date);
        return formattedDate;
    }

    public double getPausedAt()
    {
        return pausedAt;
    }

    public interface AudioPlayerListener
    {
        void OnInitialized(File file);
        void OnUpdate(AudioEvent audioEvent, double realCurrentTime);
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
