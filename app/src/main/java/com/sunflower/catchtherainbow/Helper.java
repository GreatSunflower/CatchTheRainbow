package com.sunflower.catchtherainbow;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.sunflower.catchtherainbow.AudioClasses.AudioFile;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
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
    // time converters
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

    // math
    public static <T extends Comparable<T>> T clamp(T val, T min, T max)
    {
        if (val.compareTo(min) < 0) return min;
        else if (val.compareTo(max) > 0) return max;
        else return val;
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
        }
        catch (Exception ex)
        {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println(ex.getMessage());/* ex.printStackTrace();*/
        } finally
        {
            if (cur != null) cur.close();
        }

        return songs;
    }

    public static Cursor getSongsAudioCursor(Context context, String filter, String sortOrder)
    {
        Cursor cursorMusic = null;
        ContentResolver cr =  context.getContentResolver();

        String[] projection = { BaseColumns._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Artists.ARTIST, MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM};

        //String sortOrder = MediaStore.Audio.Media.TITLE + " LIKE ?  ASC";
        //sortOrder = MediaStore.Audio.Media.TITLE;
        //storage/emulated/0/Samsung/Music/Over the Horizon.mp3
        String where = MediaStore.Audio.Media.TITLE + " LIKE ? or " + MediaStore.Audio.Artists.ARTIST + " LIKE ?";
        String[] params = new String[] { "%"+ filter + "%", "%"+ filter + "%"};

        cursorMusic = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, where, params, sortOrder);

        return cursorMusic;
    }

    public static Bitmap getAlbumArt(Context context, Long album_id)
    {
        Bitmap bm = null;
        try
        {
            //content://media/external/audio/media
            final Uri sArtworkUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);

            ContentResolver cr = context.getContentResolver();
            ParcelFileDescriptor pfd = cr.openFileDescriptor(uri, "r");

            if (pfd != null)
            {
                FileDescriptor fd = pfd.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fd);
            }

        } catch (Exception e) {       }
        return bm;
    }

    public static AudioFile getAudioFileByCursor(Context context, Cursor cursorMusic)
    {
        long idAudio = cursorMusic.getLong(cursorMusic.getColumnIndex(MediaStore.Audio.Media._ID));
        String title = cursorMusic.getString(cursorMusic.getColumnIndex(MediaStore.Audio.Media.TITLE));
        String artist = cursorMusic.getString(cursorMusic.getColumnIndex(MediaStore.Audio.Media.ARTIST));
        double duration = cursorMusic.getDouble(cursorMusic.getColumnIndex(MediaStore.Audio.Media.DURATION));
        String fullPath = cursorMusic.getString(cursorMusic.getColumnIndex(MediaStore.Audio.Media.DATA));

        Bitmap art = null;
       /* try
        {
            //art = Helper.getAlbumArt(context, cursorMusic.getLong(cursorMusic.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
        }
        catch(Exception ex)
        {
            //ex.printStackTrace();
        }*/

        AudioFile audioFile = new AudioFile(idAudio, title, artist, Helper.millisecondsToSeconds(duration), fullPath, AudioFile.imageToByteArray(art));
        return  audioFile;
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
           // String album = MediaStore.Audio.Albums._ID+ "=?";
            //new String[] {"%/storage/emulated/0/Samsung/Music/%"}
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
                        /*try
                        {
                            String artPath = cursorMusic.getString(cursorMusic.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                            art = BitmapFactory.decodeFile(artPath);
                        }
                        catch(Exception ex){}*/

                        AudioFile audioFile = new AudioFile(title, artist, Helper.millisecondsToSeconds(duration), fullPath, art);

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

    public static Integer[] getNormalizedBuffer(int []buffer)
    {
        Integer[] res = new Integer[buffer.length];
        for(int i = 0; i < buffer.length; i++)
        {
           // int value = (32767 - buffer[i]) / 65536;
            res[i] = buffer[i] + 128;
           // float value =  (buffer[i] * 255.f) * 1;
           // res[i] = (int)(value > 0 ? value: value*-1);
        }
        return res;
    }

    // clears old directory files or simply creates an empty one
    public static boolean createOrRecreateDir(String dirPath)
    {
        File directory = new File(dirPath);

        if(directory.exists())
        {
            // it's not a directory
            if(!directory.isDirectory()) return false;

            String[] children = directory.list();
            for (int i = 0; i < children.length; i++)
            {
                deleteRecursively(new File(directory, children[i]));//.delete();
            }
            return true;
        }
        return directory.mkdir();
    }

    // makes sure that the directory exists
    public static void checkDirectory(String dirPath)
    {
        File directory = new File(dirPath);

        if(!directory.exists())
            directory.mkdir();
    }

    // completely wipes out file or directory
    public static void deleteRecursively(File fileOrDirectory)
    {
        if (fileOrDirectory.isDirectory())
        {
            for (File child : fileOrDirectory.listFiles())
            {
                deleteRecursively(child);
            }
        }

        fileOrDirectory.delete();
    }
}
