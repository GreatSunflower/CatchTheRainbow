package com.sunflower.catchtherainbow.AudioClasses;

import android.os.Handler;

import com.sunflower.catchtherainbow.Helper;
import com.sunflower.catchtherainbow.SuperApplication;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by SuperComputer on 3/16/2017.
 * Main project class
 */

public class Project implements Serializable
{
    private String name;

    private transient ArrayList<ProjectListener> listeners = new ArrayList<>();

    private ArrayList<WaveTrack> tracks = new ArrayList<>();

    private FileManager fileManager;

    // audio info
    private AudioInfo projectAudioInfo = new AudioInfo(44100, 2);

    private Project(String name, ProjectListener listener)
    {
        this.name = name;
        listeners.add(listener);
    }

    public String getName()
    {
        return name;
    }

    // creates a new project
    // if something is wrong with the name null will be returned
    public static Project createNewProject(String name, ProjectListener listener)
    {
        if(name == null || name.equals(""))
            return null;

        Project project = new Project(name, listener);
        project.fileManager = new FileManager(project.getProjectFolderLocation() + "/Samples/");
        // create project and samples directories
        Helper.createOrRecreateDir(project.getProjectFolderLocation());
        Helper.createOrRecreateDir(project.getSamplesDirectory());

        project.updateProjectFile();

        if(listener != null)
        {
            listener.onCreate(project);
        }

        return project;
    }


    // creates a new project
    // if something is wrong with the name null will be returned
    public static Project openProject(String name, ProjectListener listener) throws IOException, ClassNotFoundException
    {
        if(name == null || name.equals(""))
            return null;

        Project project = new Project(name, listener);
        // create project and samples directories

        String path = project.getProjectFolderLocation() + "/" + name + ".ctr";

        // buffer for outputting in ctr format
        ObjectInputStream objectInputStreamStream = null;

        objectInputStreamStream = new ObjectInputStream(new FileInputStream(path));
        ProjectHeader header = (ProjectHeader)objectInputStreamStream.readObject();

        project.tracks = header.tracks;
        project.fileManager = header.manager;
        project.projectAudioInfo = header.info;

        // close the steam
        objectInputStreamStream.close();

        Helper.checkDirectory(project.getSamplesDirectory());

        for(WaveTrack track: project.tracks)
            track.addListener(project.trackListener);

        // notify listener
        if(listener != null)
        {
            listener.onCreate(project);
        }

        return project;
    }

    // creates a new project
    // if something is wrong with the name null will be returned
    public static void openProjectAsync(final String name, final ProjectListener listener)
    {
        if(name == null || name.equals(""))
            return;

        final Handler handler = new Handler();

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                final Project project = new Project(name, listener);

                try
                {
                    // create project and samples directories
                    String path = project.getProjectFolderLocation() + "/" + name + ".ctr";

                    // buffer for outputting in ctr format
                    ObjectInputStream objectInputStreamStream = null;

                    objectInputStreamStream = new ObjectInputStream(new FileInputStream(path));

                    ProjectHeader header = (ProjectHeader)objectInputStreamStream.readObject();

                    project.tracks = header.tracks;
                    project.fileManager = header.manager;
                    project.projectAudioInfo = header.info;

                    // close the steam
                    objectInputStreamStream.close();

                    Helper.checkDirectory(project.getSamplesDirectory());

                    for(WaveTrack track: project.tracks)
                        track.addListener(project.trackListener);

                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // notify listener
                            if(listener != null)
                            {
                                listener.onCreate(project);
                            }
                        }
                    });
                }
                catch (IOException | ClassNotFoundException e)
                {
                    e.printStackTrace();
                    // notify listener
                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // notify listener
                            if(listener != null)
                            {
                                listener.onOpenError(project);
                            }
                        }
                    });
                }

            }
        }).start();
    }

    public void updateProjectFile()
    {
        String path = SuperApplication.getAppDirectory() + "/" + name + "/" + name + ".ctr";

        // buffer for outputting in ctr format
        ObjectOutputStream outputStream = null;
        try
        {
            outputStream = new ObjectOutputStream(new FileOutputStream(path));
            ProjectHeader header = new ProjectHeader(name, tracks, fileManager, projectAudioInfo);
            // write header
            outputStream.writeObject(header);

            // write all of the data
            outputStream.flush();

            // close the steam
            outputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public String getProjectFolderLocation()
    {
        return SuperApplication.getAppDirectory() + "/" + name;
    }

    public String getSamplesDirectory()
    {
        return this.getProjectFolderLocation() + "/Samples/";
    }

    public FileManager getFileManager()
    {
        return fileManager;
    }

    public AudioInfo getProjectAudioInfo()
    {
        return projectAudioInfo;
    }

    // ----- Tracks-------
    public void addTrack(WaveTrack track)
    {
        tracks.add(track);

        track.addListener(trackListener);

        updateProjectFile();

        for(ProjectListener listener: listeners)
            listener.onTrackAdded(this, track);
    }

    public void removeTrack(WaveTrack track)
    {
        track.removeListener(trackListener);

        tracks.remove(track);

        updateProjectFile();

        for(ProjectListener listener: listeners)
            listener.onTrackRemoved(this, track);
    }

    public void clearTracks()
    {
        for(WaveTrack track: tracks)
            track.removeListener(trackListener);

        tracks.clear();

        updateProjectFile();

        for(ProjectListener listener: listeners)
            listener.onUpdate(this);
    }

    public ArrayList<WaveTrack> getTracks()
    {
        return tracks;
    }

    // ----- Tracks-End------

    private WaveTrack.WaveTrackListener trackListener = new WaveTrack.WaveTrackListener()
    {
        @Override
        public void onPropertyUpdated(WaveTrack track)
        {
            //updateProjectFile();
        }
    };

    // ----- Listener -------
    public ArrayList<ProjectListener> getListeners()
    {
        return listeners;
    }

    public void addListener(ProjectListener listener)
    {
        if(listener != null)
            listeners.add(listener);
    }
    public void removeListener(ProjectListener listener)
    {
        if(listener != null)
            listeners.remove(listener);
    }
    // ----- listener end-----


    public interface ProjectListener
    {
        void onCreate(Project project);
        void onOpenError(Project project);
        void onUpdate(Project project);
        void onTrackRemoved(Project project, WaveTrack track);
        void onTrackAdded(Project project, WaveTrack track);
        void onClose(Project project);
    }


}

class ProjectHeader implements Serializable
{
    String name;
    ArrayList<WaveTrack> tracks;
    FileManager manager;
    AudioInfo info;

    public ProjectHeader(){}

    public ProjectHeader(String name, ArrayList<WaveTrack> tracks, FileManager manager, AudioInfo info)
    {
        this.name = name;
        this.tracks = tracks;
        this.manager = manager;
        this.info = info;
    }
}