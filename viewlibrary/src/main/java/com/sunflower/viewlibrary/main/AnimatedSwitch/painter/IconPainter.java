package com.sunflower.viewlibrary.main.AnimatedSwitch.painter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.DrawableRes;

import com.sunflower.viewlibrary.R;
import com.sunflower.viewlibrary.main.AnimatedSwitch.Utils;

/**
 * @author Adrián García Lomas
 */
public abstract class IconPainter implements SwitchInboxPinnedPainter {

  @DrawableRes
  protected int drawableId;
  protected Bitmap iconBitmap;
  protected Context context;
  protected Paint paint;
  protected int width;
  protected int height;
  protected int imageHeight;
  protected int imageWidth;
  protected boolean isVisible = false;
  protected int iconXPosition;
  protected int iconYPosition;
  protected int margin;

  public IconPainter(Context context, @DrawableRes int drawableId, int margin) {
    this.context = context;
    this.drawableId = drawableId;
    this.margin = margin;
    init();
  }

  private void init() {
    paint = new Paint();
    paint.setAntiAlias(true);
    initBitmap();
  }

  protected void initBitmap() {
    int iconSize = (int) context.getResources().getDimension(R.dimen.icon_size);
    iconBitmap = Utils.getBitmap(context,drawableId,iconSize,iconSize);
    imageHeight = iconBitmap.getHeight();
    imageWidth = iconBitmap.getWidth();
  }

  @Override public void draw(Canvas canvas) {
    if (isVisible) {
      canvas.drawBitmap(iconBitmap, iconXPosition, iconYPosition, paint);
    }
  }

  @Override public void onSizeChanged(int height, int width) {
    this.height = height;
    this.width = width;
  }
}
