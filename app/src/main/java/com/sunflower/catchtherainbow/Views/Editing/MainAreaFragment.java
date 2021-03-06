package com.sunflower.catchtherainbow.Views.Editing;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.sunflower.catchtherainbow.AudioClasses.AudioHelper;
import com.sunflower.catchtherainbow.AudioClasses.AudioIO;
import com.sunflower.catchtherainbow.AudioClasses.Project;
import com.sunflower.catchtherainbow.AudioClasses.WaveTrack;
import com.sunflower.catchtherainbow.Helper;
import com.sunflower.catchtherainbow.R;
import com.sunflower.catchtherainbow.Views.Helpful.ResizeAnimation;
import com.sunflower.catchtherainbow.Views.Helpful.Thumb;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainAreaFragment.TrackFragmentListener} interface
 * to handle interaction events.
 * Use the {@link MainAreaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainAreaFragment extends Fragment
{
    private Mode mode = Mode.Hand;

    private SampleRange selection = new SampleRange();

    private Project project;

    private WaveRenderer renderer;

    // a reference to sample player
    private AudioIO globalPlayer;

    private TrackFragmentListener listener;

    private TableLayout tracksLayout;

    // a list of track holders
    private ArrayList<TrackHolder> tracks = new ArrayList<>();

    protected WaveTrack selectedTrack = null;

    // wave track drawing management
    protected long offset = 0;
    protected float touchStart = 0;
    protected float touchInitialOffset = 0;

    protected int samplesPerPixel = 1;

    protected boolean headerCollapsed = false;

    protected View dummySpacer;

    protected float collapsedHeadWidth = 0;
    protected float expandedHeadWidth = 0;

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

        collapsedHeadWidth = getResources().getDimension(R.dimen.track_head_collapsed);
        expandedHeadWidth = getResources().getDimension(R.dimen.track_head_expanded);

        renderer = new WaveRenderer(getContext());
        renderer.setName("Wave tracks renderer");
        renderer.setPriority(Thread.MAX_PRIORITY);
        renderer.start();
    }

    LinearLayout liner, message;
    Timeline timeline;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_main_area, container, false);

        tracksLayout = (TableLayout)root.findViewById(R.id.tracks_layout);
        liner = (LinearLayout)root.findViewById(R.id.id_liner);
        message = (LinearLayout)root.findViewById(R.id.id_message);

        dummySpacer = root.findViewById(R.id.dummy_view);

        timeline = (Timeline)root.findViewById(R.id.timeline);
        timeline.setProject(project);

       /* if(headerCollapsed) setViewWidth(dummySpacer, collapsedHeadWidth, false);
        else setViewWidth(dummySpacer, expandedHeadWidth, false);;*/

        /*ScrollView scrollView = (ScrollView)root.findViewById(R.id.verticalScrollView);
        scrollView.requestDisallowInterceptTouchEvent(true);*/
        clearTracks();
        for(WaveTrack track: project.getTracks())
        {
            addTrack(track);
        }

        setVisibilityView();

        return root;
    }

    public void setVisibilityView()
    {
        if(project.getTracks().size() == 0)
        {
            Helper.animateViewVisibility(message, View.VISIBLE);
            Helper.animateViewVisibility(liner, View.GONE);
        }
        else
        {
            Helper.animateViewVisibility(liner, View.VISIBLE);
            Helper.animateViewVisibility(message, View.GONE);
        }
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        if(headerCollapsed) setViewWidth(dummySpacer, collapsedHeadWidth, false);
        else setViewWidth(dummySpacer, expandedHeadWidth, false);
    }

    @Override
    public void onDetach()
    {
        project.removeListener(projectListener);
        renderer.setRunning(false);

        super.onDetach();
    }

    public void addTrack(final WaveTrack track)
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
        head.setCollapsed(headerCollapsed);
        setViewWidth(head, headerCollapsed?collapsedHeadWidth:expandedHeadWidth, false);

        // waveform view
        WaveTrackView trackView = new WaveTrackView(getActivity(), track, 1);
        lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        trackView.setLayoutParams(lp);
        trackView.setListener(waveTrackViewListener);
        trackView.setTag(trow);
        trackView.setOffset(offset);
        trackView.setSamplesPerPixel(samplesPerPixel);
        trackView.setSelection(selection);
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

        // make it render waveforms
        renderer.addTrack(trackView);
        setVisibilityView();
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
                renderer.removeTrack(holder.waveformView);
                break;
            }
        } // for
        setVisibilityView();
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

    public void startDrawing()
    {
        for(TrackHolder holder: tracks)
        {
            holder.waveformView.startDrawing();
        }
    }

    public void stopDrawing()
    {
        for(TrackHolder holder: tracks)
        {
            holder.waveformView.stopDrawing();
        }
    }

    public void setSamplesPerPixel(int samplesPerPixel)
    {
        if(samplesPerPixel < 1)
            samplesPerPixel = 1;
        else if(samplesPerPixel > WaveTrackView.MAX_SAMPLES_PER_PIXEL)
            return;

        for(TrackHolder holder: tracks)
        {
            holder.waveformView.setSamplesPerPixel(samplesPerPixel);
        }

        this.samplesPerPixel = samplesPerPixel;

        timeline.setSamplesPerPixel(samplesPerPixel);

        if(listener != null)
            listener.onZoomChanged(samplesPerPixel);

        demandUpdate();
    }

    public int getSamplesPerPixel()
    {
        return samplesPerPixel;
    }

    public float getOffset()
    {
        return offset;
    }

    // offset in samples
    public void setOffset(long offset)
    {
        WaveTrack longestTrack = findLongestTrack();

        if(longestTrack == null) return;

        if(offset < 0) offset = 0;
        if(offset > longestTrack.getEndSample()) return;

        this.offset = offset;

        timeline.setOffset(offset);

        //Log.e("Offset Update! ", "Offset : " + offset);

        for(TrackHolder holder: tracks)
        {
            holder.waveformView.setOffset(offset);
        }
    }

    public double getSelectionStartTime()
    {
        if(selection == null) return -1;

        return AudioHelper.samplesToTime(selection.startingSample, project.getProjectAudioInfo());
    }

    public double getSelectionEndTime()
    {
        if(selection == null) return -1;

        return AudioHelper.samplesToTime(selection.endSample, project.getProjectAudioInfo());
    }

    // returns null if an error occurred
    public WaveTrack findLongestTrack()
    {
        if(tracks.isEmpty()) return null;

        WaveTrack maxTrack = tracks.get(0).track;
        for(int i = 0; i < tracks.size(); i++)
        {
            WaveTrack track =  tracks.get(i).track;

            if(track.getEndTime() > maxTrack.getEndTime())
            {
                maxTrack = track;
            }
        }
        return maxTrack;
    }

    public void demandUpdate()
    {
        for (TrackHolder holder : tracks)
        {
            holder.waveformView.demandFullUpdate();
        }
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
            if(mode == Mode.Hand)
            {
                offset = (long) (touchInitialOffset + ((touchStart - x)* samplesPerPixel));

                if(offset < 0) offset = 0;

                for (TrackHolder holder : tracks)
                {
                    holder.waveformView.setOffset((long) (offset));
                }

                timeline.setOffset(offset);
            }
            else // selection
            {
                long startSample = (long)(offset + (touchStart * samplesPerPixel));
                long endSample = (long) (offset + ((x) * samplesPerPixel));

                if(startSample > endSample)  // swap
                {
                    long temp = startSample;
                    startSample = endSample;
                    endSample = temp;
                }

                selection = new SampleRange(startSample, endSample);

                if(listener != null)
                    listener.onSelectionChanged(selection);

                // Log.e("Selection Update ", "Start : " + startSample + ", End: " + endSample);

                for (TrackHolder holder : tracks)
                {
                    holder.waveformView.setSelection(selection);
                }
            }
        }

        @Override
        public void touchEnd()
        {
            /*if(mode == Mode.Selection)
            {
                double start = getSelectionStartTime();
                double end = getSelectionEndTime();

                Helper.showCuteToast(getContext(), "S : " + Helper.round(start, 2) + ", E: " + Helper.round(end, 2), Gravity.CENTER, Toast.LENGTH_SHORT);
            }*/
        }

        @Override
        public void draw()
        {

        }

        @Override
        public void zoomIn()
        {
            setSamplesPerPixel(samplesPerPixel - 4);
        }

        @Override
        public void zoomOut()
        {
            for(TrackHolder holder: tracks)
            {
                setSamplesPerPixel(samplesPerPixel + 4);
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
                addTrack(track);
            }
        }

        @Override
        public void onOpenError(Project project)
        {

        }

        @Override
        public void onUpdate(Project project)
        {
            clearTracks();
            for(WaveTrack track: project.getTracks())
            {
                addTrack(track);
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
            addTrack(track);
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

    public SampleRange getSelection()
    {
        return selection;
    }

    public void setSelection(SampleRange selection)
    {
        this.selection = selection;
        for (TrackHolder holder : tracks)
        {
            holder.waveformView.setSelection(selection);
        }
    }

    public Mode getMode()
    {
        return mode;
    }

    public void setMode(Mode mode)
    {
        this.mode = mode;
    }

    // changes header sizes
    TrackHeaderView.HeaderListener headerListener = new TrackHeaderView.HeaderListener()
    {
        @Override
        public void onToggleVisibilityRequest()
        {
            final boolean cachedCollapsed = headerCollapsed;

            float newWidth;
            if (headerCollapsed) newWidth = expandedHeadWidth;
            else newWidth = collapsedHeadWidth;

            for(final TrackHolder trackHolder: tracks)
            {
                trackHolder.header.setCollapsed(!headerCollapsed);

                ResizeAnimation animation = new ResizeAnimation(trackHolder.header, trackHolder.header.getWidth(), newWidth);
                animation.setDuration(600);
                animation.setInterpolator(new FastOutSlowInInterpolator());
                animation.setAnimationListener(new Animation.AnimationListener()
                {
                    @Override
                    public void onAnimationStart(Animation animation){}
                    @Override
                    public void onAnimationEnd(Animation animation)
                    {
                        trackHolder.waveformView.demandFullUpdate();
                        timeline.invalidate();
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation){}
                });

                trackHolder.header.startAnimation(animation);
            }

            // resize timeline
            setViewWidth(dummySpacer, newWidth, true);

            headerCollapsed = !headerCollapsed;
        }
    };

    public interface TrackFragmentListener
    {
        void onSelectionChanged(SampleRange newSelection);
        void onZoomChanged(int samplesPerPixel);
    }

    public TrackFragmentListener getListener()
    {
        return listener;
    }

    public void setListener(TrackFragmentListener listener)
    {
        this.listener = listener;
    }

    private void setViewWidth(View view, float newWidth, boolean animated)
    {
        if(animated)
        {
            ResizeAnimation animation = new ResizeAnimation(view, view.getWidth(), newWidth);
            animation.setDuration(600);
            animation.setInterpolator(new FastOutSlowInInterpolator());
            view.startAnimation(animation);
        }
        else
        {
            ViewGroup.LayoutParams p = view.getLayoutParams();
            p.width = (int) newWidth;
            view.requestLayout();
        }

    }

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

    public enum Mode
    {
        Hand,
        Selection
    }

    public static class SampleRange
    {
        public Long startingSample = 0L;
        public Long endSample = 0L;

        public SampleRange(){}

        public SampleRange(long startingSample, long endSample)
        {
            this.startingSample = startingSample;
            this.endSample = endSample;
        }

        public long getLen()
        {
            if(endSample < startingSample) return 0;

            return endSample - startingSample;
        }

    }
}
