package com.sunflower.catchtherainbow.Views.Editing;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioTrack;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.sunflower.catchtherainbow.AudioClasses.AudioFileData;
import com.sunflower.catchtherainbow.AudioClasses.AudioIO;
import com.sunflower.catchtherainbow.AudioClasses.Project;
import com.sunflower.catchtherainbow.AudioClasses.SamplePlayer;
import com.sunflower.catchtherainbow.AudioClasses.SuperAudioPlayer;
import com.sunflower.catchtherainbow.AudioClasses.WaveTrack;
import com.sunflower.catchtherainbow.R;
import com.sunflower.catchtherainbow.Views.Editing.Waveform.soundfile.CheapSoundFile;
import com.sunflower.catchtherainbow.Views.Editing.Waveform.view.WaveformView;
import com.sunflower.catchtherainbow.Views.Helpful.ResizeAnimation;
import com.sunflower.catchtherainbow.Views.Helpful.Thumb;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainAreaFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainAreaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainAreaFragment extends Fragment
{
    private Project project;

    // a reference to sample player
    private AudioIO globalPlayer;

    private OnFragmentInteractionListener mListener;

    private TableLayout tracksLayout;

    // a list of track holders
    private ArrayList<TrackHolder> tracks = new ArrayList<>();

    protected WaveTrack selectedTrack = null;

    // wave track drawing management
    protected float offset = 0;
    protected float touchStart = 0;
    protected float touchInitialOffset = 0;

    protected int samplesPerPixel = 1;

    protected boolean headerCollapsed = false;

    public MainAreaFragment()
    {
        //TableRow
        // Required empty public constructor
    }

    public static MainAreaFragment newInstance(Project project)
    {
        MainAreaFragment fragment = new MainAreaFragment();
        fragment.project = project;
        fragment.project.addListener(fragment.projectListener);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_main_area, container, false);

        tracksLayout = (TableLayout)root.findViewById(R.id.tracks_layout);

        clearTracks();
        for(WaveTrack track: project.getTracks())
        {
            addTrack(track, 400, 250);
        }

        return root;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
    }

    @Override
    public void onDetach()
    {
        project.removeListener(projectListener);

        super.onDetach();
    }

    public void addTrack(final WaveTrack track, int w, int h)
    {
        // we already have this track
        if(containsTrack(track)) return;

        final TableRow trow = new TableRow(getActivity());

        // needs to be set to be able to select track
        trow.setClickable(true);
        trow.setFocusable(true);
        trow.setFocusableInTouchMode(true);
        trow.setBackgroundResource(R.drawable.track_selector);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER;
        trow.setLayoutParams(lp);
        trow.setPadding(1, 1, 1, 0);

        // header view
        TrackHeaderView head = new TrackHeaderView(getActivity());
        head.setTag(trow);
        trow.addView(head, 0);

        // waveform view
        WaveTrackView trackView = new WaveTrackView(getActivity(), track, 1);
        lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        trackView.setLayoutParams(lp);
        trackView.setListener(waveTrackViewListener);
        trow.addView(trackView, 1);

        // add header row
        tracksLayout.addView(trow);

        // thumb for resizing row
        final TableRow thumbRow = new TableRow(getActivity());

        Thumb th = new Thumb(getActivity(), head, Thumb.ThumbKind.Vertical);

        thumbRow.addView(th, 0);

        trow.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if(hasFocus)
                {
                    selectedTrack = track;
                    //Toast.makeText(getActivity(), track.getName(), Toast.LENGTH_SHORT).show();
                }
                else selectedTrack = null;
            }
        });

        // add thumb row
        tracksLayout.addView(thumbRow);

        TrackHolder holder = new TrackHolder(track, trow, head, trackView, thumbRow);
        tracks.add(holder);
    }

    public void removeTrack(WaveTrack track)
    {
        // loop through the tracks
        for(TrackHolder holder: tracks)
        {
            if(holder.track == track)
            {
                tracksLayout.removeView(holder.row);
                tracksLayout.removeView(holder.thumb);
                tracks.remove(holder);
                break;
            }
        } // for
    }

    public boolean containsTrack(WaveTrack track)
    {
        // loop through the tracks
        for(TrackHolder holder: tracks)
        {
            if(holder.track == track)
            {
                return true;
            }
        } // for
        return false;
    }

    public void clearTracks()
    {
        tracks.clear();
        tracksLayout.removeAllViews();
    }

    public void setSamplesPerPixel(int samplesPerPixel)
    {
        if(samplesPerPixel < 1 || samplesPerPixel > WaveTrackView.MAX_SAMPLES_PER_PIXEL)
            return;

        for(TrackHolder holder: tracks)
        {
            holder.waveformView.setSamplesPerPixel(samplesPerPixel);
        }

        this.samplesPerPixel = samplesPerPixel;
    }

    public int getSamplesPerPixel()
    {
        return samplesPerPixel;
    }


    private WaveTrackView.WaveTrackViewListener waveTrackViewListener = new WaveTrackView.WaveTrackViewListener()
    {
        @Override
        public void touchStart(float x)
        {
            touchStart = x;
            touchInitialOffset = offset;
        }

        @Override
        public void touchMove(float x)
        {
            offset = touchInitialOffset + (touchStart - x);

            if(offset < 0) offset = 0;

            for(TrackHolder holder: tracks)
            {
                holder.waveformView.setOffset(offset);
            }
        }

        @Override
        public void touchEnd()
        {

        }

        @Override
        public void fling(float x)
        {

        }

        @Override
        public void draw()
        {

        }

        @Override
        public void zoomIn()
        {
            for(TrackHolder holder: tracks)
            {
                holder.waveformView.zoomIn();
            }
        }

        @Override
        public void zoomOut()
        {
            for(TrackHolder holder: tracks)
            {
                holder.waveformView.zoomOut();
            }
        }
    };

    private Project.ProjectListener projectListener = new Project.ProjectListener()
    {
        @Override
        public void onCreate(Project project)
        {
            clearTracks();
            for(WaveTrack track: project.getTracks())
            {
                addTrack(track, 400, 250);
            }
        }

        @Override
        public void onUpdate(Project project)
        {
            clearTracks();
            for(WaveTrack track: project.getTracks())
            {
                addTrack(track, 400, 250);
            }
        }

        @Override
        public void onTrackRemoved(Project project, WaveTrack track)
        {
            removeTrack(track);
        }

        @Override
        public void onTrackAdded(Project project, WaveTrack track)
        {
            addTrack(track, 400, 250);
        }

        @Override
        public void onClose(Project project)
        {
            clearTracks();
        }
    };

    public WaveTrack getSelectedTrack()
    {
        return selectedTrack;
    }

    public AudioIO getGlobalPlayer()
    {
        return globalPlayer;
    }

    public void setGlobalPlayer(AudioIO globalPlayer)
    {
        this.globalPlayer = globalPlayer;
    }

    public interface OnFragmentInteractionListener
    {
        void onFragmentInteraction(Uri uri);
    }

    // changes header sizes
    TrackHeaderView.HeaderListener headerListener = new TrackHeaderView.HeaderListener()
    {
        @Override
        public void onToggleVisibilityRequest()
        {
            final boolean cachedCollapsed = headerCollapsed;
            for(final TrackHolder trackHolder: tracks)
            {
                trackHolder.header.setCollapsed(!headerCollapsed);
                int newWidth;
                if (headerCollapsed) newWidth = 375;
                else newWidth = 10;

                // Set the layer type to hardware
                //trackHolder.row.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                //trackHolder.waveformView.setWillNotDraw(true);

                ResizeAnimation animation = new ResizeAnimation(trackHolder.header, trackHolder.header.getWidth(), newWidth); /*.applyTransformation(2000, new Transformation())*/;
                animation.setDuration(600);
                animation.setInterpolator(new FastOutSlowInInterpolator());
                animation.setAnimationListener(new Animation.AnimationListener()
                {
                    @Override
                    public void onAnimationStart(Animation animation){}
                    @Override
                    public void onAnimationEnd(Animation animation)
                    {
                        // Set the layer type to hardware
                        trackHolder.waveformView.demandUpdate();
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation){}
                });

                trackHolder.header.startAnimation(animation);
            }

            // resize timeline view
            /*int newWidth;
            if (headerCollapsed) newWidth = 375;
            else newWidth = 10;

            ResizeAnimation animation = new ResizeAnimation(trackHolder.header, trackHolder.header.getWidth(), newWidth);
            animation.setDuration(600);
            animation.setInterpolator(new FastOutSlowInInterpolator());

            trackHolder.header.startAnimation(animation);*/

            headerCollapsed = !headerCollapsed;
        }
    };

    // holds views and main track
    class TrackHolder
    {
        protected WaveTrack track;
        protected TableRow row;
        protected TableRow thumb;
        protected TrackHeaderView header;
        protected WaveTrackView waveformView;

        public TrackHolder(WaveTrack track, TableRow row, TrackHeaderView header, WaveTrackView waveformView, TableRow thumb)
        {
            this.track = track;
            this.row = row;
            this.header = header;
            this.waveformView = waveformView;
            this.thumb = thumb;
            header.setTrack(track, project, headerListener);
        }
    }
}
