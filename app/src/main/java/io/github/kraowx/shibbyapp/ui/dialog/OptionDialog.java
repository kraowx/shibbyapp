package io.github.kraowx.shibbyapp.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;

import androidx.core.content.ContextCompat;

public class OptionDialog extends AlertDialog
{
	public OptionDialog(Context context, String title, String message,
						String label1, String label2, int theme, int icon,
						DialogInterface.OnClickListener action1,
						DialogInterface.OnClickListener action2)
	{
		super(context, theme);
		setTitle(title);
		setMessage(message);
		setButton(AlertDialog.BUTTON_POSITIVE, label1, action1);
		setButton(AlertDialog.BUTTON_NEGATIVE, label2, action2);
		Drawable darkIcon = ContextCompat.getDrawable(
				context, icon).mutate();
		darkIcon.setColorFilter(new ColorMatrixColorFilter(new float[]
				{
						-1, 0, 0, 0, 200,
						0, -1, 0, 0, 200,
						0, 0, -1, 0, 200,
						0, 0, 0, 1, 0
				}));
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean darkModeEnabled = prefs.getBoolean(
				"darkMode", false);
		AlertDialog.Builder builder;
		if (darkModeEnabled)
		{
			setIcon(darkIcon);
		}
		else
		{
			setIcon(icon);
		}
	}
}
