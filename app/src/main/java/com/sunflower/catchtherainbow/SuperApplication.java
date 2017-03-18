package com.sunflower.catchtherainbow;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.un4seen.bass.BASS;

/**
 * Created by SuperComputer on 3/6/2017.
 */

public class SuperApplication extends Application
{
    private static String appDirectory = Environment.getExternalStorageDirectory().toString() + "/Catch The Rainbow";

    public static String getAppDirectory()
    {
        return appDirectory;
    }

    public SuperApplication()
    {
        super();

        // initialize default output device
        if (!BASS.BASS_Init(-1, 44100, 0))
        {
            Log.e("App", "Can't initialize device");
            return;
        }
        BASS.BASS_SetConfig(BASS.BASS_CONFIG_FLOATDSP, 32);
    }
}
