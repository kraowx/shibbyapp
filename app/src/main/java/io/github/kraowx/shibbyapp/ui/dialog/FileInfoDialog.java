package io.github.kraowx.shibbyapp.ui.dialog;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.util.List;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.models.ShibbyFile;

public class FileInfoDialog extends Dialog
{
	public FileInfoDialog(MainActivity mainActivity, ShibbyFile file)
	{
		super(mainActivity);
		init(mainActivity, file);
	}
	
	private void init(MainActivity mainActivity, ShibbyFile file)
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.file_info_dialog);
		setTitle("File Info");
		TextView title = findViewById(R.id.txtTitle);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mainActivity);
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
		/* Tags */
		TextView tags = findViewById(R.id.txtTags);
		if (file.getTags() == null ||
				(file.getTags() != null && file.getTags().isEmpty()))
		{
			tags.setText(Html.fromHtml("<i>No tags</i>"));
		}
		else
		{
			tags.setText(getTagsString(file.getTags()));
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
		show();
	}
	
	private String getTagsString(List<String> tags)
	{
		String tagsStr = "";
		for (int i = 0; i < tags.size(); i++)
		{
			tagsStr += tags.get(i);
			if (i < tags.size()-1)
			{
				tagsStr += "  |  ";
			}
		}
		return tagsStr + "\n";
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
