package io.github.kraowx.shibbyapp.audio;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.models.Hotspot;
import io.github.kraowx.shibbyapp.models.HotspotArray;
import io.github.kraowx.shibbyapp.models.ShibbyFile;
import io.github.kraowx.shibbyapp.tools.AudioDownloadManager;
import io.github.kraowx.shibbyapp.tools.PlayCountIncrementer;

public class AudioPlayerService extends Service
		implements MediaPlayer.OnPreparedListener
{
	public static final String ACTION_CREATE = "shibbyapp_create";
	public static final String ACTION_REWIND = "shibbyapp_rewind";
	public static final String ACTION_PLAY = "shibbyapp_play";
	public static final String ACTION_PAUSE = "shibbyapp_pause";
	public static final String ACTION_STOP = "shibbyapp_stop";
	public static final String ACTION_FASTFORWARD = "shibbyapp_fastforward";
	
	private final String NOTIFICATION_CHANNEL_ID = "ForegroundShibbyAppMediaPlayer";
	
	private final int NO_DELAY = -1;
	private final int LOOP_INFINITE = -1;
	private final int NO_LOOP = 0;
	private final int MAX_LOOPS = 100;
	private final int MIN_LOOPS = 0;
	
	private boolean fileDownloaded,
			seeking, queueIsPlaylist,
			timerRunning, wakeLockEnabled;
	private int delayTime = NO_DELAY;
	private int setDelay = NO_DELAY;
	private int loop = NO_LOOP;
	private int audioVibrationOffset;
	private long fileDuration;
	private String activePlaylist;
	private ShibbyFile activeFile;
	private List<ShibbyFile> queue;
	private List<HotspotArray> hotspots;
	private AudioPlayer audioPlayer;
	private Timer timer, vibrationTimer;
	private SharedPreferences prefs;
	private Vibrator vibrator;
	private PlayCountIncrementer playCountIncrementer;
	private PowerManager.WakeLock wakeLock;
	private WifiManager.WifiLock wifiLock;
	private Notification notification;
	private PendingIntent notificationContentIntent;
	private MediaSessionManager mediaSessionManager;
	private MediaSession mediaSession;
	private MediaController mediaController;
	
	private AudioPlayerServiceListener playerListener;
	private ActionListener actionListener;
	
	private ProgressDialog progressDialog;
	
	// Binder given to clients
	private final IBinder iBinder = new LocalBinder();
	
	public interface AudioPlayerServiceListener
	{
		void audioServiceStopping();
	}
	
	public interface ActionListener
	{
		void actionFired(String action);
	}
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return iBinder;
	}
	
	@Override
	public void onCreate()
	{
		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		vibrator = (Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
//		playCountIncrementer = new PlayCountIncrementer();
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			createNotificationChannel();
			Intent notificationIntent = new Intent(this, MainActivity.class);
			notificationContentIntent =
					PendingIntent.getActivity(this, 0, notificationIntent, 0);
			
			mediaSession = new MediaSession(getApplicationContext(), "shibbyappPlayerSession");
			
			startForeground(1, generateNotification(null, null, false));
		}
		
		/* Keep wifi and the cpu awake to avoid audio stopping while streaming */
		PowerManager powerManager = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "shibbyapp:audioplayerwakelock");
		
		WifiManager wm = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "shibbyapp:audioplayerwifilock");
		
		wakeLockEnabled = prefs.getBoolean("wakeLock", false);
        if (wakeLockEnabled)
        {
        	wakeLock.acquire();
        	wifiLock.acquire();
        }
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		destroyService();
	}
	
	private void destroyService()
	{
		if (playerListener != null)
		{
			playerListener.audioServiceStopping();
		}
		progressDialog = null;
		if (audioPlayer != null)
		{
			audioPlayer.stopAudio();
			audioPlayer.destroy();
		}
		if (wakeLockEnabled)
		{
			wakeLock.release();
			wifiLock.release();
		}
		if (notification != null)
		{
			stopForeground(true);
			stopSelf();
		}
	}
	
	@Override
	public void onPrepared(MediaPlayer mp) {
		updateNotification(activeFile, activePlaylist, true);
	}
	
	private Notification.Action generateAction(int icon, String action)
	{
		Intent intent = new Intent(getApplicationContext(), AudioPlayerService.class);
		intent.setAction(action);
		PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
		return new Notification.Action.Builder(icon, action, pendingIntent).build();
	}
	
	private Notification.Action getRewindAction()
	{
		return generateAction(R.drawable.ic_skip_previous_large, ACTION_REWIND);
	}
	
	private Notification.Action getPlayPauseAction(boolean setPlaying)
	{
		int icon = R.drawable.ic_play_arrow_large;
		String action = ACTION_PLAY;
		
		if ((audioPlayer != null && audioPlayer.isInitialized() &&
				audioPlayer.isPlaying()) || setPlaying)
		{
			icon = R.drawable.ic_pause_large;
			action = ACTION_PAUSE;
		}
		return generateAction(icon, action);
	}
	
	private Notification.Action getFastForwardAction()
	{
		return generateAction(R.drawable.ic_skip_next_large, ACTION_FASTFORWARD);
	}
	
	private Notification.Action getStopAction()
	{
		return generateAction(R.drawable.ic_close_large, ACTION_STOP);
	}
	
	private Notification generateNotification(String fileName, String playlistName,
											  boolean setPlaying)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			return new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
					.setContentTitle(fileName != null ? fileName : "No file playing")
					.setSubText(playlistName)
					.setSmallIcon(R.drawable.ic_play_circle)
					.addAction(getRewindAction())
					.addAction(getPlayPauseAction(setPlaying))
					.addAction(getFastForwardAction())
