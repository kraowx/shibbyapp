package io.github.kraowx.shibbyapp.audio;

import android.app.ProgressDialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;

class AudioPlayer extends AsyncTask<String, Void, Boolean>
{
    private boolean initialized, fileDownloaded;
    private MediaPlayer mediaPlayer;
    private ProgressDialog progressDialog;

    public AudioPlayer(ProgressDialog progressDialog, boolean fileDownloaded)
    {
        this.progressDialog = progressDialog;
        this.fileDownloaded = fileDownloaded;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public boolean isPlaying()
    {
        return mediaPlayer.isPlaying();
    }

    public boolean isInitialized()
    {
        return initialized;
    }

    public MediaPlayer getMediaPlayer()
    {
        return mediaPlayer;
    }

    public void seekTo(int millis)
    {
        if (initialized)
        {
            mediaPlayer.seekTo(millis);
        }
    }

    public void stopAudio()
    {
        if (isPlaying())
        {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
    }

    public void pauseAudio()
    {
        if (isPlaying())
        {
            mediaPlayer.pause();
        }
    }

    public void resumeAudio()
    {
        if (!isPlaying())
        {
            mediaPlayer.start();
        }
    }

    public int getPosition()
    {
        if (initialized)
        {
            return mediaPlayer.getCurrentPosition();
        }
        return -1;
    }

    public int getFileDuration()
    {
        if (initialized)
        {
            return mediaPlayer.getDuration();
        }
        return -2;
    }

    public boolean isLooping()
    {
        return mediaPlayer.isLooping();
    }

    public void setLooping(boolean loop)
    {
        mediaPlayer.setLooping(loop);
    }

    @Override
    protected Boolean doInBackground(String... strings)
    {
        Boolean prepared = false;

        try
        {
            mediaPlayer.setDataSource(strings[0]);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
            {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer)
                {
                    //mediaPlayer.stop();
                }
            });

            mediaPlayer.prepare();
            prepared = true;

        }
        catch (Exception e)
        {
            e.printStackTrace();
            prepared = false;
        }

        return prepared;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean)
    {
        super.onPostExecute(aBoolean);

        if (progressDialog.isShowing())
        {
            progressDialog.cancel();
        }

        mediaPlayer.start();
        initialized = true;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();

        if (!fileDownloaded)
        {
            progressDialog.setMessage("Buffering...");
            progressDialog.show();
        }
    }
}
