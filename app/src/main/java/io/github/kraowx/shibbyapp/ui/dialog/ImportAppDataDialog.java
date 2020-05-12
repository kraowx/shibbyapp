package io.github.kraowx.shibbyapp.ui.dialog;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;

public class ImportAppDataDialog extends Dialog
{
	private File selectedFile;
	
	public ImportAppDataDialog(MainActivity mainActivity)
	{
		super(mainActivity);
		init(mainActivity);
	}
	
	private void init(final MainActivity mainActivity)
	{
		File dir = Environment.getExternalStorageDirectory();
		File[] dirs = dir.listFiles();
		if (dirs == null)
		{
			Toast.makeText(mainActivity, "Enable the \"Storage\" " +
					"permission to use this feature", Toast.LENGTH_LONG).show();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.import_appdata_dialog);
		final TextView txtFile = findViewById(R.id.txtFileName);
		Button btnSelectFile = findViewById(R.id.btnSelectFile);
		btnSelectFile.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				FileChooserDialog fileChooser = new FileChooserDialog(mainActivity);
				fileChooser.setFileListener(new FileChooserDialog.FileSelectedListener()
				{
					@Override
					public void fileSelected(File file)
					{
						selectedFile = file;
						txtFile.setText(file.getName());
					}
				});
				fileChooser.showDialog();
			}
		});
		Button btnImport = findViewById(R.id.btnImport);
		btnImport.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (selectedFile != null)
				{
					SharedPreferences prefs = PreferenceManager
							.getDefaultSharedPreferences(mainActivity);
					SharedPreferences.Editor editor = prefs.edit();
					StringBuilder sb = new StringBuilder();
					JSONObject data = null;
					String line = "";
					BufferedReader reader = null;
					try
					{
						reader = new BufferedReader(new FileReader(selectedFile));
						while ((line = reader.readLine()) != null)
						{
							sb.append(line);
						}
						data = new JSONObject(sb.toString());
						editor.putString("playlists", data.getJSONArray("playlists").toString());
						editor.putString("userFiles", data.getJSONArray("userFiles").toString());
						JSONArray playlists = data.getJSONArray("playlists");
						for (int i = 0; i < playlists.length(); i++)
						{
							String playlistId = "playlist" + playlists.getString(i);
							editor.putString(playlistId, data.get(playlistId).toString());
						}
						if (data.has("playCounts"))
						{
							editor.putString("playCounts", data.getJSONObject("playCounts").toString());
						}
						
						/* Settings Data */
						if (data.has("settingsData"))
						{
							JSONObject settingsData = data.getJSONObject("settingsData");
							editor.putBoolean("updateStartup", settingsData.getBoolean("updateStartup"));
							editor.putBoolean("displayLongNames", settingsData.getBoolean("displayLongNames"));
							editor.putBoolean("showSpecialPrefixTags", settingsData.getBoolean("showSpecialPrefixTags"));
							editor.putBoolean("darkMode", settingsData.getBoolean("darkMode"));
							editor.putBoolean("wakeLock", settingsData.getBoolean("wakeLock"));
							editor.putBoolean("hotspotsEnabled", settingsData.getBoolean("hotspotsEnabled"));
							editor.putInt("audioVibrationOffset", settingsData.getInt("audioVibrationOffset"));
							editor.putInt("autoplay", settingsData.getInt("autoplay"));
							editor.putString("server", settingsData.getString("server"));
						}
						editor.commit();
					}
					catch (IOException ioe)
					{
						ioe.printStackTrace();
					}
					catch (JSONException je)
					{
						je.printStackTrace();
					}
					Toast.makeText(mainActivity, "Data imported. Restart the app " +
									"for the changes to take effect",
							Toast.LENGTH_LONG).show();
					ImportAppDataDialog.this.dismiss();
				}
				else
				{
					Toast.makeText(mainActivity, "You must first select a file",
							Toast.LENGTH_LONG).show();
				}
			}
		});
		show();
	}
}
