package com.sunflower.catchtherainbow.AudioClasses;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by SuperComputer on 2/5/2017.
 */

public class PlayListPlayer extends SuperAudioPlayer
{
    private ArrayList<AudioFile> audioFiles = new ArrayList<>();
    private AudioFile currentFile;

    public PlayListPlayer(Context context)
    {
        super(context);
    }

    public void setAudioFiles(ArrayList<AudioFile> audioFiles)
    {
        if(getState() == PlayerState.PLAYING) eject();
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
            super.load(new File(currentFile.getPath()));
            super.play();
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

    public void playBySong(AudioFile file)
    {
        int index = audioFiles.indexOf(file);
        if(index == -1) return;

        playByIndex(index);
    }

    private void playByIndex(int desiredIndex)
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

    public ArrayList<AudioFile> getAudioFiles()
    {
        return audioFiles;
    }
}
