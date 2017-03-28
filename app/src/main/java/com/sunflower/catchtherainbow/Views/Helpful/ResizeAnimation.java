package com.sunflower.catchtherainbow.Views.Helpful;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ResizeAnimation extends Animation
{
    private View mView;

    private float mToWidth;
    private float mFromWidth;

    public ResizeAnimation(View v, float fromWidth, float toWidth)
    {
        mFromWidth = fromWidth;
        mToWidth = toWidth;
        mView = v;
        setDuration(300);
    }

    @Override
    public void applyTransformation(float interpolatedTime, Transformation t)
    {
        float width = (mToWidth - mFromWidth) * interpolatedTime + mFromWidth;
        ViewGroup.LayoutParams p = mView.getLayoutParams();
        p.width = (int) width;
        mView.requestLayout();
    }
}
