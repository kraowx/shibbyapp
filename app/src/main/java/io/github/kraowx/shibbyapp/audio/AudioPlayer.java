package io.github.kraowx.shibbyapp.audio;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
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
//    private MainActivity mainActivity;
    private Context context;
    private SharedPreferences prefs;
    
    public AudioPlayer(ProgressDialog progressDialog, boolean fileDownloaded, Context context)
    {
        this.progressDialog = progressDialog;
        this.fileDownloaded = fileDownloaded;
        this.context = context;
        mediaPlayer = new MediaPlayer();
//        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build());
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean wakeLock = prefs.getBoolean("wakeLock", false);
        if (wakeLock)
        {
            /* Keep the cpu awake to avoid audio stopping while streaming */
            mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
        }
    }

    public boolean isPlaying()
    {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public boolean isInitialized()
    {
        return initialized;
    }

    public MediaPlayer getMediaPlayer()
    {
        return mediaPlayer;
    }
    
    public void setOnPreparedListener(MediaPlayer.OnPreparedListener listener)
    {
        mediaPlayer.setOnPreparedListener(listener);
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
    
    public void destroy()
    {
        if (mediaPlayer != null)
        {
            mediaPlayer.release();
            mediaPlayer = null;
            initialized = false;
        }
    }

    public int getPosition()
    {
        if (initialized)
        {
            try
            {
                return mediaPlayer.getCurrentPosition();
            }
            catch (IllegalStateException e)
            {
                return -1;
            }
        }
        return -1;
    }

    public int getFileDuration()
    {
        if (initialized)
        {
            try
            {
                return mediaPlayer.getDuration();
            }
            catch (IllegalStateException e)
            {
                return -1;
            }
        }
        return -2;
    }

    public boolean isLooping()
    {
        return mediaPlayer != null && mediaPlayer.isLooping();
    }

    public void setLooping(boolean loop)
    {
        if (mediaPlayer != null)
        {
            mediaPlayer.setLooping(loop);
        }
    }

    @Override
    protected Boolean doInBackground(final String... strings)
    {
        Boolean prepared = false;
        try
        {
//            if (strings[0].contains("patreon"))
//            {
//                String cookie = prefs.getString("shibbydexAuthCookie", null);
//                Map<String, String> headers = new HashMap<String, String>();
//                if (cookie == null)
//                {
//                    cookie = "";
//                }
//                headers.put("Cookie", cookie);
//                mediaPlayer.setDataSource(context, Uri.parse(strings[0]), headers);
//            }
//            else
//            {
//                mediaPlayer.setDataSource(strings[0]);
//            }
            String cookie = prefs.getString("shibbydexAuthCookie", null);
            Map<String, String> headers = new HashMap<String, String>();
            if (cookie == null)
            {
                cookie = "";
            }
            headers.put("Cookie", cookie);
            mediaPlayer.setDataSource(context, Uri.parse(strings[0]), headers);
            mediaPlayer.prepare();
            prepared = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
//            showErrorOnUI(strings[0]);
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
            try
            {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        progressDialog.setMessage("Buffering...");
                        progressDialog.show();
                    }
                });
            }
            catch (Exception e)
            {
            
            }
        }
    }
    
//    private void showErrorOnUI(final String link)
//    {
//        new Thread()
//        {
//            @Override
//            public void run()
//            {
//                if (link.contains("patreon"))
//                {
//                    SharedPreferences prefs = PreferenceManager
//                            .getDefaultSharedPreferences(context);
//                    final String patreonEmail = prefs.getString(
//                            "patreonEmail", null);
//                    final String patreonPassword = prefs.getString(
//                            "patreonPassword", null);
//                    int code = mainActivity.getPatreonSessionManager()
//                            .verifyCredentials(patreonEmail, patreonPassword);
//                    if (code == 1)
//                    {
//                        showMessageDialog("Verification Failed",
//                                "Your Patreon account is either invalid or " +
//                                        "you do not have permission to access to this file.");
//                    }
//                    else if (code == 2)
//                    {
//                        showMessageDialog("Email Confirmation",
//                                "Before this file can be streamed you must " +
//                                        "first confirm this device/location by clicking " +
//                                        "the link in the email Patreon just sent you.");
//                    }
//                    else if (code == 3)
//                    {
//                        showMessageDialog("Too Many Requests",
//                                "Too many requests have been sent to Patreon. Access has " +
//                                        "been restricted for 10 minutes");
//                    }
//                }
//            }
//        }.start();
//    }
//
//    private void showMessageDialog(final String title, final String message)
//    {
//        mainActivity.runOnUiThread(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                SharedPreferences prefs = PreferenceManager
//                        .getDefaultSharedPreferences(context);
//                boolean darkModeEnabled = prefs.getBoolean("darkMode", false);
//                AlertDialog.Builder builder;
//                if (darkModeEnabled)
//                {
//                    builder = new AlertDialog.Builder(context, R.style.DialogThemeDark);
//                }
//                else
//                {
//                    builder = new AlertDialog.Builder(context);
//                }
//                builder.setTitle(title)
//                        .setMessage(message)
//                        .setCancelable(false)
//                        .setPositiveButton(android.R.string.ok, null)
//                        .show();
//            }
//        });
//    }
}
