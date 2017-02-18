package com.sunflower.catchtherainbow.Views.Editing;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TableRow;

import com.sunflower.catchtherainbow.Helper;
import com.sunflower.catchtherainbow.R;

/**
 * Created by SuperComputer on 2/17/2017.
 */

public class TrackHeaderView extends RelativeLayout
{
    public TrackHeaderView(Context context)
    {
        super(context);
        init();
    }

    public TrackHeaderView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    void init()
    {
        inflate(getContext(), R.layout.audio_track_head, this);
        setLayoutParams(new TableRow.LayoutParams(375, 200));

        //final ImageButton verticalThumb = (ImageButton)findViewById(R.id.vertical_thumb);
        ImageButton horizontalThumb = (ImageButton)findViewById(R.id.horizontal_thumb);

        //verticalThumb.setOnTouchListener(thumbTouchListener);
        horizontalThumb.setOnTouchListener(thumbTouchListener);
    }

    // used for resizing cell(both vertically and horizontally)
    View.OnTouchListener thumbTouchListener = new View.OnTouchListener()
    {
        Point lastPoint = new Point();

        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            // we need absolute raw coordinates in order to resize the cell
            int x = (int)event.getRawX();
            int y = (int)event.getRawY();
            int width = getLayoutParams().width;
            int height = getLayoutParams().height;

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

                    if(v.getId() == R.id.vertical_thumb)
                    {
                        //
                        float newHeight = height + (y - lastPoint.y);
                        float normalizedHeight = Helper.clamp(newHeight, getResources().getDimension(R.dimen.audio_track_min_height),
                                getResources().getDimension(R.dimen.audio_track_max_height));

                         getLayoutParams().height = (int) normalizedHeight;
                    }
                    else
                    {
                        float newWidth = width + (x - lastPoint.x);
                        float normalizedWidth = Helper.clamp(newWidth, getResources().getDimension(R.dimen.audio_track_min_width),
                                getResources().getDimension(R.dimen.audio_track_max_width));

                        getLayoutParams().width = (int) normalizedWidth;
                    }

                    // update layout
                    requestLayout();

                    // if the view is out of visible bounds it will be scrolled to
                    requestChildFocus(v, v);

                    lastPoint = new Point(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    // unpress
                    v.setPressed(false);
                    break;
            }

            /*float oldHeight = getMeasuredHeight();

            double Yoffset = event.getRawY() - lastPoint.y;

            int newHeight =  (int)(oldHeight + Yoffset);//(int)(oldHeight + ((getY() + event.getRawY()) / 2));

            float clampedHeight = Helper.clamp((float)(newHeight), getResources().getDimension(R.dimen.audio_track_min_height),
                  getResources().getDimension(R.dimen.audio_track_max_height));

            TableRow.LayoutParams lParams = new TableRow.LayoutParams(getMeasuredWidth(), (int) clampedHeight);
            setLayoutParams(lParams);*/


            //Log.d("!!!!!!!! THUMB !!!!!!", "YOffset: " + Yoffset + ", Y: " + event.getRawY() + ". Old Y: " + lastPoint.y);

            return true;
        }

       /* @Override
        public boolean onDrag(View v, DragEvent event)
        {
            Log.i("!!!!!!!! THUMB !!!!!!", "DRAG!!!");
            return true;
        }*/
    };


}
