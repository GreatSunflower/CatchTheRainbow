package com.sunflower.catchtherainbow;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.Toast;

import com.sunflower.catchtherainbow.AudioClasses.AudioFile;
import com.sunflower.catchtherainbow.AudioClasses.AudioImporter;
import com.sunflower.catchtherainbow.AudioClasses.Clip;
import com.sunflower.catchtherainbow.AudioClasses.Project;
import com.sunflower.catchtherainbow.AudioClasses.SamplePlayer;
import com.sunflower.catchtherainbow.AudioClasses.SuperAudioPlayer;
import com.sunflower.catchtherainbow.AudioClasses.WaveTrack;
import com.sunflower.catchtherainbow.Views.AudioChooserFragment;
import com.sunflower.catchtherainbow.Views.AudioChooserFragment.OnFragmentInteractionListener;
import com.sunflower.catchtherainbow.Views.AudioProgressView;
import com.sunflower.catchtherainbow.Views.AudioVisualizerView;
import com.sunflower.catchtherainbow.Views.Editing.MainAreaFragment;
import com.sunflower.catchtherainbow.Views.Effects.EffectsHostFragment;
import com.un4seen.bass.BASS;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Queue;

public class ProjectActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnFragmentInteractionListener, View.OnClickListener, EffectsHostFragment.OnEffectsHostListener
{
    private static final String TAG = "Project";

    private ActionMenuView amvMenu;
    private ImageButton playStopButt, bNext, bPrev;
    private AudioProgressView progressView;
    private View viewContentProject;
    private AudioVisualizerView visualizerView;

    private RelativeLayout waveFormViewContainer;
    private MainAreaFragment tracksFragment;

    // updates status(position and time)
    private Handler statusHandler = new Handler();

    // temp
    private boolean isPlaying = false, isDragging = false;
    private SamplePlayer player;

    private Project project;

    Project currentProject;
    public void setCurrentProject(Project currentProject)
    {
        this.currentProject = currentProject;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        // TEMP. Clears project directory on loading------------------------------------------------
        Helper.createOrRecreateDir(SuperApplication.getAppDirectory());

        Intent intent = getIntent();
        String nameProject = intent.getStringExtra("nameProject");
        String openProjectWithName = intent.getStringExtra("openProjectWithName");

        if(nameProject != null) project = Project.createNewProject(nameProject, projectListener);
        if(openProjectWithName != null) {}

        // initialize default output device
        if (!BASS.BASS_Init(-1, 44100, 0))
        {
            Log.e(TAG, "Can't initialize device");
            return;
        }
        BASS.BASS_SetConfig(BASS.BASS_CONFIG_FLOATDSP, 32);

        ////////////////////////////////////////////////////permissions//////////////////////////
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAPTURE_AUDIO_OUTPUT,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MEDIA_CONTENT_CONTROL};

        if (!hasPermissions(this, PERMISSIONS))
        {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        //////////////////////////////////////////////////////////end permissions///////////////////////

        viewContentProject = (View) findViewById(R.id.content_project);

        // Init toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        amvMenu = (ActionMenuView) findViewById(R.id.amvMenu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Init drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // Init nav view
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // -----------------------------------------------------FOR TESTING PURPOSES---------------------------------------
        visualizerView = (AudioVisualizerView) findViewById(R.id.audioVisualizerView);
        //visualizerView.setDrawingKind(AudioVisualizerView.DrawingKind.Points);

        waveFormViewContainer = (RelativeLayout) findViewById(R.id.mainAreaContainer);

        // play/stop/next/prev
        playStopButt = (ImageButton) findViewById(R.id.Sacha);
        bNext = (ImageButton) findViewById(R.id.playNext);
        bPrev = (ImageButton) findViewById(R.id.playPrev);

        bPrev.setOnClickListener(this);
        bNext.setOnClickListener(this);

        progressView = (AudioProgressView) findViewById(R.id.audioProgressView);
        progressView.setMax(1.f);
        progressView.setCurrent(0);
        progressView.setOnSeekBar(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b)
            {
                if (player != null)
                    progressView.setCurrent(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
                isDragging = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                if (player != null)
                {
                    player.setPosition(seekBar.getProgress());
                }
                isDragging = false;
            }
        });

        // --------------------------------------AUDIO STUFF-------------------------------------------------
        player = new SamplePlayer(this);

        player.addPlayerListener(new SuperAudioPlayer.AudioPlayerListener()
        {
            @Override
            public void onInitialized(int totalTime/*final File file*/)
            {
                progressView.setMax(totalTime);
                progressView.setCurrent(0);
                /*MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(file.getAbsolutePath());
                String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                audioInfo.setText(artist + " - " + album + " - " + title);
                mmr.release();*/
                //waveFormViewContainer.setSoundFile(CheapSoundFile.create(file.getAbsolutePath(), null));
                //waveFormViewContainer.invalidate();
            }
            @Override
            public void onCompleted(){}
        });

