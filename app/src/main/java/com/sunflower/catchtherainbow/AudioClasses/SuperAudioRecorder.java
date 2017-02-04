package com.sunflower.catchtherainbow.AudioClasses;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.SilenceDetector;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.writer.WriterProcessor;

/**
 * Created by SuperComputer on 2/3/2017.
 */

public class SuperAudioRecorder implements AudioProcessor
{
    private ArrayList<RecorderListener> recorderListeners = new ArrayList<>();
    private RecorderState state = RecorderState.NOT_INITIALIZED;
    private RandomAccessFile outputFile;

    AudioDispatcher dispatcher;

    int sampleRate = 22050;

    private double currentTime = 0;

    private SilenceDetector silenceDetector;

    public SuperAudioRecorder()
    {

    }

    public void load(File file)
    {
        if(getState() != RecorderState.NOT_INITIALIZED)
        {
            eject();
        }

        try
        {
            outputFile = new RandomAccessFile(file.getAbsolutePath(), "rw");
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return;
        }

        currentTime = 0;
        setState(RecorderState.INITIALIZED);

        for (RecorderListener listener : recorderListeners)
            listener.OnInitialized();
    }

    public void eject()
    {
        outputFile = null;
        stopRecording();
        setState(RecorderState.NOT_INITIALIZED);
    }


    double silenceThreshold = -30;
    public void startRecording()
    {
        if(state == RecorderState.NOT_INITIALIZED)
        {
            throw new IllegalStateException("Can not play when no file is loaded");
        }
        else if(state == RecorderState.PAUSED)
        {

        }
        else
        {
            if(state == RecorderState.RECORDING) stopRecording();

            dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, 1024, 0);
            // initialize the processors
            silenceDetector = new SilenceDetector(silenceThreshold, false);
            WriterProcessor writerProcessor = new WriterProcessor(dispatcher.getFormat(), outputFile);

            // add them to the dispatcher
            dispatcher.addAudioProcessor(silenceDetector);
            dispatcher.addAudioProcessor(writerProcessor);
            dispatcher.addAudioProcessor(this);

            new Thread(dispatcher, "Audio Recorder Thread").start();
            state = RecorderState.RECORDING;
        }
    }

    public void stopRecording()
    {
        if(state == RecorderState.RECORDING || state == RecorderState.PAUSED)
        {
            dispatcher.stop();
            setState(RecorderState.STOPPED);
        }
        else if(state != RecorderState.STOPPED)
        {
            throw new IllegalStateException("Can not stopRecording when nothing is being recorded!");
        }
    }

    @Override
    public boolean process(AudioEvent audioEvent)
    {
        currentTime = audioEvent.getTimeStamp();
        for (RecorderListener listener : recorderListeners)
            listener.OnUpdate(audioEvent);

        return true;
    }

    @Override
    public void processingFinished()
    {
        if(state != RecorderState.STOPPED)
        {
            state = RecorderState.STOPPED;

            for (RecorderListener listener : recorderListeners)
                listener.OnFinish();
        }
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

    public RecorderState getState()
    {
        return state;
    }

    public void setState(RecorderState state)
    {
        this.state = state;
    }


    public enum RecorderState
    {
        /**
         * No file or stream is found.
         */
        NOT_INITIALIZED,
        /**
         * a file or stream is found.
         */
        INITIALIZED,
        /**
         * The file is playing
         */
        RECORDING,
        /**
         * Audio play back is paused.
         */
        PAUSED,
        /**
         * Audio play back is stopped.
         */
        STOPPED
    }

    interface RecorderListener
    {
        void OnInitialized();
        void OnUpdate(AudioEvent audioEvent);
        void OnFinish();
    }

    public ArrayList<RecorderListener> getPlayerListeners()
    {
        return recorderListeners;
    }

    public void addPlayerListener(RecorderListener playerListener)
    {
        recorderListeners.add(playerListener);
    }

}
