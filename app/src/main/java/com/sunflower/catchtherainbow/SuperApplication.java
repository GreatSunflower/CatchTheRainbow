package com.sunflower.catchtherainbow;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.os.Environment;
import android.util.Log;

import com.un4seen.bass.BASS;

import java.io.File;

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

    @Override
    public void onCreate()
    {
        super.onCreate();

        // initialize default output device
        if (!BASS.BASS_Init(-1, 44100, 0))
        {
            Log.e("App", "Can't initialize device");
            return;
        }
        BASS.BASS_SetConfig(BASS.BASS_CONFIG_FLOATDSP, 32);

        // !!!-----Load plugins-----!!!
        ApplicationInfo info = this.getApplicationInfo();
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
        BASS.BASS_SetConfig(BASS.BASS_CONFIG_BUFFER, 1000);

        //BASS.BASS_SetConfig(BASS.BASS_CONFIG_UPDATETHREADS, project.getTracks().size());
        //BASS.BASS_SetConfig(BASS.BASS_CONFIG_UPDATEPERIOD, 20);
        //BASS.BASS_SetConfig(BASS.BASS_CONFIG_ASYNCFILE_BUFFER, 4096*2);/* */
        //BASS.BASS_SetConfig(BASSmix.BASS_CONFIG_MIXER_BUFFER, 5);*/
        //BASS.BASS_SetConfig(BASSmix.BASS_CONFIG_MIXER_POSEX, 5000);
    }
}
