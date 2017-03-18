package com.sunflower.catchtherainbow.AudioClasses;

import android.os.Handler;
import android.util.Log;

import com.sunflower.catchtherainbow.Helper;
import com.sunflower.catchtherainbow.SuperApplication;
import com.un4seen.bass.BASS;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by SuperComputer on 3/6/2017.
 */

// handles importing audio files in background
public class AudioImporter implements Runnable
{
    private static final String LOG_TAG = "Audio Importer";

    private Queue<ImporterQuery> filesToLoad = new LinkedList<>();

    private Project project;

    private AudioImporterListener listener;

    private Handler handler = new Handler();


    private Thread workingThread;
    // is thread running
    private boolean isRunning = false;
    // tells the thread to stop
    private boolean shouldStop = false;

    private static AudioImporter importerInstance = new AudioImporter();

    private AudioImporter(){}

    // singleton instance
    public static AudioImporter getInstance()
    {
        return importerInstance;
    }

    public void setProject(Project project)
    {
        this.project = project;
    }

    public synchronized void addToQueue(ImporterQuery... files)
    {
        for(ImporterQuery path: files)
            filesToLoad.add(path);

        // start new thread in case it's not alive
        if(!isRunning)
            start();
    }

    private void start()
    {
        // create a new thread in case it's not running
        if(!isRunning)
        {
            workingThread = new Thread(this);
            workingThread.start();
        }
    }

    // Don not call this method directly!!!!
    @Override
    public void run()
    {
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                if(listener != null)
                    listener.onBegin(filesToLoad);
            }
        });

        isRunning = true;

        final ArrayList<ImporterQuery> finishedQueries = new ArrayList<>();
        final ArrayList<ImporterQuery> failedQueries = new ArrayList<>();

        ImporterQuery query;
        // go through the queue
        while((query = filesToLoad.poll()) != null)
        {
            String file = query.audioFileInfo.getPath();

            // decoder handle
            int handle = BASS.BASS_StreamCreateFile(file, 0, 0, BASS.BASS_STREAM_DECODE|BASS.BASS_SAMPLE_FLOAT);

            final long len = BASS.BASS_ChannelGetLength(handle, BASS.BASS_POS_BYTE);

            // extract channelHandle info
            BASS.BASS_CHANNELINFO info = new BASS.BASS_CHANNELINFO();
            BASS.BASS_ChannelGetInfo(handle, info);

            // make sure that project directory is created
            Helper.checkDirectory(SuperApplication.getAppDirectory());
            // path to decoded audio file
            //String trackDirectory = Environment.getExternalStorageDirectory().toString() + "/Catch The Rainbow" + "/WaveTrackTest/";
            //boolean created = Helper.createOrRecreateDir(query.destDirectory);

            // no directory
            /*if(!created)
            {
                Log.e(LOG_TAG, "Track directory is in a lot of trouble!");
                failedQueries.add(query);
                continue;
            }*/

            // size in bytes
            int bufferSize = AudioSequence.maxChunkSize; /*1048576*4*/;
            long totalBytesRead = 0;

            // number of files
            int fileCount = 0;

            ArrayList<AudioChunk>audioChunks = new ArrayList<>();

            //WaveTrack track = new WaveTrack("", project.getFileManager());
            //final AudioSequence sequence = new AudioSequence(project.getFileManager(), new AudioInfo(info.freq, info.chans));

            final Clip clip = new Clip(project.getFileManager(), new AudioInfo(info.freq, info.chans));

            // read data piece by piece
            while(totalBytesRead < len)
            {
                ByteBuffer audioData = ByteBuffer.allocateDirect(bufferSize);
                //audioData.order(ByteOrder.LITTLE_ENDIAN); // little-endian byte order
                int bytesRead = BASS.BASS_ChannelGetData(handle, audioData, bufferSize);

                totalBytesRead += bytesRead;

               /* byte buffer[] = new byte[bufferSize/4];
                audioData.get(buffer);

                ByteBuffer resBuff = ByteBuffer.allocate(bufferSize/2);//.wrap(buffer);
                resBuff.put(buffer);*/
                try
                {
                    clip.getSequence().append(audioData, bytesRead/4);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                // increment files count
                fileCount++;

                // final variables for handler
                final ImporterQuery processedQuery = query;
                final long totalBytes = totalBytesRead;

                // notify about progress
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(listener != null)
                            listener.onProgressUpdate(processedQuery, (int) ((float)totalBytes / len * 100), clip);
                    }
                });

            } // while file is not decoded
            Log.i(LOG_TAG, "Audio chunks created: " + audioChunks.size());

            // add the query to the finished list
            finishedQueries.add(query);
        } // while

        isRunning = false;

        // notify about completion
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                if(listener != null)
                    listener.onFinish(finishedQueries, failedQueries);
            }
        });
    }

    public Queue<ImporterQuery> getFilesToLoad()
    {
        return filesToLoad;
    }

    public int getEnqueuedFilesCount()
    {
        return filesToLoad.size();
    }

    public AudioImporterListener getListener()
    {
        return listener;
    }

    public void setListener(AudioImporterListener listener)
    {
        this.listener = listener;
    }

    // callback interface
    public interface AudioImporterListener
    {
        void onBegin(Queue<ImporterQuery> queries);
        // progress in percents(0-100)
        void onProgressUpdate(ImporterQuery query, int progress, Clip clip);
        void onFinish(ArrayList<ImporterQuery> succeededQueries, ArrayList<ImporterQuery> failedQueries);
    }

    public static class ImporterQuery
    {
        public AudioFile audioFileInfo;
        public String destDirectory;

        public ImporterQuery(AudioFile audioFileInfo, String destDirectory)
        {
            this.audioFileInfo = audioFileInfo;
            this.destDirectory = destDirectory;
        }
    }
}
