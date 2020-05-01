package io.github.kraowx.shibbyapp.ui.dialog;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.IOException;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;

public class SettingsDialog extends Dialog
{
	private MainActivity mainActivity;
	
	public SettingsDialog(MainActivity mainActivity)
	{
		super(mainActivity);
		this.mainActivity = mainActivity;
		initUI();
	}
	
	private void initUI()
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.settings_dialog);
		setTitle("Settings");
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mainActivity);
		final SharedPreferences.Editor editor = prefs.edit();
		boolean updateStartup = prefs.getBoolean("updateStartup", true);
		boolean displayLongNames = prefs.getBoolean("displayLongNames", false);
		boolean showSpecialPrefixTags = prefs.getBoolean("showSpecialPrefixTags", true);
		boolean darkModeEnabled = prefs.getBoolean("darkMode", false);
		boolean wakeLockEnabled = prefs.getBoolean("wakeLock", false);
		boolean hotspotsEnabled = prefs.getBoolean("hotspotsEnabled", false);
		int audioVibrationOffset = prefs.getInt("audioVibrationOffset", 0);
		int autoplay = prefs.getInt("autoplay", 1);
		String server = prefs.getString("server", mainActivity.getString(R.string.main_server));
		final Switch switchUpdateStartup = findViewById(R.id.switchUpdateOnStartup);
		switchUpdateStartup.setChecked(updateStartup);
		switchUpdateStartup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				editor.putBoolean("updateStartup", isChecked);
				editor.commit();
			}
		});
		final Switch switchDisplayLongNames = findViewById(R.id.switchDisplayLongNames);
		switchDisplayLongNames.setChecked(displayLongNames);
		switchDisplayLongNames.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				editor.putBoolean("displayLongNames", isChecked);
				editor.commit();
				Toast.makeText(mainActivity.getContext(),
						"Refresh the current page for the change to take effect",
						Toast.LENGTH_LONG).show();
			}
		});
		final Switch switchShowPrefixTags = findViewById(R.id.switchShowSpecialPrefixTags);
		switchShowPrefixTags.setChecked(showSpecialPrefixTags);
		switchShowPrefixTags.setOnCheckedChangeListener(
				new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				editor.putBoolean("showSpecialPrefixTags", isChecked);
				editor.commit();
				Toast.makeText(mainActivity.getContext(),
						"Refresh the current page for the change to take effect",
						Toast.LENGTH_LONG).show();
			}
		});
		final Switch switchDarkMode = findViewById(R.id.switchDarkMode);
		switchDarkMode.setChecked(darkModeEnabled);
		switchDarkMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if (isChecked)
				{
					mainActivity.setTheme(R.style.AppTheme);
				}
				else
				{
					mainActivity.setTheme(R.style.AppThemeDark);
				}
				editor.putBoolean("darkMode", isChecked);
				editor.commit();
				Toast.makeText(mainActivity.getContext(),
						"Relaunch the app for the change to take effect",
						Toast.LENGTH_LONG).show();
			}
		});
		final Switch switchWakeLock = findViewById(R.id.switchWakeLock);
		switchWakeLock.setChecked(wakeLockEnabled);
		switchWakeLock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				editor.putBoolean("wakeLock", isChecked);
				editor.commit();
				Toast.makeText(mainActivity.getContext(),
						"Relaunch the app for the change to fully take effect",
						Toast.LENGTH_LONG).show();
			}
		});
		final Switch switchHotspotsEnabled = findViewById(R.id.switchHotspotsEnabled);
		switchHotspotsEnabled.setChecked(hotspotsEnabled);
		switchHotspotsEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				editor.putBoolean("hotspotsEnabled", isChecked);
				editor.commit();
			}
		});
		final SeekBar seekBarOffset = findViewById(R.id.seekBarOffset);
		seekBarOffset.setMax(2000);
		seekBarOffset.setProgress(audioVibrationOffset+1000);
		seekBarOffset.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				int offset = seekBar.getProgress()-1000;
				testAudioVibrationSync(offset);
				editor.putInt("audioVibrationOffset", offset);
				editor.commit();
			}
		});
		final EditText txtServer = findViewById(R.id.txtServer);
		txtServer.setText(server);
		final Spinner spinnerAutoplay = findViewById(R.id.spinnerAutoplay);
		spinnerAutoplay.setSelection(autoplay);
		spinnerAutoplay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
									   int position, long id)
			{
				editor.putInt("autoplay", position);
				editor.commit();
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
		});
		Button btnApplyChanges = findViewById(R.id.btnApplyChanges);
		btnApplyChanges.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				editor.putString("server", txtServer.getText().toString());
				editor.commit();
				Toast.makeText(mainActivity.getContext(), "Server address updated",
						Toast.LENGTH_LONG).show();
			}
		});
		Button btnRestore = findViewById(R.id.btnRestore);
		btnRestore.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				txtServer.setText(mainActivity.getString(R.string.main_server));
				switchUpdateStartup.setChecked(true);
				switchDisplayLongNames.setChecked(false);
				switchShowPrefixTags.setChecked(true);
				switchDarkMode.setChecked(false);
				switchWakeLock.setChecked(false);
				spinnerAutoplay.setSelection(1, true);
				editor.putString("server", mainActivity.getString(R.string.main_server));
				editor.putBoolean("updateStartup", true);
				editor.putBoolean("displayLongNames", false);
				editor.putBoolean("showSpecialTagPrefixes", true);
				editor.putBoolean("darkMode", false);
				editor.putBoolean("wakeLock", false);
				editor.putInt("autoplay", 1);
				editor.commit();
				Toast.makeText(mainActivity.getContext(),
						"Relaunch the app for the changes to take full effect",
						Toast.LENGTH_LONG).show();
			}
		});
		Button btnErasePatreonData = findViewById(R.id.btnErasePatreonData);
		btnErasePatreonData.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				editor.putString("patreonFiles", "[]");
				editor.putLong("patreonLastUpdate", 0);
				editor.commit();
				Toast.makeText(mainActivity.getContext(),
						"Relaunch the app for the change to fully take effect",
						Toast.LENGTH_LONG).show();
			}
		});
		show();
	}
	
	private void testAudioVibrationSync(int offset)
	{
		if (offset >= 0)
		{
			playSound();
		}
		else
		{
			vibrate();
		}
		try
		{
			Thread.sleep(Math.abs(offset));
		}
		catch (InterruptedException ie)
		{
			ie.printStackTrace();
		}
		if (offset >= 0)
		{
			vibrate();
		}
		else
		{
			playSound();
		}
	}
	
	private void vibrate()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			Vibrator vibrator = (Vibrator)mainActivity
					.getSystemService(Service.VIBRATOR_SERVICE);
			vibrator.vibrate(VibrationEffect
					.createOneShot(500, 200));
		}
	}
	
	private void playSound()
	{
		MediaPlayer mp = new MediaPlayer();
		mp.reset();
		try
		{
			mp.setDataSource(mainActivity, Settings.System.DEFAULT_NOTIFICATION_URI);
			mp.prepare();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		mp.start();
	}
}
