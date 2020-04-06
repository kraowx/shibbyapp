package io.github.kraowx.shibbyapp.ui.dialog;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;

public class SettingsDialog extends Dialog
{
	public SettingsDialog(MainActivity mainActivity)
	{
		super(mainActivity);
		initUI(mainActivity);
	}
	
	private void initUI(final MainActivity mainActivity)
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
		show();
	}
}
