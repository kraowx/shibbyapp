package io.github.kraowx.shibbyapp.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.NumberPicker;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.models.ShibbyFile;
import io.github.kraowx.shibbyapp.tools.AudioDownloadManager;
import io.github.kraowx.shibbyapp.tools.DataManager;
import io.github.kraowx.shibbyapp.tools.PlaylistManager;

public class UpdateV3ResetDialog extends Dialog
{
	private MainActivity mainActivity;
	private SharedPreferences prefs;
	
	public UpdateV3ResetDialog(MainActivity mainActivity)
	{
		super(mainActivity);
		this.mainActivity = mainActivity;
		prefs = PreferenceManager.getDefaultSharedPreferences(mainActivity);
		initUI();
	}
	
	private void initUI()
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.update_v3_dialog);
		
		Button btnErase = findViewById(R.id.btnErase);
		btnErase.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				createEraseDialog();
			}
		});
		show();
	}
	
	private void createEraseDialog()
	{
		String message = "This action will erase all locally stored shibby files, " +
				"playlists, and imported files.";
		OptionDialog deleteDialog = new OptionDialog(mainActivity,
				"Erase ALL ShibbyApp data?", message, "OK", "Cancel",
				getTheme(), R.drawable.ic_warning,
				new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						eraseOldData();
						dialog.dismiss();
						showEraseSuccessDialog();
					}
				},
				new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
				});
		deleteDialog.show();
	}
	
	private int getTheme()
	{
		boolean darkModeEnabled = prefs
				.getBoolean("darkMode", false);
		if (darkModeEnabled)
		{
			return R.style.DialogThemeDark_Alert;
		}
		return 0;  // invalid theme; force system default
	}
	
	private void eraseOldData()
	{
		SharedPreferences.Editor editor = prefs.edit();
		DataManager dataManager = new DataManager(mainActivity);
		for (ShibbyFile file : dataManager.getFiles())
		{
			AudioDownloadManager.deleteFile(mainActivity, file);
		}
		editor.putString("files", "[]");
		for (String playlist : PlaylistManager.getPlaylists(mainActivity))
		{
			editor.remove("playlist" + playlist);
			editor.remove("descplaylist" + playlist);
		}
		editor.putString("playlists", "[]");
		editor.putString("patreonFiles", "[]");
		editor.putString("userFiles", "[]");
		editor.commit();
	}
	
	private void showEraseSuccessDialog()
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
		dialog.setTitle("Success");
		dialog.setMessage("Restart the app for the change to fully take effect.");
		dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialogInterface, int value) {}
		});
		AlertDialog alertDialog = dialog.create();
		alertDialog.show();
	}
}
