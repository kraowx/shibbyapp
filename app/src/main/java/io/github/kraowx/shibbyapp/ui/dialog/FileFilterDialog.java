package io.github.kraowx.shibbyapp.ui.dialog;

import android.app.Dialog;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.tools.PatreonTier;

public class FileFilterDialog extends Dialog
{
	private CheckBox checkBoxDurationShort, checkBoxDurationMedium,
			checkBoxDurationLong, checkBoxDurationVeryLong;
	private final int[] DURATIONS = {0, 20, 40, 60};
	private CheckBox checkBoxTypeSoundgasm, checkBoxTypePatreon,
			checkBoxTypeUser;
	private EditText txtTags;
	
	public FileFilterDialog(MainActivity mainActivity)
	{
		super(mainActivity);
		initUI(mainActivity);
	}
	
	public int[] getDurations()
	{
		final CheckBox[] durationCheckboxes = {checkBoxDurationShort,
				checkBoxDurationMedium, checkBoxDurationLong, checkBoxDurationVeryLong};
		List<Integer> durations = new ArrayList<Integer>();
		for (int i = 0; i < durationCheckboxes.length; i++)
		{
			if (durationCheckboxes[i].isChecked())
			{
				durations.add(DURATIONS[i]);
			}
		}
		int[] durationsArr = new int[durations.size()];
		for (int i = 0; i < durationsArr.length; i++)
		{
			durationsArr[i] = durations.get(i);
		}
		return durationsArr;
	}
	
	public int[] getFileTypes()
	{
		List<Integer> types = new ArrayList<Integer>();
		if (checkBoxTypeSoundgasm.isChecked())
		{
			types.add(PatreonTier.FREE);
		}
		if (checkBoxTypePatreon.isChecked())
		{
			types.add(PatreonTier.HYPNOSUB);
		}
		if (checkBoxTypeUser.isChecked())
		{
			types.add(PatreonTier.HYPNOSLAVE);
		}
		if (checkBoxTypeUser.isChecked())
		{
			types.add(PatreonTier.HYPNOSLUT);
		}
		int[] typesInt = new int[types.size()];
		for (int i = 0; i < types.size(); i++)
		{
			typesInt[i] = types.get(i);
		}
		return typesInt;
	}
	
	public String[] getTags()
	{
		String tags = txtTags.getText().toString().replace(", ", ",");
		return !tags.isEmpty() ? tags.split(",") : new String[0];
	}
	
	private void initUI(MainActivity mainActivity)
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.filter_dialog);
		checkBoxDurationShort = findViewById(R.id.checkBoxDurationShort);
		checkBoxDurationMedium = findViewById(R.id.checkBoxDurationMedium);
		checkBoxDurationLong = findViewById(R.id.checkBoxDurationLong);
		checkBoxDurationVeryLong = findViewById(R.id.checkBoxDurationVeryLong);
		
		checkBoxTypeSoundgasm = findViewById(R.id.checkBoxTypeSoundgasm);
		checkBoxTypePatreon = findViewById(R.id.checkBoxTypePatreon);
		checkBoxTypeUser = findViewById(R.id.checkBoxTypeUser);
		
		txtTags = findViewById(R.id.txtTags);
	}
}
