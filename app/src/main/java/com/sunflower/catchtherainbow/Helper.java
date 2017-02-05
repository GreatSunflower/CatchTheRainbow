package com.sunflower.catchtherainbow;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by SuperComputer on 2/5/2017.
 */

//
public class Helper
{
    public static String secondToString(double seconds)
    {
        Date date = new Date((long)(seconds*1000));
        String formattedDate = new SimpleDateFormat("mm:ss").format(date);
        return formattedDate;
    }

    public static int secondsToMilliseconds(double currentTime)
    {
        long millis = TimeUnit.SECONDS.toMillis((long) currentTime);
        return (int)millis;
    }

    public static double millisecondsToSeconds(Number milliseconds)
    {
        return milliseconds.longValue() / 1000.d;
    }

    public static ArrayList<String> getAllSongsOnDevice(Context context)
    {
        ArrayList<String> songs = new ArrayList<>();
        Cursor cur = null;
        try
        {
            ContentResolver cr =  context.getContentResolver();

            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
            String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
            cur = cr.query(uri, null, selection, null, sortOrder);
            int count = 0;

            if (cur != null) {
                count = cur.getCount();

                if (count > 0)
                {
                    while (cur.moveToNext())
                    {
                        String fullPath = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));

                        songs.add(fullPath);
                    }
                } // count > 0
            }
        }
        catch (Exception ex)
        {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println(ex.getMessage());/* ex.printStackTrace();*/
        }
        finally
        {
            if (cur != null) cur.close();
        }

        return  songs;
    }

    public static ArrayList<String> getAllAudioOnDevice(Context context)
    {
        ArrayList<String> songs = new ArrayList<>();
        Cursor cursorMusic = null;
        try
        {
            ContentResolver cr =  context.getContentResolver();

            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
            String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
            String album = MediaStore.Audio.Albums._ID+ "=?";
            cursorMusic = cr.query(uri, null, selection, null, sortOrder);
            int count = 0;

            if (cursorMusic != null) {
                count = cursorMusic.getCount();

                if (count > 0)
                {
                    while (cursorMusic.moveToNext())
                    {
                        String fullPath = cursorMusic.getString(cursorMusic.getColumnIndex(MediaStore.Audio.Media.DATA));
                        String path = cursorMusic.getString(cursorMusic.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));

                        int x = cursorMusic.getColumnIndex(android.provider.MediaStore.Audio.Albums.ALBUM_ART);
                        String thisArt = cursorMusic.getString(x);

                        Bitmap bm= BitmapFactory.decodeFile(thisArt);
                        //ImageView image=(ImageView)findViewById(R.id.image);
                        //image.setImageBitmap(bm);

                        songs.add(fullPath);
                    }
                } // count > 0
            }
        }
        catch (Exception ex)
        {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println(ex.getMessage());/* ex.printStackTrace();*/
        }
        finally
        {
            if (cursorMusic != null) cursorMusic.close();
        }

        return  songs;
    }


}
