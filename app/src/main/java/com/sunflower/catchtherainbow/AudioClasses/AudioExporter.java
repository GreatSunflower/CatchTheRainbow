package com.sunflower.catchtherainbow.AudioClasses;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.un4seen.bass.BASS;
import com.un4seen.bass.BASSenc;
import com.un4seen.bass.BASSenc_OGG;
import com.un4seen.bass.BASSenc_OPUS;
import com.un4seen.bass.BASSmix;

import java.nio.ByteBuffer;
import java.util.Locale;

// Saves effect samples to track
public class AudioExporter extends AsyncTask<Void, Integer, Void>
{
    private boolean running;
    private ProgressDialog progressDialog;
    private Context context;
    private String message;
    private String location;

    Project project;
    String name, album, year, format;

    public AudioExporter(Context context, String message, Project project, String name, String album, String year, String format, String location)
    {
        this.context = context;
        this.message = message;
        this.project = project;
        this.name = name;
        this.album = album;
        this.year = year;
        this.format = format;
        this.location = location;
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        AudioInfo audioInfo = project.getProjectAudioInfo();
        int sampleSize = project.getProjectAudioInfo().getFormat().getSampleSize();

        int mixer = BASSmix.BASS_Mixer_StreamCreate(audioInfo.getSampleRate(), audioInfo.channels, BASS.BASS_SAMPLE_FLOAT|BASS.BASS_STREAM_DECODE);

        long tmpLen = Long.MIN_VALUE;
        for(WaveTrack track: project.getTracks())
        {
            TrackInfo trackInfo = new TrackInfo(0, track, 0);
            int channel = BASS.BASS_StreamCreate(audioInfo.getSampleRate(), audioInfo.getChannels(),
                    BASS.BASS_SAMPLE_FLOAT|BASS.BASS_STREAM_DECODE, new StreamProc(), trackInfo);
            trackInfo.setChannel(channel);

            BASSmix.BASS_Mixer_StreamAddChannel(mixer, channel, 0);

            if(track.getEndSample() * sampleSize > tmpLen)
                tmpLen = track.getEndSample() * sampleSize;

           /* int enc = BASSenc.BASS_Encode_Start(channel, "flac.exe n-f --sig=signed --force-raw-format --endian=little --sample-rate=44100 --channels=2 --bps=16 - -o test.flac", BASSenc.BASS_ENCODE_NOHEAD, null, 0);
            Log.d("ggg", enc+"");*/
            /*int enc = BASSenc.BASS_Encode_Start(channel, location + "/" + name + ".wav", BASSenc.BASS_ENCODE_PCM|BASSenc.BASS_ENCODE_PAUSE|BASSenc.BASS_ENCODE_QUEUE, null, null);
           /* Log.d("ggg", enc+"");*/
        }

        // length in bytes
        final long len = tmpLen; //BASS.BASS_ChannelGetLength(channel, BASS.BASS_POS_BYTE);

        String cmd = "";
        int encoder = 0;

        if(format.equalsIgnoreCase("ogg"))
        {
            cmd = String.format(Locale.ENGLISH,
                    "oggenc -R %d -C %d -t \"%s\" -l \"%s\"",
                    audioInfo.sampleRate, audioInfo.channels, name, album);
            encoder = BASSenc_OGG.BASS_Encode_OGG_StartFile(mixer, cmd, BASSenc.BASS_ENCODE_PAUSE | BASSenc.BASS_ENCODE_QUEUE, location + "/" + name + ".ogg");
        }
        else if(format.equalsIgnoreCase("opus"))
        {
            cmd = String.format(Locale.ENGLISH,
                    "opusenc --title \"%s\" --album \"%s\"",
                     name, album);
            encoder = BASSenc_OPUS.BASS_Encode_OPUS_StartFile(mixer, cmd, BASSenc.BASS_ENCODE_PAUSE | BASSenc.BASS_ENCODE_QUEUE, location + "/" + name + ".opus");
        }
        else if(format.equalsIgnoreCase("wav") || format.equalsIgnoreCase("aiff"))
        {
            cmd = location + "/" + name + "." + format.toLowerCase(); /*"--sample-rate="+ audioInfo.sampleRate +" --channels=" + audioInfo.channels + " -o " +*/
                   /* String.format(Locale.ENGLISH,
                            "-R %d -C %d -t \"%s\" -l \"%s\" %s",
                            audioInfo.sampleRate, audioInfo.channels, name, album, location + "/" + name + "." + format.toLowerCase());*/
            int flags = BASSenc.BASS_ENCODE_PAUSE | BASSenc.BASS_ENCODE_QUEUE |BASSenc.BASS_ENCODE_PCM;
            if (format.equalsIgnoreCase("wav"))
                flags |= BASSenc.BASS_ENCODE_RF64;
            else flags |= BASSenc.BASS_ENCODE_AIFF;
            encoder = BASSenc.BASS_Encode_Start(mixer, cmd/*format + " -" + name + "." + format*/, flags, null, null);
        }
        if(encoder == 0) return null;

        // size in bytes
        int bufferSize = AudioSequence.maxChunkSize/2;
        int totalBytesRead = 0;

        // read data piece by piece
        while(true)
        {
            ByteBuffer audioData = ByteBuffer.allocateDirect(bufferSize);
            int bytesRead = BASS.BASS_ChannelGetData(mixer, audioData, bufferSize);

            if(bytesRead <= 0)
            {
                publishProgress(100);
                break;
            }

            totalBytesRead += bytesRead;

            BASSenc.BASS_Encode_Write(encoder, audioData, bytesRead);

            publishProgress((int) ((float)totalBytesRead / len * 100));
        }

        BASSenc.BASS_Encode_Stop(encoder);

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values)
    {
        super.onProgressUpdate(values);
        progressDialog.setProgress(values[0]);
        //progressDialog.setMessage(String.valueOf(values[0]));
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        running = true;

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(message);
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
       /* progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                running = false;
            }
        });*/
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        super.onPostExecute(aVoid);

        progressDialog.dismiss();
    }
}

