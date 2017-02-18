package com.sunflower.catchtherainbow.Views.Editing;

import android.graphics.Color;

import com.sunflower.catchtherainbow.Views.Editing.Waveform.Segment;
import com.sunflower.catchtherainbow.Views.Editing.Waveform.WaveformFragment;

import java.util.Arrays;
import java.util.List;

/**
 * Created by SuperComputer on 2/16/2017.
 */

public class SuperWaveformFragment extends WaveformFragment
{

    private String path = "";
    public void setFilePath(String path)
    {
        this.path = path;
    }


    /**
     * Provide path to your audio file.
     *
     * @return
     */
    @Override
    protected String getFileName()
    {
        return path;
    }

    /**
     * Optional - provide list of segments (start and stop values in seconds) and their corresponding colors
     *
     * @return
     */
    @Override
    protected List<Segment> getSegments()
    {
        return Arrays.asList(
                new Segment(55.2, 55.8, Color.rgb(238, 23, 104)),
                new Segment(56.2, 56.6, Color.rgb(238, 23, 104)),
                new Segment(58.4, 59.9, Color.rgb(184, 92, 184)));
    }
}