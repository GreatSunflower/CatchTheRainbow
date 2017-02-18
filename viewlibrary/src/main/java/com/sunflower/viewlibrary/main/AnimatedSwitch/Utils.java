package com.sunflower.viewlibrary.main.AnimatedSwitch;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.AppCompatDrawableManager;
import android.util.TypedValue;

/**
 * @author Adrián García Lomas
 */
public class Utils {

  /**
   * Convert Dp to Pixel
   */
  public static int dpToPx(float dp, Resources resources) {
    float px =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    return (int) px;
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private static Bitmap getBitmap(VectorDrawable vectorDrawable) {
    Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
            vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    vectorDrawable.draw(canvas);
    return bitmap;
  }

  private static Bitmap getBitmap(VectorDrawableCompat vectorDrawable) {
    Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
            vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    vectorDrawable.draw(canvas);
    return bitmap;
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private static Bitmap getBitmap(VectorDrawable vectorDrawable, int width, int height) {
    Bitmap bitmap = Bitmap.createBitmap(width,
            height, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    vectorDrawable.setBounds(0, 0, width, height);
    vectorDrawable.draw(canvas);
    return bitmap;
  }

  private static Bitmap getBitmap(VectorDrawableCompat vectorDrawable, int width, int height) {
    Bitmap bitmap = Bitmap.createBitmap(width,
            height, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    vectorDrawable.setBounds(0, 0, width, height);
    vectorDrawable.draw(canvas);
    return bitmap;
  }

  public static Bitmap getBitmap(Context context, @DrawableRes int drawableResId) {
    Drawable drawable =
            AppCompatDrawableManager.get().getDrawable(context, drawableResId);
    if (drawable instanceof BitmapDrawable) {
      return ((BitmapDrawable) drawable).getBitmap();
    } else if (drawable instanceof VectorDrawableCompat) {
      return getBitmap((VectorDrawableCompat) drawable);
    } else if (drawable instanceof VectorDrawable) {
      return getBitmap((VectorDrawable) drawable);
    } else {
      throw new IllegalArgumentException("Unsupported drawable type");
    }
  }

  public static Bitmap getBitmap(Context context, @DrawableRes int drawableResId, int width, int height) {
    Drawable drawable =
            AppCompatDrawableManager.get().getDrawable(context, drawableResId);
    if (drawable instanceof BitmapDrawable) {
      return Bitmap.createScaledBitmap(((BitmapDrawable) drawable).getBitmap(), width, height, false);
    } else if (drawable instanceof VectorDrawableCompat) {
      return getBitmap((VectorDrawableCompat) drawable, width, height);
    } else if (drawable instanceof VectorDrawable) {
      return getBitmap((VectorDrawable) drawable, width, height);
    } else {
      throw new IllegalArgumentException("Unsupported drawable type");
    }
  }
}
