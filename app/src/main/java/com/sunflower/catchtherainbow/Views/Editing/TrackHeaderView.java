package com.sunflower.catchtherainbow.Views.Editing;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.view.animation.Transformation;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.ToggleButton;

import com.sunflower.catchtherainbow.AudioClasses.Project;
import com.sunflower.catchtherainbow.AudioClasses.WaveTrack;
import com.sunflower.catchtherainbow.Helper;
import com.sunflower.catchtherainbow.R;
import com.sunflower.catchtherainbow.Views.Helpful.ResizeAnimation;
import com.sunflower.catchtherainbow.Views.Helpful.SuperSeekBar;
import com.sunflower.catchtherainbow.Views.Helpful.VerticalSeekBar;

import static com.sunflower.catchtherainbow.R.id.mute_track;

public class TrackHeaderView extends RelativeLayout
{
    private Project project;
    private WaveTrack track;
    private boolean hidden = false;

    RelativeLayout rootContent;
    LinearLayout content;
    VerticalSeekBar gainBar;
    ToggleButton soloToggle;
    ImageButton removeButton;
    EditText trackNameEdit;
    Switch muteSwitch;
    SuperSeekBar pan;
    //pan	The pan position... -1 (full left) to +1 (full right), 0 = centre.

    boolean isGainDragging = false;
    GestureDetector gestureDetector;

    private HeaderListener listener;

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
        // inside onCreate of Activity or Fragment
        gestureDetector = new GestureDetector(getContext(), new GestureListener());
        //final ImageButton verticalThumb = (ImageButton)findViewById(R.id.vertical_thumb);
        //ImageButton horizontalThumb = (ImageButton)findViewById(R.id.horizontal_thumb);

        //verticalThumb.setOnTouchListener(thumbTouchListener);
        //horizontalThumb.setOnTouchListener(thumbTouchListener);
    }

    void setTrack(final WaveTrack track, final Project project, HeaderListener listener)
    {
        this.track = track;
        this.project = project;
        this.listener = listener;

        gainBar = (VerticalSeekBar)findViewById(R.id.gain_bar);
        soloToggle = (ToggleButton)findViewById(R.id.solo_toggle);
        muteSwitch = (Switch)findViewById(mute_track);
        removeButton = (ImageButton)findViewById(R.id.removeButton);
        trackNameEdit = (EditText)findViewById(R.id.track_name_edit);
        pan = (SuperSeekBar)findViewById(R.id.pan_bar);
        content = (LinearLayout)findViewById(R.id.content);
        rootContent = (RelativeLayout)findViewById(R.id.rootContent);

        // Animation
        Animator scaleDown = ObjectAnimator.ofPropertyValuesHolder((Object)null, PropertyValuesHolder.ofFloat("scaleX", 1, 0), PropertyValuesHolder.ofFloat("scaleY", 1, 0));
        scaleDown.setDuration(300);
        //scaleDown.setStartDelay(1000);
        scaleDown.setInterpolator(new OvershootInterpolator());

        Animator scaleUp = ObjectAnimator.ofPropertyValuesHolder((Object)null, PropertyValuesHolder.ofFloat("scaleX", 0, 1), PropertyValuesHolder.ofFloat("scaleY", 0, 1));
        scaleUp.setDuration(600);
        //scaleUp.setStartDelay(1000);
        scaleUp.setInterpolator(new OvershootInterpolator());

        LayoutTransition itemLayoutTransition = new LayoutTransition();
        itemLayoutTransition.setAnimator(LayoutTransition.APPEARING, scaleUp);
        itemLayoutTransition.setAnimator(LayoutTransition.DISAPPEARING, scaleDown);

        content.setLayoutTransition(itemLayoutTransition);
        // Animation End

        // for gesture
        content.setOnTouchListener(new OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return gestureDetector.onTouchEvent(event);
            }
        });

        gainBar.setMax(100);
        gainBar.setProgress((int)Math.floor((float)track.getGain() * 100));

        pan.setMinValue(-1);
        pan.setMaxValue(1);
        pan.setCurrentValue(track.getPan());

        soloToggle.setChecked(track.isSolo());
        muteSwitch.setChecked(!track.isMuted());
        trackNameEdit.setText(track.getName());

        //.setOnSuperSeekBarChangeListener(new SuperSeekBar.OnSuperSeekBarChangeListener()
        gainBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                track.setGain((float)progress/100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
                isGainDragging = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                isGainDragging = false;
            }
        });

        removeButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog removeDialog = new AlertDialog.Builder(getContext())
                //set message, title, and icon
                .setTitle(R.string.delete)
                .setMessage(R.string.remove_track_conformation)
                .setPositiveButton(R.string.conformation, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        project.removeTrack(track);
                        dialog.dismiss();
                    }
                })

                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                })
                .create();
                removeDialog.show();
            }
        });

        soloToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                track.setSolo(isChecked);
            }
        });

        muteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                track.setMuted(!isChecked);

               /* if (isChecked) {
                    muteSwitch.setThumbResource(R.drawable.track_sound_enabled);
                } else {
                    muteSwitch.setThumbResource(R.drawable.track_sound_disabled);
                }*/

            }
        });

        trackNameEdit.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                track.setName(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s){}
        });

        pan.setOnSuperSeekBarChangeListener(new SuperSeekBar.OnSuperSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SuperSeekBar seekBar, float progress, boolean fromUser)
            {
                track.setPan(progress);
            }

            @Override
            public void onStartTrackingTouch(SuperSeekBar seekBar){}

            @Override
            public void onStopTrackingTouch(SuperSeekBar seekBar){}
        });

    }

    public void show()
    {
        setCollapsed(false);
    }

    public void collapse()
    {
        setCollapsed(true);
    }

    public void setCollapsed(boolean collapsed)
    {
        if(hidden == collapsed) return;

        setViewVisibility(content, collapsed? View.GONE: View.VISIBLE);

        hidden = collapsed;
    }

    // animates visibility changes. Applied to child views as well
    public void setViewVisibility(LinearLayout rootView, int visibility)
    {
        final int childCount = rootView.getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            View v = rootView.getChildAt(i);
            if(v instanceof LinearLayout)
            {
                setViewVisibility((LinearLayout)v, visibility);
                if (R.id.solo_layout != v.getId())
                {
                    //Helper.animateViewVisibility(v, visibility);
                    v.setVisibility(visibility);
                }
            }
            else
            {
                if (R.id.mute_track != v.getId())
                {
                    v.setVisibility(visibility);
                    //Helper.animateViewVisibility(v, visibility);
                }
            }
        } // for
    }

    public HeaderListener getListener()
    {
        return listener;
    }

    public void setListener(HeaderListener listener)
    {
        this.listener = listener;
    }

    // gesture
    public class GestureListener extends
            GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onDown(MotionEvent event)
        {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event)
        {
            View row = (View)getTag();
            return row.requestFocus();
        }
        // event when double tap occurs
        @Override
        public boolean onDoubleTap(MotionEvent e)
        {
            if(listener != null)
                listener.onToggleVisibilityRequest();

            return true;
        }
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

                    /*if(v.getId() == R.id.vertical_thumb)
                    {

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
                    }*/

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

    public interface HeaderListener
    {
        void onToggleVisibilityRequest();
    }
}
