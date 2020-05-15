package io.github.kraowx.shibbyapp.audio;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.models.Hotspot;
import io.github.kraowx.shibbyapp.models.HotspotArray;
import io.github.kraowx.shibbyapp.models.ShibbyFile;
import io.github.kraowx.shibbyapp.tools.AudioDownloadManager;
import io.github.kraowx.shibbyapp.tools.PlayCountManager;
import io.github.kraowx.shibbyapp.ui.dialog.DurationPickerDialog;
import mobi.upod.timedurationpicker.TimeDurationPicker;

public class AudioPlayerDialog extends Dialog implements MediaPlayer.OnCompletionListener
{
    private final int NO_DELAY = -1;
    private final int LOOP_INFINITE = -1;
    private final int NO_LOOP = 0;
    private final int MAX_LOOPS = 100;
    private final int MIN_LOOPS = 0;
    
    private boolean fileDownloaded,
            seeking, queueIsPlaylist,
            timerRunning;
    private int delayTime, setDelay,
            loop, audioVibrationOffset;
    private long fileDuration;
    private ShibbyFile activeFile;
    private List<ShibbyFile> queue;
    private List<HotspotArray> hotspots;
    private AudioPlayer audioPlayer;
    private Timer timer, vibrationTimer;
    private SharedPreferences prefs;
    private Vibrator vibrator;
    private PlayCountIncrementer playCountIncrementer;

    private TextView txtTitle, txtTags, txtElapsedTime, txtRemainingTime;
    private ImageButton btnRewind, btnPlayPause, btnFastForward,
            btnTimer, btnRepeat;
    private SeekBar progressBar;
    private ProgressDialog progressDialog;
    private MainActivity mainActivity;

