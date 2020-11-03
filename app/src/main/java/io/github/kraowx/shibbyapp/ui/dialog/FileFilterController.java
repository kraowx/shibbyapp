package io.github.kraowx.shibbyapp.ui.dialog;

import android.content.DialogInterface;
import android.view.MenuItem;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.tools.PatreonTier;

public class FileFilterController
{
	public static int[] DEFAULT_FILE_TYPES = {PatreonTier.FREE, PatreonTier.DRIFTING,
			PatreonTier.HYPNOSUB, PatreonTier.HYPNOSLAVE, PatreonTier.HYPNOSLUT,
			PatreonTier.DEVOTED_PET, PatreonTier.WORSHIPFUL_SUBJECT};  // all files types
	public static int[] DEFAULT_DURATIONS = {0, 20, 40, 60};  // all durations
	
	private int[] fileTypes;
	private String[] tags;
	private int[] durations;
	private FilterListener listener;
	private FileFilterDialog dialog;
	private MenuItem menuItem;
	
	public interface FilterListener
	{
		void filtersUpdated(int[] fileTypes, int[] duration, String[] tags);
	}
	
	public FileFilterController(MainActivity mainActivity)
	{
		fileTypes = DEFAULT_FILE_TYPES;
		durations = DEFAULT_DURATIONS;
		tags = new String[0];
		dialog = new FileFilterDialog(mainActivity);
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
		{
			@Override
			public void onDismiss(DialogInterface dialog)
			{
				if (listener != null)
				{
					fileTypes = FileFilterController.this.dialog.getFileTypes();
					durations = FileFilterController.this.dialog.getDurations();
					tags = FileFilterController.this.dialog.getTags();
					listener.filtersUpdated(fileTypes, durations, tags);
				}
			}
		});
	}
	
	public void setListener(FilterListener listener)
	{
		this.listener = listener;
	}
	
	public void setMenuItem(MenuItem menuItem)
	{
		this.menuItem = menuItem;
	}
	
	public void showDialog()
	{
		if (!dialog.isShowing())
		{
			dialog.show();
		}
	}
	
	public void setButtonVisible(boolean visible)
	{
		if (menuItem != null)
		{
			menuItem.setVisible(visible);
		}
	}
}
