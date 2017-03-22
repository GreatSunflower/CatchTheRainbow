package com.sunflower.catchtherainbow.AudioClasses;

import java.io.Serializable;

/**
 * Created by SuperComputer on 3/21/2017.
 * Manages audio chunks.
 */

public enum SampleFormat implements Serializable
{
    int_8Bit(1),
    int_16Bit(2),
    float_32Bit(4);

    private int sampleSize;

    SampleFormat(int sampleSize) { this.sampleSize = sampleSize; }

    public int getSampleSize()
    {
        return sampleSize;
    }
}