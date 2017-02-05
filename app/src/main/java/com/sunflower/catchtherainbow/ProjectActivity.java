package com.sunflower.catchtherainbow;


import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import com.sunflower.catchtherainbow.AudioClasses.SuperAudioPlayer;
import com.sunflower.catchtherainbow.Views.AudioChooserFragment;
import com.sunflower.catchtherainbow.Views.AudioChooserFragment.OnFragmentInteractionListener;
import com.sunflower.catchtherainbow.Views.AudioProgressView;

import java.io.File;
import java.util.List;
import java.util.Random;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AndroidFFMPEGLocator;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;

public class ProjectActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnFragmentInteractionListener, View.OnClickListener
{
    private ActionMenuView amvMenu;
    private Button playStopButt, bGetAudio;
    private AudioProgressView progressView;
    private View viewContentProject;
    private static final int PERMISSION_REQUEST_CODE = 1;

    // temp
    boolean isPlaying = false;
    SuperAudioPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);
        viewContentProject = (View)findViewById(R.id.content_project);
        // Init toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        amvMenu=  (ActionMenuView) findViewById(R.id.amvMenu);
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
        playStopButt = (Button)findViewById(R.id.Sacha);
        bGetAudio = (Button)findViewById(R.id.b_getAudio);
        bGetAudio.setOnClickListener(this);
        progressView = (AudioProgressView)findViewById(R.id.audioProgressView);
        progressView.setOnSeekBar(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b)
            {

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });

        new AndroidFFMPEGLocator(this);

        if (!checkPermission()) requestPermission();
        else
        {
            AudioDispatcher tempDisp = AudioDispatcherFactory.fromDefaultMicrophone(44100, 2048, 0);
            player = new SuperAudioPlayer(this);
            player.addPlayerListener(new SuperAudioPlayer.AudioPlayerListener()
            {
                @Override
                public void OnInitialized()
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            progressView.setMax((float) player.getDurationInSeconds());
                        }
                    });
                }

                @Override
                public void OnUpdate(AudioEvent audioEvent)
                {
                    //  final float duration = (float) audioEvent.getFrameLength() / audioEvent.getfra();
                    final float currentTime = (float) audioEvent.getTimeStamp();
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            //progressView.setMax(duration);
                            progressView.setCurrent(currentTime);
                        }
                    });
                }

                @Override
                public void OnFinish()
                {

                }
            });

            playStopButt.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (!isPlaying)
                    {
                        Random rand = new Random();
                        List<String> songs = Helper.getAllSongsOnDevice(ProjectActivity.this);
                        String file = songs.get(rand.nextInt(songs.size()));
                        try
                        {
                            player.load(new File(file));
                            isPlaying = true;
                            isPlaying = true;
                            playStopButt.setText("Stop");
                            player.play();
                        }
                        catch (Exception e)
                        {
                            Toast.makeText(ProjectActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    } else
                    {
                        player.stop();
                        isPlaying = false;
                        playStopButt.setText("Play");
                    }
                }
            });
        }
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.b_getAudio:
            {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                Fragment prev = getSupportFragmentManager().findFragmentById(R.id.MainLayout);
                if (prev != null) {  ft.remove(prev); }
                ft.addToBackStack(null);
                // Create and show the dialog.
                DialogFragment newFragment = AudioChooserFragment.newInstance();
                newFragment.show(ft, "dialog");
            }
        }
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
        getMenuInflater().inflate(R.menu.project, amvMenu.getMenu() /*menu*/); // adds menu items to the left side. If it's not needed replace second param with default menu
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
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        }
        else if (id == R.id.nav_gallery)
        {

        }
        else if (id == R.id.nav_slideshow)
        {

        }
        else if (id == R.id.nav_manage)
        {

        }
        else if (id == R.id.nav_share)
        {

        }
        else if (id == R.id.nav_send)
        {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri)
    {

    }


    /////////////////////////////////////////Permission///////////////////////////////////

    private boolean checkPermission()
    {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        } else
        {
            return false;
        }
    }

    private void requestPermission()
    {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
        {
            Toast.makeText(this,"GPS permission allows us to access location data. Please allow in App Settings for additional functionality.",Toast.LENGTH_LONG).show();
        }
        else
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode)
        {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Snackbar.make(viewContentProject,"Permission Granted, Now you can access location data.",Snackbar.LENGTH_LONG).show();
                }
                else
                {
                    Snackbar.make(viewContentProject,"Permission Denied, You cannot access location data.",Snackbar.LENGTH_LONG).show();
                }
                break;
        }
    }
}
