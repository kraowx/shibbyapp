package io.github.kraowx.shibbyapp.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
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
	private final String AUDIO_INFO_SEPARATOR = " • ";
	private final int MAX_INITIAL_TAGS = 12;
	
	private String queueName;
	private ShibbyFile file;
	private List<ShibbyFile> queue;
	private MainActivity mainActivity;
	private SharedPreferences prefs;
	
	public FileInfoDialog(MainActivity mainActivity,
						  ShibbyFile file, List<ShibbyFile> queue,
						  String queueName)
	{
		super(mainActivity);
		this.mainActivity = mainActivity;
		this.file = file;
		this.queue = queue;
		this.queueName = queueName;
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
		/* Name */
		String name = file.getName();
		if (file.getViewType().equals("patreon"))
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
		final TagContainerLayout tags = findViewById(R.id.tags);
		if (file.getTags() != null)
		{
			for (int i = 0; i < file.getTags().size() && i < MAX_INITIAL_TAGS; i++)
			{
				tags.addTag(file.getTags().get(i));
			}
		}
		Button btnMoreTags = findViewById(R.id.btnMoreTags);
		int tagDiff = file.getTags().size() - MAX_INITIAL_TAGS;
		String moreTagsText = "+ " + tagDiff + " more tag";
		if (tagDiff != 1)
		{
			moreTagsText += "s";
		}
		btnMoreTags.setText(moreTagsText);
		btnMoreTags.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				v.setVisibility(View.GONE);
				for (int i = MAX_INITIAL_TAGS; i < file.getTags().size(); i++)
				{
					tags.addTag(file.getTags().get(i));
				}
			}
		});
		if (file.getTags().size() <= MAX_INITIAL_TAGS)
		{
			btnMoreTags.setVisibility(View.GONE);
		}
		/* Audio Info */
		TextView audioInfo = findViewById(R.id.txtAudioInfo);
		if (file.getVersion() >= 3)
		{
			audioInfo.setText(getAudioInfoText());
		}
		else
		{
			audioInfo.setVisibility(View.GONE);
		}
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
		/* More Info */
		final LinearLayout layoutTone = findViewById(R.id.layoutTone);
		final LinearLayout layoutSetting = findViewById(R.id.layoutSetting);
		final LinearLayout layoutConsent = findViewById(R.id.layoutConsent);
		final LinearLayout layoutDS = findViewById(R.id.layoutDS);
		final LinearLayout layoutHypnosisStyle = findViewById(R.id.layoutHypnosisStyle);
		final LinearLayout layoutHypnosisLevel = findViewById(R.id.layoutHypnosisLevel);
		final LinearLayout layoutWakener = findViewById(R.id.layoutWakener);
		final LinearLayout layoutAftercare = findViewById(R.id.layoutAftercare);
		final TagContainerLayout triggers = findViewById(R.id.triggers);
		final TextView txtTone = findViewById(R.id.txtTone);
		final TextView txtSetting = findViewById(R.id.txtSetting);
		final TextView txtConsent = findViewById(R.id.txtConsent);
		final TextView txtDS = findViewById(R.id.txtDS);
		final TextView txtHypnosisStyle = findViewById(R.id.txtHypnosisStyle);
		final TextView txtHypnosisLevel = findViewById(R.id.txtHypnosisLevel);
		final TextView txtWakener = findViewById(R.id.txtWakener);
		final TextView txtAftercare = findViewById(R.id.txtAftercare);
		final TextView txtTriggersHeader = findViewById(R.id.txtTriggersHeader);
		boolean alwaysShowDetailedFileInfo = prefs.getBoolean("alwaysShowDetailedFileInfo", false);
		if (!alwaysShowDetailedFileInfo)
		{
			layoutTone.setVisibility(View.GONE);
			layoutSetting.setVisibility(View.GONE);
			layoutConsent.setVisibility(View.GONE);
			layoutDS.setVisibility(View.GONE);
			layoutHypnosisStyle.setVisibility(View.GONE);
			layoutHypnosisLevel.setVisibility(View.GONE);
			layoutWakener.setVisibility(View.GONE);
			layoutAftercare.setVisibility(View.GONE);
			txtTriggersHeader.setVisibility(View.GONE);
			triggers.setVisibility(View.GONE);
		}
		final Button btnMoreInfo = findViewById(R.id.btnMoreInfo);
		btnMoreInfo.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				String text = btnMoreInfo.getText().toString();
				boolean show = text.equals("Show More Info");
				int visibility = show ? View.VISIBLE : View.GONE;
				layoutTone.setVisibility(visibility);
				layoutSetting.setVisibility(visibility);
				layoutConsent.setVisibility(visibility);
				layoutDS.setVisibility(visibility);
				if (file.getHypnosisStyle() != null)
				{
					layoutHypnosisStyle.setVisibility(visibility);
					layoutHypnosisLevel.setVisibility(visibility);
					layoutWakener.setVisibility(visibility);
					layoutAftercare.setVisibility(visibility);
				}
				if (file.getTriggers() != null && !file.getTriggers().isEmpty())
				{
					txtTriggersHeader.setVisibility(visibility);
					triggers.setVisibility(visibility);
				}
				if (show)
				{
					btnMoreInfo.setText("Show Less Info");
				}
				else
				{
					btnMoreInfo.setText("Show More Info");
				}
			}
		});
		if (file.getVersion() >= 3)
		{
			txtTone.setText(file.getTone());
			txtSetting.setText(file.getSetting());
			txtConsent.setText(file.getConsentType());
			txtDS.setText(file.getDSType());
			txtHypnosisStyle.setText(file.getHypnosisStyle());
			txtHypnosisLevel.setText(file.getHypnosisLevel());
			int hypnosisLevelColor = getHypnosisLevelColor();
			if (hypnosisLevelColor != -1)
			{
				txtHypnosisLevel.setTextColor(hypnosisLevelColor);
			}
			txtWakener.setText(file.hasWakener() ? "Yes" : "No");
			txtAftercare.setText(file.hasAftercare() ? "Yes" : "No");
			System.out.println(file.getTriggers().size());
			if (file.getTriggers() != null && !file.getTriggers().isEmpty())
			{
				triggers.setTags(file.getTriggers());
			}
			else
			{
//				txtTriggersHeader.setVisibility(View.GONE);
				txtTriggersHeader.setText("No Triggers");
				triggers.setVisibility(View.GONE);
			}
			
			if (file.getHypnosisStyle() == null)
			{
				layoutHypnosisStyle.setVisibility(View.GONE);
				layoutHypnosisLevel.setVisibility(View.GONE);
				layoutWakener.setVisibility(View.GONE);
				layoutAftercare.setVisibility(View.GONE);
			}
		}
		else
		{
			btnMoreInfo.setVisibility(View.GONE);
			layoutTone.setVisibility(View.GONE);
			layoutSetting.setVisibility(View.GONE);
			layoutConsent.setVisibility(View.GONE);
			layoutDS.setVisibility(View.GONE);
			layoutHypnosisStyle.setVisibility(View.GONE);
			layoutHypnosisLevel.setVisibility(View.GONE);
			layoutWakener.setVisibility(View.GONE);
			layoutAftercare.setVisibility(View.GONE);
			txtTriggersHeader.setVisibility(View.GONE);
			triggers.setVisibility(View.GONE);
		}
		
		ImageButton btnPlay = findViewById(R.id.btnPlay);
		btnPlay.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				AudioController audioController = mainActivity.getAudioController();
				audioController.loadFile(file, queueName);
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
						file.getViewType().equals("user")))
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
					if (file.getViewType().equals("user"))
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
											if (file.getViewType().equals("user"))
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
		ImageButton btnExport = findViewById(R.id.btnExport);
		btnExport.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				File fileOnDisk = AudioDownloadManager
						.getFileLocation(mainActivity, file);
				
				
