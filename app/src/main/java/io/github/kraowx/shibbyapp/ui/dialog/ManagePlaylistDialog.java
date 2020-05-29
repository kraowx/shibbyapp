package io.github.kraowx.shibbyapp.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

import androidx.core.content.ContextCompat;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.tools.PlaylistManager;

public class ManagePlaylistDialog extends Dialog
{
	private String playlistName;
	private MainActivity mainActivity;
	private PlaylistModifiedListener listener;
	private SharedPreferences prefs;
	
	public ManagePlaylistDialog(MainActivity mainActivity,
								String playlistName)
	{
		super(mainActivity);
		this.mainActivity = mainActivity;
		this.playlistName = playlistName;
		prefs = PreferenceManager
				.getDefaultSharedPreferences(mainActivity);
		init();
	}
	
	public interface PlaylistModifiedListener
	{
		void playlistTitleChanged(String oldName, String newName);
		void playlistDescriptionChanged(String playlistName, String description);
		void playlistDeleted(String playlistName);
	}
	
	public void setListener(PlaylistModifiedListener listener)
	{
		this.listener = listener;
	}
	
	public void setPlaylistName(String playlistName)
	{
		this.playlistName = playlistName;
	}
	
	private void init()
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.manage_playlist_dialog);
		ImageButton btnRename = findViewById(R.id.btnRename);
		btnRename.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				createRenameDialog();
			}
		});
		ImageButton btnEditDescription = findViewById(R.id.btnEditDescription);
		btnEditDescription.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				createEditDescriptionDialog();
			}
		});
		ImageButton btnDelete = findViewById(R.id.btnDelete);
		btnDelete.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				createDeleteDialog();
			}
		});
		boolean darkModeEnabled = prefs
				.getBoolean("darkMode", false);
		if (darkModeEnabled)
		{
			btnRename.setColorFilter(ContextCompat
					.getColor(mainActivity, R.color.grayLight));
			btnEditDescription.setColorFilter(ContextCompat
					.getColor(mainActivity, R.color.grayLight));
			btnDelete.setColorFilter(ContextCompat
					.getColor(mainActivity, R.color.grayLight));
		}
	}
	
	private void createRenameDialog()
	{
		TextEditDialog renameDialog = new TextEditDialog(mainActivity,
				"Rename playlist", playlistName, TextEditDialog.Type.SINGLE_LINE, getTheme(),
				new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						String newName = ((TextEditDialog)dialog).getText();
						if (PlaylistManager.renamePlaylist(mainActivity,
								playlistName, newName) && listener != null)
						{
							listener.playlistTitleChanged(playlistName, newName);
							playlistName = newName;
						}
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
		renameDialog.show();
	}
	
	private void createEditDescriptionDialog()
	{
		String description = PlaylistManager.getPlaylistDescription(
				mainActivity, playlistName);
		TextEditDialog editDialog = new TextEditDialog(mainActivity,
				"Edit description", description, TextEditDialog.Type.MULTI_LINE, getTheme(),
				new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						final String description = ((TextEditDialog)dialog).getText();
						new Thread(new Runnable()
						{
							@Override
							public void run()
							{
								PlaylistManager.setPlaylistDescription(mainActivity,
										playlistName, description);
							}
						}).start();
						if (listener != null)
						{
							listener.playlistDescriptionChanged(
									playlistName, description);
						}
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
		editDialog.show();
	}
	
	private void createDeleteDialog()
	{
		OptionDialog deleteDialog = new OptionDialog(mainActivity,
				"Delete playlist", "Are you sure you want to delete " +
				"the playlist \"" + playlistName + "\"?", "OK", "Cancel",
				getTheme(), R.drawable.ic_warning,
				new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						PlaylistManager.removePlaylist(mainActivity, playlistName);
						if (listener != null)
						{
							listener.playlistDeleted(playlistName);
						}
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
}
