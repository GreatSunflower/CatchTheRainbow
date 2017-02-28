package com.sunflower.catchtherainbow.AudioClasses;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.widget.Toast;

import com.un4seen.bass.BASS;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by SuperComputer on 2/19/2017.
 */

public class SuperAudioPlayer
{
    private int channel;

    private Activity context;

    private boolean isPlaying = false;

    private int sampleRate = 44100;
    private int bufferSize = 512;

    public SuperAudioPlayer(Activity context)
    {
        this.context = context;

        // !!!-----Load plugins-----!!!
        ApplicationInfo info = context.getApplicationInfo();
        if(info != null)
        {
            String path = info.nativeLibraryDir;
            String[] list = new File(path).list();
            for (String s: list)
            {
                BASS.BASS_PluginLoad(path+"/"+s, 0);
            }
        }
        // plugins end

       // BASS.BASS_Init(-1, sampleRate, 0);
      //  BASS.BASS_SetConfig(BASS.BASS_CONFIG_FLOATDSP, 32);
        channel = 0;
    }

    private final BASS.SYNCPROC EndSync=new BASS.SYNCPROC()
    {
        public void SYNCPROC(int handle, int channel, int data, Object user)
        {
            isPlaying = false;
            for(AudioPlayerListener listener: audioPlayerListeners)
            {
                listener.onCompleted();
            }
        }
    };

    public void playPause(boolean play)
    {
        if(play)
        {
            BASS.BASS_ChannelPlay(channel, false);
            isPlaying = true;
        }
        else
        {
            BASS.BASS_ChannelPause(channel);
            isPlaying = false;
        }
    }

    public void stop()
    {
        BASS.BASS_ChannelStop(channel);
        isPlaying = false;
    }

    public void open(String path, boolean autoPlay)
    {
        BASS.BASS_ChannelStop(channel);

        //Free memory
        BASS.BASS_StreamFree(channel);

        channel = BASS.BASS_StreamCreateFile(path, 0, 0, 0);

        long bytes = BASS.BASS_ChannelGetLength(channel, BASS.BASS_POS_BYTE);
        int totalTime = (int)BASS.BASS_ChannelBytes2Seconds(channel, bytes);

        for(AudioPlayerListener listener: audioPlayerListeners)
        {
            listener.onInitialized(totalTime);
        }

        //Set callback
        BASS.BASS_ChannelSetSync(channel, BASS.BASS_SYNC_END, 0, EndSync, 0);

        if(autoPlay)
            playPause(true);
    }

    public void setTempo(double value)
    {

    }
    public void disposePlayer()
    {
        BASS.BASS_ChannelStop(channel);

        BASS.BASS_MusicFree(channel);

        //Free memory
        BASS.BASS_StreamFree(channel);

        channel = 0;
    }
    public int getProgress()
    {
        double position = BASS.BASS_ChannelBytes2Seconds(channel, BASS.BASS_ChannelGetPosition(channel, BASS.BASS_POS_BYTE));
        return (int) position;
    }

    public int getDuration()
    {
        long len = BASS.BASS_ChannelGetLength(channel, BASS.BASS_POS_BYTE);

        double time = BASS.BASS_ChannelBytes2Seconds(channel, len);
        return (int) time;
    }

    public void setPosition(int position)
    {
       // long len = BASS.BASS_ChannelGetLength(channel, BASS.BASS_POS_BYTE);
      //  long bytesPosition = len * position;

        BASS.BASS_ChannelSetPosition(channel, BASS.BASS_ChannelSeconds2Bytes(channel, position), BASS.BASS_POS_BYTE);
    }

    public void setVolume(int volume)
    {
        BASS.BASS_SetConfig(BASS.BASS_CONFIG_GVOL_STREAM, volume * 10000);
    }

    public int getVolume()
    {
       return BASS.BASS_GetConfig(BASS.BASS_CONFIG_GVOL_STREAM) / 10000;
    }

    public boolean isPlaying()
    {
        long isPlaying = BASS.BASS_ChannelIsActive(channel);
        return isPlaying == BASS.BASS_ACTIVE_PLAYING;
    }

    public int getPercentage()
    {
        if(getDuration() == 0)
            return 0;
        return (100 * getProgress()) / getDuration();
    }



    public void onAudioLoaded(final int duration)
    {
        context.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(context, "Audio Loaded: " + duration +  " ms!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onLoadError()
    {
        context.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(context, "Audio could not be loaded!", Toast.LENGTH_SHORT).show();
                playNext();
            }
        });
    }


    // temp
    private ArrayList<AudioFile> audioFiles = new ArrayList<>();
    private AudioFile currentFile;

    public void setAudioFiles(ArrayList<AudioFile> audioFiles)
    {
        currentFile = null;

        this.audioFiles = audioFiles;
    }

    public void play()
    {
        if(audioFiles.size() == 0) return;

        // check whether something is playing or not
        if(currentFile == null)
        {
            currentFile = audioFiles.get(0);
        }

        try
        {
            open(currentFile.getPath(), true);
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public void playNext()
    {
        playByIndex(audioFiles.indexOf(currentFile) + 1);
    }

    public void playPrev()
    {
        playByIndex(audioFiles.indexOf(currentFile) - 1);
    }

    public void playByIndex(int desiredIndex)
    {
        if(audioFiles.size() == 0) return;

        if(currentFile == null)
        {
            currentFile = audioFiles.get(0);
        }
        else
        {
            int oldIndex = audioFiles.indexOf(currentFile);

            int newIndex;
            // circular travel through the array
            if(desiredIndex < oldIndex) newIndex = (desiredIndex + audioFiles.size()) %  audioFiles.size();
            else newIndex = desiredIndex %  audioFiles.size();

            currentFile = audioFiles.get(newIndex);
        }
        play();
    }

    public void playByDirectIndex(int desiredIndex)
    {
        if(audioFiles.size() == 0) return;

        currentFile = audioFiles.get(desiredIndex);

        play();
    }

    public ArrayList<AudioFile> getAudioFiles()
    {
        return audioFiles;
    }


    // Player Listeners
    private ArrayList<AudioPlayerListener> audioPlayerListeners = new ArrayList<>();

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

    public int getChannel()
    {
        return channel;
    }

    public interface AudioPlayerListener
    {
        void onInitialized(int totalTime);
        void onCompleted();
    }

}
