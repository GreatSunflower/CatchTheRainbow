package com.sunflower.catchtherainbow.AudioClasses;

/**
 * Created by SuperComputer on 3/28/2017.
 */

public class WaveData
{
    public float []min;
    public float []max;

    public WaveData()
    {
    }
    public WaveData(int size)
    {
        min = new float[size];
        max = new float[size];
    }
}