        tracksFragment = MainAreaFragment.newInstance("");
        tracksFragment.setGlobalPlayer(player);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainAreaContainer, tracksFragment)
                .commit();

        // play stop handler
        playStopButt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (!isPlaying)
                {
                    try
                    {
                        isPlaying = true;
                        playStopButt.setImageResource(R.drawable.ic_pause);
                        player.playPause(true);
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(ProjectActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
                else
                {
                    try
                    {
                        player.playPause(false);
                        isPlaying = false;
                        playStopButt.setImageResource(R.drawable.ic_play);
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(ProjectActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
        });
        // -----------------------------------------------

        // timer to update the display
        Runnable timer = new Runnable()
        {
            public void run()
            {
                if (!isDragging && player.isPlaying()) progressView.setCurrent(player.getProgress());

                int bufferSize = 1024;
                ByteBuffer audioData = ByteBuffer.allocateDirect(bufferSize*4);
                audioData.order(ByteOrder.LITTLE_ENDIAN); // little-endian byte order
                BASS.BASS_ChannelGetData(player.getChannelHandle(), audioData, bufferSize*4);
                float[] pcm = new float[bufferSize]; // allocate an array for the sample data
                audioData.asFloatBuffer().get(pcm);

                // make object array
                Float []res = new Float[pcm.length];
                for(int i = 0; i < pcm.length; i++)
                    res[i] = pcm[i];

                // pass new data
                visualizerView.updateVisualizer(res);

                statusHandler.postDelayed(this, 50);
            }
        };
        statusHandler.postDelayed(timer, 50);
    }

    @Override
    protected void onStop()
    {
        if(player!=null) player.stop();
        super.onStop();
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.playNext:
            {
                if (player != null)
                {
                    try
                    {
                        player.playNext();
                    }
                    catch (Exception ex)
                    {
                        Toast.makeText(this, "Cannot be played!", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
            case R.id.playPrev:
            {
                if (player != null)
                {
                    try
                    {
                        player.playPrev();
                    }
                    catch (Exception ex)
                    {
                        Toast.makeText(this, "Cannot be played!", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
        } // switch
    }

    @Override
    public void onDestroy()
    {
        player.disposePlayer();
        super.onDestroy();
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.project, /*amvMenu.getMenu() /**/menu); // adds menu items to the left side. If it's not needed replace second param with default menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment prev = getSupportFragmentManager().findFragmentById(R.id.SuperAudioChooser);
            if (prev != null)  ft.remove(prev);
            ft.addToBackStack(null);
            // Create and show the dialog.
            AudioChooserFragment newFragment = AudioChooserFragment.newInstance();
            newFragment.show(ft, "Song chooser dialog");
            return true;
        }
        if (id == R.id.action_effects)
        {
            //EffectsHostFragment fragment = (EffectsHostFragment) createNewDialog(R.id.effectsFragment, EffectsHostFragment.class, true);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment prev = getSupportFragmentManager().findFragmentById(R.id.effectsFragment);
            if (prev != null) ft.remove(prev);
            ft.addToBackStack(null);
            // Create and show the dialog.

            //ListEffectsFragment effectsFragment = ListEffectsFragment.newInstance(player.getChannelHandle());
            //effectsFragment.setEffects(delayEffect, rateTransposer, flangerEffect);

            EffectsHostFragment hostFragment = EffectsHostFragment.newInstance();
            hostFragment.setChannel(player.getChannelHandle());
            hostFragment.show(ft, "Effects dialog");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    // Universal method for creating dialog fragments
    private DialogFragment createNewDialog(int fragmentId, Class<? extends DialogFragment> fragmentClass, boolean showImmediately)
    {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentById(fragmentId);
        if (prev != null)
        {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        // Create and show the dialog.
        DialogFragment newFragment = null;
        try
        {
            newFragment = fragmentClass.newInstance();
            if (showImmediately) newFragment.show(ft, "Effects dialog");
            return newFragment;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera)
        {
            // Handle the camera action
        } else if (id == R.id.nav_gallery)
        {

        } else if (id == R.id.nav_slideshow)
        {

        } else if (id == R.id.nav_manage)
        {

        } else if (id == R.id.nav_share)
        {

        } else if (id == R.id.nav_send)
        {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private Project.ProjectListener projectListener = new Project.ProjectListener()
    {
        @Override
        public void onCreate(Project project)
        {

        }

        @Override
        public void onUpdate(Project project)
        {
            tracksFragment.clearTracks();
            for(WaveTrack track: project.getTracks())
            {
                tracksFragment.addTrack(track, 400, 250);
            }
        }

        @Override
        public void onClose(Project project)
        {

        }
    };

    // ---------------------------------SONG CHOOSER LISTENER INTERFACE----------------------------------

    @Override
    public void onOk(ArrayList<AudioFile> selectedAudioFiles)
    {
        AudioImporter importer = AudioImporter.getInstance();
        importer.setProject(project);
        importer.setListener(importerListener);

        AudioImporter.ImporterQuery[] queries = new AudioImporter.ImporterQuery[selectedAudioFiles.size()];

        // add all of the selected sound files to queue
        for (int i = 0; i <selectedAudioFiles.size(); i++)
        {
            AudioFile f = selectedAudioFiles.get(i);
            String destPath = SuperApplication.getAppDirectory() + "/" + f.getTitle();
            queries[i] = new AudioImporter.ImporterQuery(f, destPath);
            //tracksFragment.addTrack(f.getPath(), R.dimen.audio_track_default_width, R.dimen.audio_track_default_height);
        }
        // start loading
        importer.addToQueue(queries);
    }

    private AudioImporter.AudioImporterListener importerListener = new AudioImporter.AudioImporterListener()
    {
        NotificationManager notificationManager;
        NotificationCompat.Builder builder;
        int notificationId = 0;
        int count = 0, totalFiles = 0; // number of imported files

        @Override
        public void onBegin(Queue<AudioImporter.ImporterQuery> queries)
        {
            count = 0;
            totalFiles = queries.size()+1;

            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // remove the notification in case it's there
            notificationManager.cancel(notificationId);

            // custom notification layout
            RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.import_notification);
            contentView.setImageViewResource(R.id.image, R.drawable.app_icon);
            contentView.setTextViewText(R.id.title, "Audio Import");
            contentView.setTextViewText(R.id.text, "Import progress: 1/" + queries.size());

            builder = new NotificationCompat.Builder(ProjectActivity.this)
                    .setSmallIcon(R.drawable.app_icon)
                    .setAutoCancel(true)
                    .setContent(contentView);

            // allows to focus activity on click
            Intent activityIntent = new Intent(ProjectActivity.this, ProjectActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pIntent = PendingIntent.getActivity(ProjectActivity.this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pIntent);


            Notification notification = builder.build();
            notification.flags |= Notification.FLAG_NO_CLEAR;
            notification.defaults |= Notification.DEFAULT_LIGHTS;
            notificationManager.notify(notificationId, notification);
        }

        @Override
        public void onProgressUpdate(AudioImporter.ImporterQuery query, int progress, Clip clip)
        {
            RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.import_notification);
            contentView.setImageViewResource(R.id.image, R.drawable.app_icon);
            contentView.setTextViewText(R.id.title, "Audio Import");
            contentView.setTextViewText(R.id.text, "Import progress: " + (count+1) + "/" + totalFiles);
            contentView.setProgressBar(R.id.progressBar, 100, progress, false);

            Notification notification = builder.setContent(contentView).build();
            notification.flags |= Notification.FLAG_NO_CLEAR; // non cancellable notificaion
            notificationManager.notify(notificationId, notification);

            // audio loaded
            if(progress == 100)
            {
                WaveTrack track = new WaveTrack(query.audioFileInfo.getTitle(), project.getFileManager());
                track.addClip(clip);

                project.addTrack(track);
                count++;
            }
        }

        @Override
        public void onFinish(ArrayList<AudioImporter.ImporterQuery> succeededQueries, ArrayList<AudioImporter.ImporterQuery> failedQueries)
        {
            RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.import_notification);
            contentView.setImageViewResource(R.id.image, R.drawable.app_icon);
            contentView.setTextViewText(R.id.title, "Importing has been complete!");

            String detailedMessage = "Succeeded: " + succeededQueries.size();
            if(failedQueries.size() > 0)
                detailedMessage += "\nFailed: " + failedQueries.size();

            contentView.setTextViewText(R.id.text, detailedMessage);

            // hide progress bar
            contentView.setViewVisibility(R.id.progressBar, View.GONE);

            Notification notification = builder.setContent(contentView).build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.defaults |= Notification.DEFAULT_VIBRATE;
            notificationManager.notify(notificationId, notification);

            // remove listener
            AudioImporter.getInstance().setListener(null);
        }
    };


    @Override
    public void onCancel()
    {

    }

    // ---------------------------------Effects Host LISTENER INTERFACE----------------------------------

    @Override
    public void onEffectsConfirmed()
    {

    }

    @Override
    public void onEffectsCancelled()
    {

    }

    // ----- permissions ----------
    public static boolean hasPermissions(Context context, String... permissions)
    {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null)
        {
            for (String permission : permissions)
            {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                {
                    return false;
                }
            }
        }
        return true;
    }
}