//					.addAction(getStopAction())
					.setStyle(new Notification.MediaStyle()
							.setMediaSession(mediaSession.getSessionToken())
							.setShowActionsInCompactView(0, 1, 2, 3))
//					.setContentIntent(notificationContentIntent)
					.build();
		}
		return null;
	}
	
	private void updateNotification(ShibbyFile file, String playlistName, boolean setPlaying)
	{
		System.out.println("updating");
		Notification notification = generateNotification(file != null ?
				file.getShortName() : null, playlistName, setPlaying);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(1, notification);
	}
	
	public void setPlayerListener(AudioPlayerServiceListener playerListener)
	{
		this.playerListener = playerListener;
	}
	
	public void setActionListener(ActionListener actionListener)
	{
		this.actionListener = actionListener;
	}
	
	public void setProgressDialog(ProgressDialog progressDialog)
	{
		this.progressDialog = progressDialog;
	}
	
	public class LocalBinder extends Binder
	{
		public AudioPlayerService getService()
		{
			return AudioPlayerService.this;
		}
	}
	
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		String action = intent.getAction();
		if (action.equals(ACTION_CREATE))
		{
			try
			{
				activeFile = ShibbyFile.fromJSON(intent.getExtras().getString("media"));
			}
			catch (NullPointerException e)
			{
				stopSelf();
			}
			
			if (activeFile != null && activeFile.getLink() != null)
			{
				loadFile(activeFile, activePlaylist);
			}
		}
		else if (action.equals(ACTION_REWIND) && audioPlayer != null)
		{
		
		}
		else if (action.equals(ACTION_PLAY) && audioPlayer != null)
		{
			playAudio();
		}
		else if (action.equals(ACTION_PAUSE) && audioPlayer != null)
		{
			pauseAudio();
		}
		else if (action.equals(ACTION_FASTFORWARD) && audioPlayer != null)
		{
		
		}
		if (actionListener != null)
		{
			actionListener.actionFired(action);
		}
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	public boolean playerIsInitialized()
	{
		return audioPlayer != null && audioPlayer.isInitialized();
	}
	
	public boolean playerExists()
	{
		return audioPlayer != null;
	}
	
	public boolean isPlaying()
	{
		return audioPlayer.isPlaying();
	}
	
	public boolean isLooping()
	{
		return audioPlayer.isLooping();
	}
	
	public int getFileDuration()
	{
		return audioPlayer.getFileDuration();
	}
	
	public int getPosition()
	{
		return audioPlayer.getPosition();
	}
	
	public ShibbyFile getActiveFile()
	{
		return activeFile;
	}
	
	public String getActivePlaylist()
	{
		return activePlaylist;
	}
	
	public void setLooping(boolean looping)
	{
		audioPlayer.setLooping(looping);
	}
	
	public void seekTo(int millis)
	{
		audioPlayer.seekTo(millis);
	}
	
	public void destroyPlayer()
	{
		if (audioPlayer != null && audioPlayer.isInitialized())
		{
			audioPlayer.destroy();
		}
	}
	
	public void loadFile(ShibbyFile file, String playlistName)
	{
		this.activeFile = file;
		this.activePlaylist = playlistName;
		delayTime = setDelay = NO_DELAY;
//		if (audioPlayer != null && audioPlayer.isPlaying())
//		{
//			audioPlayer.stopAudio();
//		}
		fileDownloaded = file != null ? AudioDownloadManager
				.fileIsDownloaded(getApplicationContext(), file) : false;
//		fileDuration = file.getDuration();
		if (audioPlayer != null && audioPlayer.isInitialized())
		{
			audioPlayer.destroy();
		}
		if (file != null)
		{
			audioPlayer = new AudioPlayer(progressDialog,
					fileDownloaded, getApplicationContext());
			audioPlayer.setOnPreparedListener(this);
			audioPlayer.setLooping(loop != NO_LOOP);
		}
		else
		{
			audioPlayer = null;
		}
		audioVibrationOffset = prefs.getInt("audioVibrationOffset", 0);
		updateNotification(file, playlistName, false);
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
	
	public void playAudio()
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
					.getFileLocation(getApplicationContext(), activeFile)
					.getAbsolutePath());
		}
		else
		{
			audioPlayer.execute(AudioPlayerService.this.activeFile.getLink());
		}
		updateNotification(activeFile, activePlaylist, true);
		vibrate();
	}
	
	public void pauseAudio()
	{
		if (audioPlayer != null)
		{
			audioPlayer.pauseAudio();
			updateNotification(activeFile, activePlaylist, false);
		}
	}
	
	public void resumeAudio()
	{
		if (audioPlayer != null)
		{
			audioPlayer.resumeAudio();
			updateNotification(activeFile, activePlaylist, false);
		}
	}
	
	public void stopAudio()
	{
		if (audioPlayer != null)
		{
			audioPlayer.stopAudio();
			updateNotification(activeFile, activePlaylist, false);
		}
	}
	
	private void vibrate()
	{
//		btnPlayPause.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
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
	
	private void createNotificationChannel()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			NotificationChannel serviceChannel = new NotificationChannel(
					NOTIFICATION_CHANNEL_ID,
					"Media player",
					NotificationManager.IMPORTANCE_HIGH
			);
			NotificationManager manager = getSystemService(NotificationManager.class);
			manager.createNotificationChannel(serviceChannel);
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
}