//				sendIntent.setType(getMimeType(fileOnDisk.getAbsolutePath()));
//				sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileOnDisk));
				sendIntent.putExtra(Intent.EXTRA_TITLE, "TEST");


				Uri uri = FileProvider.getUriForFile(mainActivity, mainActivity
								.getPackageName() + ".provider", fileOnDisk);
				sendIntent.setDataAndType(uri, getMimeType(fileOnDisk.getAbsolutePath()));
				sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				
				mainActivity.startActivity(sendIntent);
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
			btnExport.setColorFilter(ContextCompat
					.getColor(mainActivity, R.color.grayLight));
			btnAddToPlaylist.setColorFilter(ContextCompat
					.getColor(mainActivity, R.color.grayLight));
		}
		else
		{
			btnPlay.setColorFilter(null);
			btnDownload.setColorFilter(null);
			btnExport.setColorFilter(null);
			btnAddToPlaylist.setColorFilter(null);
		}
		
		if (mainActivity.getDownloadManager().isDownloadingFile(file))
		{
			btnDownload.setColorFilter(ContextCompat
					.getColor(mainActivity, R.color.redAccent));
		}
		else if (AudioDownloadManager.fileIsDownloaded(mainActivity, file) ||
				file.getViewType().equals("user"))
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
	
	private String getAudioInfoText()
	{
		String text = "";
		if (!file.getAudioFileType().equals("N/A"))
		{
			text += file.getAudioFileType();
		}
		if (!file.getAudioType().equals("N/A"))
		{
			if (!text.equals(""))
			{
				text += AUDIO_INFO_SEPARATOR;
			}
			text += file.getAudioType();
		}
		if (!file.getAudioBackground().equals("N/A"))
		{
			if (!text.equals(""))
			{
				text += AUDIO_INFO_SEPARATOR;
			}
			text += file.getAudioBackground();
		}
		return text;
	}
	
	private int getHypnosisLevelColor()
	{
		if (file.getHypnosisLevel() != null)
		{
			switch (file.getHypnosisLevel())
			{
				case "Beginner": return Color.GREEN;
				case "Intermediate": return Color.YELLOW;
				case "Advanced": return Color.RED;
			}
		}
		return -1;
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
	
	private String getMimeType(String url)
	{
		String type = null;
		String extension = MimeTypeMap.getFileExtensionFromUrl(url);
		if (extension != null) {
			type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
		}
		return type;
	}
}
