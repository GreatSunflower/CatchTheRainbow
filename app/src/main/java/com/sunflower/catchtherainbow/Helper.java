package com.sunflower.catchtherainbow;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import com.sunflower.catchtherainbow.AudioClasses.AudioFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        Date date = new Date((long) (seconds * 1000));
        String formattedDate = new SimpleDateFormat("mm:ss").format(date);
        return formattedDate;
    }

    public static int secondsToMilliseconds(double currentTime)
    {
        long millis = TimeUnit.SECONDS.toMillis((long) currentTime);
        return (int) millis;
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
            ContentResolver cr = context.getContentResolver();

            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
            String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
            cur = cr.query(uri, null, selection, null, sortOrder);
            int count = 0;

            if (cur != null)
            {
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
        } catch (Exception ex)
        {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println(ex.getMessage());/* ex.printStackTrace();*/
        } finally
        {
            if (cur != null) cur.close();
        }

        return songs;
    }

    public static Cursor getSongsAudioCursor(Context context, String filter)
    {
        Cursor cursorMusic = null;
        ContentResolver cr =  context.getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " LIKE ?  ASC";
        String album = MediaStore.Audio.Albums._ID+ "=?";
        cursorMusic = cr.query(uri, null, selection, new String[]{ filter }, sortOrder);

        return cursorMusic;
    }
    public static AudioFile getAudioFileById(long id, Cursor cursorMusic)
    {
        //cursorMusic.moveToFirst();
        //while (cursorMusic.moveToNext())
        {
            long idAudio = cursorMusic.getLong(cursorMusic.getColumnIndex(MediaStore.Audio.Media._ID));
            String title = cursorMusic.getString(cursorMusic.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artist = cursorMusic.getString(cursorMusic.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            double duration = cursorMusic.getDouble(cursorMusic.getColumnIndex(MediaStore.Audio.Media.DURATION));
            String fullPath = cursorMusic.getString(cursorMusic.getColumnIndex(MediaStore.Audio.Media.DATA));

            Bitmap art = null;
            try
            {
                String artPath = cursorMusic.getString(cursorMusic.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                art = BitmapFactory.decodeFile(artPath);
            }
            catch(Exception ex){}

            AudioFile audioFile = new AudioFile(idAudio, title, artist, duration, fullPath, AudioFile.imageToByteArray(art));
            return  audioFile;
        }
      //  return null;
    }

    public static ArrayList<AudioFile> getAllAudioOnDevice(Context context)
    {
        ArrayList<AudioFile> songs = new ArrayList<>();
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
                        long id = cursorMusic.getLong(cursorMusic.getColumnIndex(MediaStore.Audio.Media._ID));
                        String title = cursorMusic.getString(cursorMusic.getColumnIndex(MediaStore.Audio.Media.TITLE));
                        String artist = cursorMusic.getString(cursorMusic.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                        double duration = cursorMusic.getDouble(cursorMusic.getColumnIndex(MediaStore.Audio.Media.DURATION));
                        String fullPath = cursorMusic.getString(cursorMusic.getColumnIndex(MediaStore.Audio.Media.DATA));

                        Bitmap art = null;
                        try
                        {
                            String artPath = cursorMusic.getString(cursorMusic.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                            art = BitmapFactory.decodeFile(artPath);
                        }
                        catch(Exception ex){}

                        AudioFile audioFile = new AudioFile(title, artist, duration, fullPath, art);

                        songs.add(audioFile);
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

    public static double round(double value, int places)
    {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);

        return bd.doubleValue();
    }


}
