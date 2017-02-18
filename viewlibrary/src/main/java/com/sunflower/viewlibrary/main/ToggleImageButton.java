package com.sunflower.viewlibrary.main;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ToggleButton;

import com.sunflower.viewlibrary.R;

public class ToggleImageButton extends ToggleButton {

    private Drawable drawableOn;
    private Drawable drawableOff;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ToggleImageButton(final Context context,
                             final AttributeSet attrs,
                             final int defStyleAttr,
                             final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        obtainStyledAttributes(context, attrs);
        removeText();
    }

    public ToggleImageButton(final Context context,
                             final AttributeSet attrs,
                             final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        obtainStyledAttributes(context, attrs);
        removeText();
    }

    public ToggleImageButton(final Context context,
                             final AttributeSet attrs) {
        super(context, attrs);
        obtainStyledAttributes(context, attrs);
        removeText();
    }

    private void removeText() {
        this.setTextOn("");
        this.setTextOff("");


        // Workaround to force text removal
        super.setChecked(!this.isChecked());
        super.setChecked(!this.isChecked());
    }

    private void obtainStyledAttributes(final Context context, final AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ToggleImageButton, 0, 0);
        drawableOn = a.getDrawable(R.styleable.ToggleImageButton_tib_drawable_on);
        drawableOff = a.getDrawable(R.styleable.ToggleImageButton_tib_drawable_off);
        a.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        syncDrawableState();
    }

    private void syncDrawableState() {
        final boolean checked = isChecked();
        if (checked && drawableOn != null) {
            setBackgroundDrawable(drawableOn);
        } else if (!checked && drawableOff != null) {
            setBackgroundDrawable(drawableOff);
        }
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);

        syncDrawableState();
    }

    public void setDrawableOn(final Drawable drawableOn) {
        this.drawableOn = drawableOn;
        syncDrawableState();
    }

    public void setDrawableOff(final Drawable drawableOff) {
        this.drawableOff = drawableOff;
        syncDrawableState();
    }
}