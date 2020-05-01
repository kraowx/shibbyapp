package io.github.kraowx.shibbyapp.audio;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.tools.HttpRequest;

class AudioPlayer extends AsyncTask<String, Void, Boolean>
{
    private boolean initialized, fileDownloaded;
    private MediaPlayer mediaPlayer;
    private ProgressDialog progressDialog;
    private MainActivity mainActivity;

    public AudioPlayer(ProgressDialog progressDialog, boolean fileDownloaded,
                       MainActivity mainActivity)
    {
        this.progressDialog = progressDialog;
        this.fileDownloaded = fileDownloaded;
        this.mainActivity = mainActivity;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        boolean wakeLock = prefs.getBoolean("wakeLock", false);
        if (wakeLock)
        {
            /* Keep the cpu awake to avoid audio stopping while streaming */
            mediaPlayer.setWakeMode(mainActivity, PowerManager.PARTIAL_WAKE_LOCK);
        }
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
    
    public void setCompletionListener(MediaPlayer.OnCompletionListener listener)
    {
        mediaPlayer.setOnCompletionListener(listener);
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
    protected Boolean doInBackground(final String... strings)
    {
        Boolean prepared = false;

        try
        {
            if (strings[0].contains("patreon"))
            {
                String cookie = mainActivity.getPatreonSessionManager().getCookie();
                Map<String, String> headers = new HashMap<String, String>();
                if (cookie == null)
                {
                    cookie = "";
                }
                headers.put("Cookie", cookie);
                mediaPlayer.setDataSource(mainActivity, Uri.parse(strings[0]), headers);
            }
            else
            {
                mediaPlayer.setDataSource(strings[0]);
            }
            mediaPlayer.prepare();
            prepared = true;
        }
        catch (Exception e)
        {
            showErrorOnUI(strings[0]);
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
            mainActivity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    progressDialog.setMessage("Buffering...");
                    progressDialog.show();
                }
            });
        }
    }
    
    private void showErrorOnUI(final String link)
    {
        new Thread()
        {
            @Override
            public void run()
            {
                if (link.contains("patreon"))
                {
                    SharedPreferences prefs = PreferenceManager
                            .getDefaultSharedPreferences(mainActivity);
                    final String patreonEmail = prefs.getString(
                            "patreonEmail", null);
                    final String patreonPassword = prefs.getString(
                            "patreonPassword", null);
                    int code = mainActivity.getPatreonSessionManager()
                            .verifyCredentials(patreonEmail, patreonPassword);
                    if (code == 1)
                    {
                        showMessageDialog("Verification Failed",
                                "Your Patreon account is either invalid or " +
                                        "you do not have permission to access to this file.");
                    }
                    else if (code == 2)
                    {
                        showMessageDialog("Email Confirmation",
                                "Before this file can be streamed you must " +
                                        "first confirm this device/location by clicking " +
                                        "the link in the email Patreon just sent you.");
                    }
                    else if (code == 3)
                    {
                        showMessageDialog("Too Many Requests",
                                "Too many requests have been sent to Patreon. Access has " +
                                        "been restricted for 10 minutes");
                    }
                }
            }
        }.start();
    }
    
    private void showMessageDialog(final String title, final String message)
    {
        mainActivity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(mainActivity);
                boolean darkModeEnabled = prefs.getBoolean("darkMode", false);
                AlertDialog.Builder builder;
                if (darkModeEnabled)
                {
                    builder = new AlertDialog.Builder(mainActivity, R.style.DialogThemeDark);
                }
                else
                {
                    builder = new AlertDialog.Builder(mainActivity);
                }
                builder.setTitle(title)
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        });
    }
}
