package com.sunflower.catchtherainbow.Views.Editing;

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
import android.view.View;
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

    RelativeLayout rootContent;
    LinearLayout content;
    VerticalSeekBar gainBar;
    ToggleButton soloToggle;
    ImageButton removeButton;
    EditText trackNameEdit;
    Switch muteSwitch;
    SuperSeekBar pan;

    //pan	The pan position... -1 (full left) to +1 (full right), 0 = centre.


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
        gestureDetector = new GestureDetector(getContext(),new GestureListener());
        //final ImageButton verticalThumb = (ImageButton)findViewById(R.id.vertical_thumb);
        //ImageButton horizontalThumb = (ImageButton)findViewById(R.id.horizontal_thumb);

        //verticalThumb.setOnTouchListener(thumbTouchListener);
        //horizontalThumb.setOnTouchListener(thumbTouchListener);
    }

    boolean isGainDragging = false;
    GestureDetector gestureDetector;
    void setTrack(final WaveTrack track, final Project project)
    {
        this.track = track;
        this.project = project;

        gainBar = (VerticalSeekBar)findViewById(R.id.gain_bar);
        soloToggle = (ToggleButton)findViewById(R.id.solo_toggle);
        muteSwitch = (Switch)findViewById(mute_track);
        removeButton = (ImageButton)findViewById(R.id.removeButton);
        trackNameEdit = (EditText)findViewById(R.id.track_name_edit);
        pan = (SuperSeekBar)findViewById(R.id.pan_bar);
        content = (LinearLayout)findViewById(R.id.content);
        rootContent = (RelativeLayout)findViewById(R.id.rootContent);

        content.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                return gestureDetector.onTouchEvent(event);
            }

        });

        gainBar.setMax(100);
        gainBar.setProgress((int)Math.floor((float)track.getGain() * 100));

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

                if (isChecked) {
                    muteSwitch.setThumbResource(R.drawable.track_sound_enabled);
                } else {
                    muteSwitch.setThumbResource(R.drawable.track_sound_disabled);
                }

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

    }

    boolean hidden = false;
    public class GestureListener extends
            GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {

            return true;
        }

        // event when double tap occurs
        @Override
        public boolean onDoubleTap(MotionEvent e)
        {
            GoneOrVisibleLayout(content);
            hidden = !hidden;
            if (hidden != false)
            {
                //new ResizeAnimation(content, rootContent.getWidth(), 10, rootContent.getHeight()).applyTransformation(2000, new Transformation());
                new ResizeAnimation(rootContent, rootContent.getWidth(), 10).applyTransformation(2000, new Transformation());
            }
            else
            {
                //new ResizeAnimation(content, rootContent.getWidth(), 350, rootContent.getHeight()).applyTransformation(2000, new Transformation());
                new ResizeAnimation(rootContent, rootContent.getWidth(), 350).applyTransformation(2000, new Transformation());
            }
            return true;
        }
    }

    public void GoneOrVisibleLayout(LinearLayout rootView)
    {
        final int childcount = rootView.getChildCount();
        for (int i = 0; i < childcount; i++)
        {
            View v = rootView.getChildAt(i);
            if(v instanceof LinearLayout)
            {
                GoneOrVisibleLayout((LinearLayout)v);
                if (R.id.solo_layout != v.getId())
                {
                    if (hidden == false) Helper.animateViewVisibility(v, View.GONE);
                    else Helper.animateViewVisibility(v, View.VISIBLE);
                }
            }
            else
            {
                if (R.id.mute_track != v.getId())
                {
                    if (hidden == false) Helper.animateViewVisibility(v, View.GONE);
                    else Helper.animateViewVisibility(v, View.VISIBLE);
                }
            }
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
}
