package io.github.kraowx.shibbyapp.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.util.List;

import co.lujun.androidtagview.TagContainerLayout;
import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.audio.AudioController;
import io.github.kraowx.shibbyapp.models.ShibbyFile;
import io.github.kraowx.shibbyapp.tools.AudioDownloadManager;
import io.github.kraowx.shibbyapp.tools.DataManager;
import io.github.kraowx.shibbyapp.tools.PlayCountManager;
import io.github.kraowx.shibbyapp.ui.playlists.AddFileToPlaylistDialog;

public class FileInfoDialog extends Dialog
{
	private ShibbyFile file;
	private List<ShibbyFile> queue;
	private MainActivity mainActivity;
	private SharedPreferences prefs;
	
	public FileInfoDialog(MainActivity mainActivity,
						  ShibbyFile file, List<ShibbyFile> queue)
	{
		super(mainActivity);
		this.mainActivity = mainActivity;
		this.file = file;
		this.queue = queue;
		prefs = PreferenceManager
				.getDefaultSharedPreferences(mainActivity);
		init();
	}
	
	private void init()
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.file_info_dialog);
		setTitle("File Info");
		TextView title = findViewById(R.id.txtTitle);
		boolean displayLongNames = prefs.getBoolean(
				"displayLongNames", false);
		/* Name */
		String name = displayLongNames ? file.getName() : file.getShortName();
		if (file.isPatreonFile())
		{
			int color = mainActivity.getResources().getColor(R.color.redAccent);
			String hex = String.format("#%06X", (0xFFFFFF & color));
			name += " <font color=" + hex + ">(Patreon)</font>";
		}
		title.setText(Html.fromHtml(name));
		/* Duration */
		TextView duration = findViewById(R.id.txtDuration);
		if (file.getDuration() != 0)
		{
			duration.setText(formatTime((int)file.getDuration()));
		}
		else
		{
			duration.setVisibility(View.GONE);
		}
		/* Play Count */
		TextView playCount = findViewById(R.id.txtPlayCount);
		int count = PlayCountManager.getPlayCount(file, mainActivity);
		String countText = "Played " + count + " time";
		if (count != 1)
		{
			countText += "s";
		}
		playCount.setText(countText);
		/* Tags */
		TagContainerLayout tags = findViewById(R.id.tags);
		tags.setTags(file.getTags());
		/* Description */
		TextView description = findViewById(R.id.txtDescription);
		if (file.getDescription() == null ||
				(file.getDescription() != null && file.getDescription().isEmpty()))
		{
			description.setText(Html.fromHtml("<i>No description</i>"));
		}
		else
		{
			description.setMovementMethod(LinkMovementMethod.getInstance());
			description.setText(Html.fromHtml(file.getDescription()));
		}
		
		ImageButton btnPlay = findViewById(R.id.btnPlay);
		btnPlay.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				AudioController audioController = mainActivity.getAudioController();
				audioController.loadFile(file);
				audioController.setQueue(queue, false);
				audioController.setVisible(true);
			}
		});
		final ImageButton btnDownload = findViewById(R.id.btnDownload);
		btnDownload.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if (!(AudioDownloadManager.fileIsDownloaded(mainActivity, file) ||
						file.getType().equals("user")))
				{
					mainActivity.getDownloadManager().downloadFile(file, btnDownload);
					btnDownload.setColorFilter(ContextCompat
							.getColor(mainActivity, R.color.redAccent));
				}
				else if (mainActivity.getDownloadManager().isDownloadingFile(file))
				{
					if (mainActivity.getDownloadManager().cancelDownload(file))
					{
						boolean darkModeEnabled = prefs
								.getBoolean("darkMode", false);
						if (darkModeEnabled)
						{
							btnDownload.setColorFilter(ContextCompat
									.getColor(mainActivity, R.color.grayLight));
						}
						else
						{
							btnDownload.setColorFilter(null);
						}
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
					Drawable darkIcon = ContextCompat.getDrawable(mainActivity,
							R.drawable.ic_warning).mutate();
					darkIcon.setColorFilter(new ColorMatrixColorFilter(new float[]
							{
									-1, 0, 0, 0, 200,
									0, -1, 0, 0, 200,
									0, 0, -1, 0, 200,
									0, 0, 0, 1, 0
							}));
					boolean darkModeEnabled = prefs.getBoolean("darkMode", false);
					AlertDialog.Builder builder;
					if (darkModeEnabled)
					{
						builder = new AlertDialog.Builder(mainActivity,
								R.style.DialogThemeDark_Alert);
						builder.setIcon(darkIcon);
					}
					else
					{
						builder = new AlertDialog.Builder(mainActivity);
						builder.setIcon(R.drawable.ic_warning);
					}
					String title = "Delete ";
					String message = "Are you sure you want to delete this file?";
					if (file.getType().equals("user"))
					{
						title += "user file";
						message += " You will have to re-import it if " +
								"you want to listen to it again.";
					}
					else
					{
						title += "download";
					}
					builder.setTitle(title)
							.setMessage(message)
							.setPositiveButton(android.R.string.yes,
									new DialogInterface.OnClickListener()
									{
										public void onClick(DialogInterface dialog, int which)
										{
											AudioDownloadManager.deleteFile(mainActivity, file);
											boolean darkModeEnabled = prefs
													.getBoolean("darkMode", false);
											if (darkModeEnabled)
											{
												btnDownload.setColorFilter(ContextCompat
														.getColor(mainActivity, R.color.grayLight));
											}
											else
											{
												btnDownload.setColorFilter(null);
											}
											if (file.getType().equals("user"))
											{
												new DataManager(mainActivity).removeUserFile(file);
//												mData.remove(file);
//												notifyDataSetChanged();
											}
										}
									})
							.setNegativeButton(android.R.string.no, null)
							.show();
				}
			}
		});
		ImageButton btnAddToPlaylist = findViewById(R.id.btnAddToPlaylist);
		btnAddToPlaylist.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				showAddFileToPlaylistDialog(file);
			}
		});
		boolean darkModeEnabled = prefs
				.getBoolean("darkMode", false);
		if (darkModeEnabled)
		{
			btnPlay.setColorFilter(ContextCompat
					.getColor(mainActivity, R.color.grayLight));
			btnDownload.setColorFilter(ContextCompat
					.getColor(mainActivity, R.color.grayLight));
			btnAddToPlaylist.setColorFilter(ContextCompat
					.getColor(mainActivity, R.color.grayLight));
		}
		else
		{
			btnPlay.setColorFilter(null);
			btnDownload.setColorFilter(null);
			btnAddToPlaylist.setColorFilter(null);
		}
		
		if (mainActivity.getDownloadManager().isDownloadingFile(file))
		{
			btnDownload.setColorFilter(ContextCompat
					.getColor(mainActivity, R.color.redAccent));
		}
		else if (AudioDownloadManager.fileIsDownloaded(mainActivity, file) ||
				file.getType().equals("user"))
		{
			btnDownload.setColorFilter(ContextCompat
					.getColor(mainActivity, R.color.colorAccent));
		}
		show();
	}
	
	private void showAddFileToPlaylistDialog(ShibbyFile file)
	{
		AddFileToPlaylistDialog dialog = new AddFileToPlaylistDialog(
				mainActivity, new ShibbyFile[]{file}, false);
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
