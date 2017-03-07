package com.sunflower.catchtherainbow;

import android.app.Application;
import android.os.Environment;

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
}
