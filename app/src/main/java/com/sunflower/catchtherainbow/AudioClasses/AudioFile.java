package com.sunflower.catchtherainbow.AudioClasses;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Alexandr on 05.02.2017.
 */

public class AudioFile
{
    private String name, artist, path;
    private byte[] image;
    public AudioFile(String name, String artist, String path, byte[] image)
    {
        this.name = name;
        this.artist = artist;
        this.path = path;
        this.image = image;
    }

    public void setImage(Bitmap img) throws IOException
    {
        image = imageToByteArray(img);
    }

    public Bitmap getBitmapImage()
    {
        if(image == null) return null;

        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
        return bitmap;
    }

    public static byte[] imageToByteArray(Bitmap image) throws IOException
    {
        if(image == null) return null;

        image = resizeImageForImageView(image, 512);//image.createScaledBitmap(image, 120, 120, true);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 50, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    public static Bitmap resizeImageForImageView(Bitmap bitmap, int scaleSize)
    {
        Bitmap resizedBitmap = null;
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int newWidth = -1;
        int newHeight = -1;
        float multFactor = -1.0F;
        if(originalHeight > originalWidth)
        {
            newHeight = scaleSize ;
            multFactor = (float) originalWidth/(float) originalHeight;
            newWidth = (int) (newHeight*multFactor);
        }
        else if(originalWidth > originalHeight)
        {
            newWidth = scaleSize ;
            multFactor = (float) originalHeight/ (float)originalWidth;
            newHeight = (int) (newWidth*multFactor);
        }
        else if(originalHeight == originalWidth)
        {
            newHeight = scaleSize ;
            newWidth = scaleSize ;
        }
        resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
        return resizedBitmap;
    }
}
