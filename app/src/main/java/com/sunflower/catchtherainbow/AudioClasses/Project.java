package com.sunflower.catchtherainbow.AudioClasses;

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

    private ProjectListener listener;

    private ArrayList<WaveTrack> tracks = new ArrayList<>();

    private FileManager fileManager;

    // audio info
    private AudioInfo projectAudioInfo = new AudioInfo(44100, 2);

    private Project(String name, ProjectListener listener)
    {
        this.name = name;
        this.listener = listener;
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

        Helper.createOrRecreateDir(project.getSamplesDirectory());

        // notify listener
        if(listener != null)
        {
            listener.onCreate(project);
        }

        return project;
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

        updateProjectFile();

        if(listener != null)
            listener.onUpdate(this);
    }

    public void removeTrack(WaveTrack track)
    {
        tracks.remove(track);

        updateProjectFile();

        if(listener != null)
            listener.onUpdate(this);
    }

    public void clearTracks()
    {
        tracks.clear();

        updateProjectFile();

        if(listener != null)
            listener.onUpdate(this);
    }

    public ArrayList<WaveTrack> getTracks()
    {
        return tracks;
    }

    // ----- Tracks-End------

    // ----- Listener -------
    public ProjectListener getListener()
    {
        return listener;
    }

    public void setListener(ProjectListener listener)
    {
        this.listener = listener;
    }
    // ----- listener end-----


    public interface ProjectListener
    {
        void onCreate(Project project);
        void onUpdate(Project project);
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