    public AudioPlayerDialog(MainActivity mainActivity)
    {
        super(mainActivity);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.audio_player_dialog);
        this.mainActivity = mainActivity;
        prefs = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        vibrator = (Vibrator)mainActivity.getSystemService(Context.VIBRATOR_SERVICE);
        playCountIncrementer = new PlayCountIncrementer();
        delayTime = setDelay = NO_DELAY;
        loop = NO_LOOP;
        initUI();
    }

    public void setQueue(List<ShibbyFile> queue, boolean isPlaylist)
    {
        this.queue = queue;
        queueIsPlaylist = isPlaylist;
    }

    public void startTimer()
    {
        timerRunning = true;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                if (audioPlayer != null)
                {
                    if (delayTime < 0 && setDelay < 0)
                    {
                        if (audioPlayer.getFileDuration() != -1 &&
                                progressBar.getMax() != audioPlayer.getFileDuration())
                        {
                            progressBar.setMax(audioPlayer.getFileDuration());
                        }
                        // note: 1000 (arbitrary constant) is safe, but 975 (arbitrary constant)
                        // is not always safe, though it usually gives a smoother experience
                        if (audioPlayer.getPosition() > 3 &&
                                audioPlayer.getPosition() > audioPlayer.getFileDuration() - 975)
                        {
                            if (loop > 0)
                            {
                                loop--;
                                playCountIncrementer.increment();
                                mainActivity.runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        String msg = loop + " loop";
                                        if (loop != 1)
                                        {
                                            msg += "s";
                                        }
                                        msg += " remaining";
                                        Toast.makeText(mainActivity, msg,
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                                if (loop == 0)
                                {
                                    final boolean darkModeEnabled = prefs
                                            .getBoolean("darkMode", false);
                                    btnRepeat.post(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            if (darkModeEnabled)
                                            {
                                                btnRepeat.setColorFilter(ContextCompat
                                                        .getColor(mainActivity, R.color.grayLight));
                                            }
                                            else
                                            {
                                                btnRepeat.setColorFilter(null);
                                            }
                                        }
                                    });
                                }
                            }
                            else if (loop == 0)
                            {
                                audioPlayer.setLooping(false);
                                playCountIncrementer.increment();
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
                            else if (loop == LOOP_INFINITE)
                            {
                                playCountIncrementer.increment();
                            }
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
                            playCountIncrementer.increment();
                            // there is a file next in the queue
                            if (progressBar.getProgress() + 1000 >= audioPlayer.getFileDuration() &&
                                    autoplayAllowed())
                            {
                                if (queue != null && queue.indexOf(activeFile) < queue.size() - 1)
                                {
                                    mainActivity.runOnUiThread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            loadFile(queue.get(queue.indexOf(activeFile) + 1));
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
                                else
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
                                    stopTimer();
                                }
                            }
                            // the player is not looping, so stop the player
                            if (!audioPlayer.isLooping() && !autoplayAllowed())
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
                                stopTimer();
                            }
                        }
                    }
                    else if (delayTime > 0)
                    {
                        delayTime--;
                        progressBar.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                progressBar.setProgress(delayTime*1000);
                            }
                        });
                    }
                    else if (delayTime == 0)
                    {
                        delayTime--;
                        setDelay = -1;
                        playAudio();
                        btnTimer.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                btnTimer.setImageResource(R.drawable.ic_timer_off);
                            }
                        });
                    }
                }
            }
        }, 0, 1000);
        boolean hotspotsEnabled = prefs.getBoolean("hotspotsEnabled", false);
        if (hotspotsEnabled)
        {
            vibrationTimer = new Timer();
            final int VIBRATION_ERROR = 75;  // compensation for not updating every millisecond
            vibrationTimer.scheduleAtFixedRate(new TimerTask()
            {
                @Override
                public void run()
                {
                    for (HotspotArray arr : hotspots)
                    {
                        for (Hotspot hotspot : arr.getHotspots())
                        {
                            if (audioPlayer.getPosition() + audioVibrationOffset >
                                    hotspot.getStartTime() - VIBRATION_ERROR &&
                                    audioPlayer.getPosition() + audioVibrationOffset <
                                            hotspot.getEndTime() + VIBRATION_ERROR)
                            {
                                executeHotspot(hotspot);
                            }
                        }
                    }
                    System.out.println(audioPlayer.getPosition());
                }
            }, 0, 50);
        }
    }

    public void stopTimer()
    {
        timerRunning = false;
        if (timer != null)
        {
            timer.cancel();
            timer.purge();
    
            boolean hotspotsEnabled = prefs.getBoolean(
                    "hotspotsEnabled", false);
            if (hotspotsEnabled)
            {
                vibrationTimer.cancel();
                vibrationTimer.purge();
            }
        }
    }

    public void loadFile(final ShibbyFile file)
    {
        this.activeFile = file;
        txtTitle.post(new Runnable()
        {
            @Override
            public void run()
            {
                txtTitle.setText(file != null ?
                        getFileName(file) : "No file selected");
                if (file != null)
                {
                    txtTags.setVisibility(View.VISIBLE);
                    List<String> tags = file.getTags();
                    String tagsStr = "";
                    for (int i = 0; i < tags.size(); i++)
                    {
                        tagsStr += tags.get(i);
                        if (i < tags.size() - 1)
                        {
                            tagsStr += "  |  ";
                        }
                    }
                    if (tagsStr.isEmpty())
                    {
                        txtTags.setVisibility(View.GONE);
                    }
                    else
                    {
                        txtTags.setText(tagsStr);
                    }
                }
                else
                {
                    txtTags.setVisibility(View.GONE);
                }
            }
        });
        delayTime = setDelay = NO_DELAY;
        btnTimer.post(new Runnable()
        {
            @Override
            public void run()
            {
                btnTimer.setImageResource(R.drawable.ic_timer_off);
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
                txtRemainingTime.setText(formatTime((int)fileDuration));
            }
        });
        progressBar.post(new Runnable()
        {
            @Override
            public void run()
            {
                progressBar.setMax((int)fileDuration);
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
            stopTimer();
        }
        fileDownloaded = file != null ? AudioDownloadManager
                .fileIsDownloaded(mainActivity, file) : false;
        fileDuration = file.getDuration();
        if (file != null)
        {
            audioPlayer = new AudioPlayer(progressDialog, fileDownloaded, mainActivity);
            audioPlayer.setLooping(loop != NO_LOOP);
        }
        else
        {
            audioPlayer = null;
        }
        audioVibrationOffset = prefs.getInt("audioVibrationOffset", 0);
        getHotspots();
    }
    
    private List<HotspotArray> getHotspots()
    {
        hotspots = new ArrayList<HotspotArray>();
        try
        {
            JSONArray arr = new JSONArray(prefs.getString("hotspots", "[]"));
            for (int i = 0; i < arr.length(); i++)
            {
                hotspots.add(HotspotArray.fromJSON(arr.getJSONObject(i)));
            }
        }
        catch (JSONException je)
        {
            je.printStackTrace();
        }
        return hotspots;
    }

    private void initUI()
    {
        progressDialog = new ProgressDialog(getContext());

        txtTitle = findViewById(R.id.txtAudioTitle);
        txtTags = findViewById(R.id.txtTags);
        if (activeFile == null)
        {
            txtTitle.setText("No file selected");
            txtTags.setVisibility(View.GONE);
            txtTags.setText("");
        }
        btnTimer = findViewById(R.id.btnPlayerTimer);
        btnTimer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DurationPickerDialog durationPicker = new DurationPickerDialog(mainActivity,
                        (long) delayTime * 1000 + 1, new TimeDurationPicker.OnDurationChangedListener() {
                    @Override
                    public void onDurationChanged(TimeDurationPicker view, long duration)
                    {
                        if (duration == 0)
                        {
                            delayTime = NO_DELAY;
                        }
                        else
                        {
                            delayTime = NO_DELAY;
                            setDelay = (int)duration/1000;
                            final String progressTime = formatTime(setDelay*1000);
                            final String remainingTime = formatTime(0);
                            txtElapsedTime.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    txtElapsedTime.setText(progressTime);
                                }
                            });
                            txtRemainingTime.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    txtRemainingTime.setText(remainingTime);
                                }
                            });
                            progressBar.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    progressBar.setMax(setDelay*1000);
                                    progressBar.setProgress(setDelay*1000);
                                }
                            });
                        }
                        if (setDelay > 0)
                        {
                            btnTimer.setImageResource(R.drawable.ic_timer);
                            audioPlayer.pauseAudio();
                            btnPlayPause.setImageResource(R.drawable.ic_play_circle);
                            stopTimer();
                        }
                    }
                });
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
                            if (!timerRunning)
                            {
                                startTimer();
                            }
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
                        stopTimer();
                        vibrate();
                    }
                    else if (audioPlayer != null)
                    {
                        if (setDelay <= 0)
                        {
                            playAudio();
                        }
                        if (timerRunning)
                        {
                            stopTimer();
                            btnPlayPause.setImageResource(R.drawable.ic_play_circle);
                        }
                        else
                        {
                            if (delayTime == -1)
                            {
                                delayTime = setDelay;
                            }
                            startTimer();
                            btnPlayPause.setImageResource(R.drawable.ic_pause_circle);
                        }
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
                            if (!timerRunning)
                            {
                                startTimer();
                            }
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
                    if (loop != NO_LOOP)
                    {
                        boolean darkModeEnabled = prefs
                                .getBoolean("darkMode", false);
                        if (darkModeEnabled)
                        {
                            btnRepeat.setColorFilter(ContextCompat
                                    .getColor(mainActivity, R.color.grayLight));
                        }
                        else
                        {
                            btnRepeat.setColorFilter(null);
                        }
                    }
                    else
                    {
                        btnRepeat.setColorFilter(ContextCompat.getColor(getContext(),
                                R.color.colorAccent));
                    }
                    loop = loop == NO_LOOP ? LOOP_INFINITE : NO_LOOP;
                    audioPlayer.setLooping(loop != NO_LOOP);
                }
            }
        });
        btnRepeat.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                AlertDialog.Builder dialog;
                boolean darkMode = prefs.getBoolean("darkMode", false);
                if (darkMode)
                {
                    dialog = new AlertDialog.Builder(mainActivity,
                            R.style.DialogThemeDark_Alert);
                }
                else
                {
                    dialog = new AlertDialog.Builder(mainActivity);
                }
                LayoutInflater inflater = mainActivity.getLayoutInflater();
                View dialogView = inflater.inflate(
                        R.layout.loop_selector_dialog, null);
                dialog.setTitle("Number of Loops");
                dialog.setMessage("Select how many times the file should loop");
                dialog.setView(dialogView);
                final NumberPicker numberPicker =
                        dialogView.findViewById(R.id.numberPicker);
                numberPicker.setMaxValue(MAX_LOOPS);
                numberPicker.setMinValue(MIN_LOOPS);
                numberPicker.setValue(loop);
                numberPicker.setWrapSelectorWheel(false);
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int value)
                    {
                        loop = numberPicker.getValue();
                        if (audioPlayer != null)
                        {
                            if (loop == 0)
                            {
                                boolean darkModeEnabled = prefs
                                        .getBoolean("darkMode", false);
                                if (darkModeEnabled)
                                {
                                    btnRepeat.setColorFilter(ContextCompat
                                            .getColor(mainActivity, R.color.grayLight));
                                }
                                else
                                {
                                    btnRepeat.setColorFilter(null);
                                }
                            }
                            else
                            {
                                btnRepeat.setColorFilter(ContextCompat.getColor(getContext(),
                                        R.color.colorAccent));
                            }
                            audioPlayer.setLooping(loop != NO_LOOP);
                        }
                    }
                });
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int value) {}
                });
                AlertDialog alertDialog = dialog.create();
                alertDialog.show();
                return true;
            }
        });
        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(10000);  // 10 seconds; arbitrary default
        progressBar.setProgress(0);
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                if (audioPlayer != null && audioPlayer.getFileDuration() > 0)
                {
                    int duration = audioPlayer.getFileDuration();
                    String progressTime = formatTime(progress);
                    String remainingTime = formatTime(duration-progress);
                    String remainingTimeSeeking = formatTime(duration-audioPlayer.getPosition());
                    if (seeking)
                    {
                        txtElapsedTime.setText(progressTime);
                        txtRemainingTime.setText(remainingTime);
                    }
                    else
                    {
                        txtElapsedTime.setText(progressTime);
                        txtRemainingTime.setText(remainingTimeSeeking);
                    }
                }
                else if (delayTime != NO_DELAY && setDelay != NO_DELAY)
                {
                    String progressTime = formatTime(progress+1);
                    String remainingTime = formatTime((setDelay*1000)+2-progress);
                    String remainingTimeSeeking = formatTime((setDelay*1000)-(delayTime*1000));
                    if (seeking)
                    {
                        txtElapsedTime.setText(progressTime);
                        txtRemainingTime.setText(remainingTime);
                    }
                    else
                    {
                        txtElapsedTime.setText(progressTime);
                        txtRemainingTime.setText(remainingTimeSeeking);
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
                        audioPlayer.getFileDuration() > 0 && delayTime == -1)
                {
                    audioPlayer.seekTo(seekBar.getProgress());
                }
                else if (delayTime != -1)
                {
                    delayTime = seekBar.getProgress()/1000;
                }
            }
        });
        txtElapsedTime = findViewById(R.id.txtElapsedTime);
        txtRemainingTime = findViewById(R.id.txtRemainingTime);
        boolean darkModeEnabled = prefs.getBoolean("darkMode", false);
        if (darkModeEnabled)
        {
            btnTimer.setColorFilter(ContextCompat
                    .getColor(mainActivity, R.color.grayLight));
            btnRewind.setColorFilter(ContextCompat
                    .getColor(mainActivity, R.color.grayLight));
            btnFastForward.setColorFilter(ContextCompat
                    .getColor(mainActivity, R.color.grayLight));
            btnRepeat.setColorFilter(ContextCompat
                    .getColor(mainActivity, R.color.grayLight));
        }
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
        vibrate();
    }
    
    class PlayCountIncrementer
    {
        private boolean incrementing;
        private Timer resetTimer;
        
        public void increment()
        {
            if (!incrementing)
            {
                incrementing = true;
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        PlayCountManager.incrementPlayCount(
                                activeFile, mainActivity);
                        reset();
                    }
                }).start();
            }
        }
        
        private void reset()
        {
            resetTimer = new Timer();
            resetTimer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    incrementing = false;
                    resetTimer.cancel();
                    resetTimer.purge();
                }
            }, 3000);
        }
    }
    
    private void vibrate()
    {
        btnPlayPause.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    }
    
    private void vibrateFor(long duration)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, 200));
        }
        else
        {
            vibrator.vibrate(duration);
        }
    }
    
    private void executeHotspot(Hotspot hotspot)
    {
        vibrateFor(hotspot.getDuration());
    }

    private boolean autoplayAllowed()
    {
        int autoplay = prefs.getInt("autoplay", 1);
        return autoplay == 0 || (autoplay == 1 && queueIsPlaylist);
    }
    
    private String getFileName(ShibbyFile file)
    {
        boolean displayLongNames = prefs.getBoolean("displayLongNames", false);
        return displayLongNames ? file.getName() : file.getShortName();
    }

    private String formatTime(int time)
    {
        int hours = (int)((time / (1000*60*60)) % 24);
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
    
    @Override
    public void onCompletion(MediaPlayer mp)
    {
        Log.d("INFO", "test");
    }
}
