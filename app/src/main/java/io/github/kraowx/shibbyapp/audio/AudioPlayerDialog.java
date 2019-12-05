package io.github.kraowx.shibbyapp.audio;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.models.ShibbyFile;
import io.github.kraowx.shibbyapp.tools.AudioDownloadManager;

public class AudioPlayerDialog extends Dialog
{
    private boolean fileDownloaded,
            seeking, queueIsPlaylist;
    private ShibbyFile activeFile;
    private List<ShibbyFile> queue;
    private AudioPlayer audioPlayer;
    private Timer timer;

    private TextView title, txtElapsedTime, txtRemainingTime;
    private ImageButton btnRewind, btnPlayPause, btnFastForward,
            btnDownload, btnRepeat;
    private SeekBar progressBar;
    private ProgressDialog progressDialog;
    private MainActivity mainActivity;

    public AudioPlayerDialog(MainActivity mainActivity)
    {
        super(mainActivity);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.audio_player_dialog);
        this.mainActivity = mainActivity;
        initUI();
    }

    public void setQueue(List<ShibbyFile> queue, boolean isPlaylist)
    {
        this.queue = queue;
        queueIsPlaylist = isPlaylist;
    }

    public void startTimer()
    {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                if (audioPlayer != null)
                {
                    if (audioPlayer.getFileDuration() != -1 &&
                            progressBar.getMax() != audioPlayer.getFileDuration())
                    {
                        progressBar.setMax(audioPlayer.getFileDuration());
                    }
                    if (audioPlayer.isPlaying())
                    {
                        if (!seeking)
                        {
                            progressBar.setProgress(audioPlayer.getPosition());
                        }
                    }
                    // player has reached the end of the audio file
                    else if (audioPlayer.isInitialized())
                    {
                        // there is a file next in the queue
                        if (progressBar.getProgress()+1000 >= audioPlayer.getFileDuration() &&
                                queue != null && queue.indexOf(activeFile) < queue.size()-1 &&
                                autoplayAllowed())
                        {
                            mainActivity.runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    loadFile(queue.get(queue.indexOf(activeFile)+1));
                                    playAudio();
                                    btnPlayPause.post(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            btnPlayPause.setImageResource(
                                                    R.drawable.ic_pause_circle);
                                        }
                                    });
                                }
                            });
                        }
                        // the player is not looping, so stop the player
                        if (!audioPlayer.isLooping())
                        {
                            btnPlayPause.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    btnPlayPause.setImageResource(
                                            R.drawable.ic_play_circle);
                                }
                            });
                        }
                    }
                }
            }
        }, 0, 1000);
    }

    public void stopTimer()
    {
        timer.cancel();
        timer.purge();
    }

    public void loadFile(final ShibbyFile file)
    {
        this.activeFile = file;
        title.post(new Runnable()
        {
            @Override
            public void run()
            {
                title.setText(file != null ? file.getName() : "No file selected");
            }
        });
        txtElapsedTime.post(new Runnable()
        {
            @Override
            public void run()
            {
                txtElapsedTime.setText(formatTime(0));
            }
        });
        txtRemainingTime.post(new Runnable()
        {
            @Override
            public void run()
            {
                txtRemainingTime.setText(formatTime(0));
            }
        });
        progressBar.post(new Runnable()
        {
            @Override
            public void run()
            {
                progressBar.setMax(10000);
                progressBar.setProgress(0);
            }
        });
        if (audioPlayer != null && audioPlayer.isPlaying())
        {
            audioPlayer.stopAudio();
            btnPlayPause.post(new Runnable()
            {
                @Override
                public void run()
                {
                    btnPlayPause.setImageResource(R.drawable.ic_play_circle);
                }
            });
        }
        fileDownloaded = file != null ? AudioDownloadManager
                .fileIsDownloaded(mainActivity, file) : false;
        if (fileDownloaded)
        {
            btnDownload.post(new Runnable()
            {
                @Override
                public void run()
                {
                    btnDownload.setColorFilter(ContextCompat.getColor(
                            getContext(), R.color.colorAccent));
                }
            });
        }
        else if (mainActivity.getDownloadManager().isDownloadingFile(file))
        {
            btnDownload.post(new Runnable()
            {
                @Override
                public void run()
                {
                    btnDownload.setColorFilter(ContextCompat.getColor(
                            getContext(), R.color.redAccent));
                }
            });
        }
        else
        {
            btnDownload.post(new Runnable()
            {
                @Override
                public void run()
                {
                    btnDownload.setColorFilter(null);
                }
            });
        }
        if (file != null)
        {
            audioPlayer = new AudioPlayer(progressDialog, fileDownloaded);
        }
        else
        {
            audioPlayer = null;
        }
    }

    private void initUI()
    {
        progressDialog = new ProgressDialog(getContext());

        title = findViewById(R.id.txtAudioTitle);
        if (activeFile == null)
        {
            title.setText("No file selected");
        }
        btnDownload = findViewById(R.id.btnPlayerDownload);
        btnDownload.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (activeFile != null)
                {
                    if (!AudioDownloadManager.fileIsDownloaded(mainActivity, activeFile))
                    {
                        mainActivity.getDownloadManager().downloadFile(activeFile, btnDownload);
                        btnDownload.setColorFilter(ContextCompat.getColor(
                                getContext(), R.color.redAccent));
                    }
                    else if (mainActivity.getDownloadManager().isDownloadingFile(activeFile))
                    {
                        if (mainActivity.getDownloadManager().cancelDownload(activeFile))
                        {
                            btnDownload.setColorFilter(null);
                            Toast.makeText(mainActivity, "Download cancelled",
                                    Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toast.makeText(mainActivity, "Failed to cancel download",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        new AlertDialog.Builder(mainActivity)
                                .setTitle("Delete download")
                                .setMessage("Are you sure you want to delete this file?")
                                .setPositiveButton(android.R.string.yes,
                                        new DialogInterface.OnClickListener()
                                        {
                                            public void onClick(DialogInterface dialog, int which)
                                            {
                                                AudioDownloadManager.deleteFile(mainActivity, activeFile);
                                                btnDownload.setColorFilter(null);
                                                loadFile(null);
                                            }
                                        })
                                .setNegativeButton(android.R.string.no, null)
                                .setIcon(R.drawable.ic_warning)
                                .show();
                    }
                }
                else
                {
                    Toast.makeText(getContext(), "Error: No file selected",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        btnRewind = findViewById(R.id.btnRewind);
        btnRewind.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (queue != null)
                {
                    int index = queue.indexOf(activeFile);
                    if (index > 0)
                    {
                        boolean isPlaying = audioPlayer.isPlaying();
                        ShibbyFile newFile = queue.get(index - 1);
                        loadFile(newFile);
                        if (isPlaying)
                        {
                            playAudio();
                            btnPlayPause.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    btnPlayPause.setImageResource(
                                            R.drawable.ic_pause_circle);
                                }
                            });
                        }
                    }
                    else
                    {
                        Toast.makeText(getContext(),
                                "Beginning of playlist reached",
                                Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Toast.makeText(getContext(),
                            "No queue selected",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnPlayPause.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (AudioPlayerDialog.this.activeFile != null)
                {
                    if (audioPlayer != null && audioPlayer.isPlaying())
                    {
                        audioPlayer.pauseAudio();
                        btnPlayPause.setImageResource(R.drawable.ic_play_circle);
                    }
                    else if (audioPlayer != null)
                    {
                        playAudio();
                        btnPlayPause.setImageResource(R.drawable.ic_pause_circle);
                    }
                }
            }
        });
        btnFastForward = findViewById(R.id.btnFastForward);
        btnFastForward.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (queue != null)
                {
                    int index = queue.indexOf(activeFile);
                    if (index < queue.size() - 1)
                    {
                        boolean isPlaying = audioPlayer.isPlaying();
                        ShibbyFile newFile = queue.get(index + 1);
                        loadFile(newFile);
                        if (isPlaying)
                        {
                            playAudio();
                            btnPlayPause.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    btnPlayPause.setImageResource(
                                            R.drawable.ic_pause_circle);
                                }
                            });
                        }
                    }
                    else
                    {
                        Toast.makeText(getContext(),
                                "End of playlist reached",
                                Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Toast.makeText(getContext(),
                            "No queue selected",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        btnRepeat = findViewById(R.id.btnPlayerRepeat);
        btnRepeat.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (audioPlayer != null)
                {
                    boolean looping = audioPlayer.isLooping();
                    if (looping)
                    {
                        btnRepeat.setColorFilter(null);
                    }
                    else
                    {
                        btnRepeat.setColorFilter(ContextCompat.getColor(getContext(),
                                R.color.colorAccent));
                    }
                    audioPlayer.setLooping(!looping);
                }
            }
        });
        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(10000);
        progressBar.setProgress(0);
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                if (audioPlayer != null && audioPlayer.getFileDuration() > 0)
                {
                    if (seeking)
                    {
                        txtElapsedTime.setText(formatTime(progress));
                        txtRemainingTime.setText(formatTime(
                                audioPlayer.getFileDuration()-progress));
                    }
                    else
                    {
                        txtElapsedTime.setText(formatTime(progress));
                        txtRemainingTime.setText(formatTime(
                                audioPlayer.getFileDuration() - audioPlayer.getPosition()));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
                seeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                seeking = false;
                if (audioPlayer != null && audioPlayer.isInitialized() &&
                        audioPlayer.getFileDuration() > 0)
                {
                    audioPlayer.seekTo(seekBar.getProgress());
                }
            }
        });
        txtElapsedTime = findViewById(R.id.txtElapsedTime);
        txtRemainingTime = findViewById(R.id.txtRemainingTime);
    }

    private void playAudio()
    {
        if (audioPlayer.isInitialized())
        {
            if (audioPlayer.getPosition() != audioPlayer.getFileDuration())
            {
                audioPlayer.resumeAudio();
            }
            else
            {
                audioPlayer.seekTo(0);
            }
        }
        else if (fileDownloaded)
        {
            audioPlayer.execute(AudioDownloadManager
                    .getFileLocation(mainActivity, activeFile)
                    .getAbsolutePath());
        }
        else
        {
            audioPlayer.execute(AudioPlayerDialog.this.activeFile.getLink());
        }
    }

    private boolean autoplayAllowed()
    {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mainActivity);
        int autoplay = prefs.getInt("autoplay", 1);
        return autoplay == 0 || (autoplay == 1 && queueIsPlaylist);
    }

    private String formatTime(int time)
    {
        int hours   = (int)((time / (1000*60*60)) % 24);
        int minutes = (int)((time / (1000*60)) % 60);
        int seconds = (int)(time / 1000) % 60;
        String hoursStr = String.format("%02d", hours);
        String minutesStr = String.format("%02d", minutes);
        String secondsStr = String.format("%02d", seconds);
        if (hours > 0)
        {
            return hoursStr + ":" + minutesStr + ":" + secondsStr;
        }
        else
        {
            return minutesStr + ":" + secondsStr;
        }
    }
}
