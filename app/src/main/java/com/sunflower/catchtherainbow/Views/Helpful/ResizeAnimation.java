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
       // setStartTime(1050);
        //setStartOffset(1500);
        setDuration(300);
    }

    @Override
    public boolean willChangeBounds()
    {
        return true;
    }

    @Override
    public void applyTransformation(float interpolatedTime, Transformation t)
    {
        float width = mFromWidth + ((mToWidth - mFromWidth) * interpolatedTime); ;/*(mToWidth - mFromWidth) * interpolatedTime + mFromWidth*/;
        ViewGroup.LayoutParams p = mView.getLayoutParams();
        p.width = (int) width;
        mView.requestLayout();
    }
}
