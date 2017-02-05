package com.sunflower.catchtherainbow;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    // temp
    boolean isPlaying = false;
    SuperAudioPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);
        viewContentProject = (View)findViewById(R.id.content_project);

        ////////////////////////////////////////////////////permission
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAPTURE_AUDIO_OUTPUT,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MEDIA_CONTENT_CONTROL};

        if(!hasPermissions(this, PERMISSIONS))
        {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        //////////////////////////////////////////////////////////end permission


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

        //if (checkAndRequestPermissions())
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

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.b_getAudio:
            {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                Fragment prev = getSupportFragmentManager().findFragmentById(R.id.SuperAudioChooser);
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
    /*<uses-permission android:name="android.permission.RECORD_AUDIO"/>
    <uses-permission android:name="android.permission.CAPTURE_AUDIO_OUTPUT"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.MEDIA_CONTENT_CONTROL"/>*/
    /*private  boolean checkAndRequestPermissions()
    {
        int permissionRECORD_AUDIO = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        int permissionCAPTURE_AUDIO_OUTPUT = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAPTURE_AUDIO_OUTPUT);
        int permissionWRITE_EXTERNAL_STORAGE = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionMEDIA_CONTENT_CONTROL = ContextCompat.checkSelfPermission(this,
                Manifest.permission.MEDIA_CONTENT_CONTROL);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (permissionRECORD_AUDIO != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if (permissionCAPTURE_AUDIO_OUTPUT != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAPTURE_AUDIO_OUTPUT);
        }
        if (permissionWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionMEDIA_CONTENT_CONTROL != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.MEDIA_CONTENT_CONTROL);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        //Log.d(TAG, "Permission callback called-------");
        switch (requestCode)
        {
            case REQUEST_ID_MULTIPLE_PERMISSIONS:
            {
                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.CAPTURE_AUDIO_OUTPUT, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.MEDIA_CONTENT_CONTROL, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.CAPTURE_AUDIO_OUTPUT) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.MEDIA_CONTENT_CONTROL) == PackageManager.PERMISSION_GRANTED){
                        //Log.d(TAG, "sms & location services permission granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                        //Log.d(TAG, "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAPTURE_AUDIO_OUTPUT)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.MEDIA_CONTENT_CONTROL))
                        {
                            showDialogOK("Permission required for this app",
                                    new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }*/
}
