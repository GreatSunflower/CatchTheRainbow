package com.sunflower.catchtherainbow.AudioClasses;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by Alexandr on 08.02.2017.
 */

public class Folder
{
    private String title;
    private String path;
    private String size;
    private int countSong;

    public Folder(String title, String path, String size, int countSong)
    {
        this.title = title;
        this.path = path;
        this.size = size;
        this.countSong = countSong;
    }

    public String getTitle()
    {
        return title;
    }
    public String getPath()
    {
        return path;
    }
    public String getSize()
    {
        return size;
    }
    public int getCountSong()
    {
        return countSong;
    }

    public static int getCountFiles(File dir)
    {
        if(dir == null) return 0;
        int count = 0;
        try
        {
            for (File file : dir.listFiles())
            {
                if (file.isFile())
                {
                    count++;
                }
            }
        }
        catch (Exception ex){ }
        return count;
    }

    public static long getFolderSize(File dir)
    {
        long size = 0;
        for (File file : dir.listFiles())
        {
            if (file.isFile())
            {
                size += file.length();
            }
            /*else
                size += getFolderSize(file);*/
        }
        return size;
    }

    public static String getReadableSize(long size)
    {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups))
                + " " + units[digitGroups];
    }
}