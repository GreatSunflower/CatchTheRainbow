package com.sunflower.catchtherainbow.Views.Helpful;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableRow;

import com.sunflower.catchtherainbow.Helper;
import com.sunflower.catchtherainbow.R;

/**
 * A view that handles resizing from user. Needs to have a target view.
 */

public class Thumb extends RelativeLayout
{
    // a view that is gonna be resized
    private ViewGroup targetView;
    private Button thumb;
    private ThumbKind kind = ThumbKind.Vertical;
    private ScrollView verticalScrollView;

    public Thumb(Context context, ViewGroup targetView, ThumbKind kind)
    {
        super(context);
        init();
        setKind(kind);
        setTargetView(targetView);

    }

    public Thumb(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    private void init()
    {
        setLayoutParams(new TableRow.LayoutParams(375, 200));

        setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        setFocusable(false);
        setFocusableInTouchMode(false);

        TableRow.LayoutParams tl = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 18);
        tl.weight = 1;
        tl.gravity = Gravity.FILL_VERTICAL;
        tl.setMargins(0,0,0,0);
        setLayoutParams(tl);
        setPadding(0, 0, 0, 2);

        thumb = new Button(getContext());
        thumb.setId(getId());
        thumb.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        thumb.setBackgroundResource(R.drawable.track_thumb_selector);

        addView(thumb);

    }

   /* public void getHitRect(Rect outRect)
    {
        outRect.set(getLeft(), getTop(), getRight(), getBottom() + 130);
        Log.e("CustomView", "RECT!!! " + getId());
    }*/

    // Find a way to make it work!!!
   /* @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();

        final View parent = (View) getParent().getParent();  // button: the view you want to enlarge hit area
        parent.post( new Runnable()
        {
            public void run()
            {
                final Rect rect = new Rect();
                getHitRect(rect);
                rect.top -= 100;    // increase top hit area
                rect.left -= 100;   // increase left hit area
                rect.bottom += 100; // increase bottom hit area
                rect.right += 100;  // increase right hit area
                parent.setTouchDelegate( new TouchDelegate( rect , (View)getParent()));
            }
        });
        Log.d("CustomView", "onAttachedToWindow called for " + getId());
    }*/
    // increase touch area
        /*verticalThumb.post( new Runnable()
        {
            public void run()
            {
                final Rect r = new Rect();
                verticalThumb.getHitRect(r);
                r.top -= 12;
                r.bottom += 12;
                TouchDelegate expandedArea = new TouchDelegate(r,
                        verticalThumb);

                verticalThumb.setTouchDelegate(expandedArea);

                if (View.class.isInstance(verticalThumb.getParent())) {
                    ((View) verticalThumb.getParent())
                            .setTouchDelegate(expandedArea);
                }
            }
        });*/

    public View getTargetView()
    {
        return targetView;
    }

    public void setTargetView(ViewGroup targetView)
    {
        this.targetView = targetView;

        // set listener or reset
        if(targetView != null) thumb.setOnTouchListener(thumbTouchListener);
        else thumb.setOnTouchListener(null);
    }

    // used for resizing cell(both vertically and horizontally)
    View.OnTouchListener thumbTouchListener = new View.OnTouchListener()
    {
        Point lastPoint = new Point();

        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            // we need absolute raw coordinates in order to resize the cell
            int x = (int) event.getRawX();
            int y = (int) event.getRawY();
            int width = targetView.getLayoutParams().width;
            int height = targetView.getLayoutParams().height;

            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    lastPoint = new Point(x, y);
                    // don't let a scrollview mess with us
                    requestDisallowInterceptTouchEvent(true);
                    // we consume the touch event so to make selector work without click call this method
                    v.setPressed(true);
                    break;
                // actual resizing is done here
                case MotionEvent.ACTION_MOVE:
                    //Log.d(">>","width:"+width+" height:"+height+" x:"+x+" y:"+y);

                    if (kind == ThumbKind.Vertical)
                    {
                        // find the difference between touches and add it to the height
                        float newHeight = height + (y - lastPoint.y);
                        // clamp the value
                        float normalizedHeight = Helper.clamp(newHeight, getResources().getDimension(R.dimen.audio_track_min_height),
                                getResources().getDimension(R.dimen.audio_track_max_height));

                        // set new height
                        targetView.getLayoutParams().height = (int) normalizedHeight;
                    }
                    else
                    {
                        // the same as for vertical only work with x axis
                        float newWidth = width + (x - lastPoint.x);
                        float normalizedWidth = Helper.clamp(newWidth, getResources().getDimension(R.dimen.audio_track_min_width),
                                getResources().getDimension(R.dimen.audio_track_max_width));

                        targetView.getLayoutParams().width = (int) normalizedWidth;
                    }

                    // update layout
                    targetView.requestLayout();

                    // if the view is out of visible bounds it will be scrolled to
                    final Rect rect = new Rect(0, 0, getWidth(), getHeight());
                    requestRectangleOnScreen(rect, false);

                    lastPoint = new Point(x, y);
                    break;
                case MotionEvent.ACTION_UP: case MotionEvent.ACTION_CANCEL:
                    // unpress
                    v.setPressed(false);
                    break;
            }
            // consume input
            return true;
        }
    };

    public ThumbKind getKind()
    {
        return kind;
    }

    public void setKind(ThumbKind kind)
    {
        this.kind = kind;
    }

    public enum ThumbKind
    {
        Horizontal,
        Vertical
    }
}
