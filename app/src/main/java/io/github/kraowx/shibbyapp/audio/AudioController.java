package io.github.kraowx.shibbyapp.audio;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import java.util.List;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.models.ShibbyFile;

public class AudioController
{
    private boolean audioServiceBound;
    
    private AudioPlayerDialog dialog;
    private MainActivity mainActivity;
    private AudioPlayerService audioPlayerService;
    private ServiceConnection serviceConnection;

    public AudioController(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
        dialog = new AudioPlayerDialog(mainActivity);
    }
    
    public boolean isServiceBound()
    {
        return audioServiceBound;
    }
    
    public ServiceConnection getServiceConnection()
    {
        return serviceConnection;
    }
    
    public AudioPlayerService getService()
    {
        return audioPlayerService;
    }

    public void loadFile(final ShibbyFile file, final String playlist)
    {
        if (!audioServiceBound)
        {
            Intent playerIntent = new Intent(mainActivity,
                    AudioPlayerService.class);
            playerIntent.putExtra("media", file.toJSON().toString());
            playerIntent.setAction(AudioPlayerService.ACTION_CREATE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                mainActivity.startForegroundService(playerIntent);
            }
            else
            {
                mainActivity.startService(playerIntent);
            }
            serviceConnection = new ServiceConnection()
            {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service)
                {
                    AudioPlayerService.LocalBinder binder = (AudioPlayerService.LocalBinder)service;
                    audioPlayerService = binder.getService();
                    audioPlayerService.setPlayerListener(new AudioPlayerService.AudioPlayerServiceListener()
                    {
                        @Override
                        public void audioServiceStopping()
                        {
                            dialog.stopTimer();
                        }
                    });
                    audioServiceBound = true;
                    dialog.setService(audioPlayerService);
                    dialog.loadFile(file, playlist);
                }
    
                @Override
                public void onServiceDisconnected(ComponentName name)
                {
                    audioServiceBound = false;
                }
            };
            mainActivity.bindService(playerIntent, serviceConnection,
                    Context.BIND_AUTO_CREATE);
        }
        else
        {
            audioPlayerService.loadFile(file, playlist);
            dialog.loadFile(file, playlist);
        }
    }

    public void setQueue(List<ShibbyFile> queue, boolean isPlaylist)
    {
        dialog.setQueue(queue, isPlaylist);
    }

    public boolean isVisible()
    {
        return dialog.isShowing();
    }

    public void toggleVisible()
    {
        if (dialog.isShowing())
        {
//            dialog.stopTimer();
            dialog.hide();
        }
        else
        {
//            dialog.startTimer();
            dialog.show();
        }
    }

    public void setVisible(boolean visible)
    {
        if (visible && !dialog.isShowing())
        {
            //dialog.startTimer();
            dialog.show();
        }
        else if (dialog.isShowing())
        {
            dialog.stopTimer();
            dialog.hide();
        }
    }
    
    private void destroyOldPlayer()
    {
        if (audioPlayerService != null && audioPlayerService.playerIsInitialized())
        {
            audioPlayerService.destroyPlayer();
        }
    }
}
