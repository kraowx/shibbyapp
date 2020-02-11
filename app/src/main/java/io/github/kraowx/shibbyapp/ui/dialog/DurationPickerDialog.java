package io.github.kraowx.shibbyapp.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

import io.github.kraowx.shibbyapp.R;
import mobi.upod.timedurationpicker.TimeDurationPicker;

public class DurationPickerDialog extends Dialog
{
	public DurationPickerDialog(Context context, long length,
								TimeDurationPicker.OnDurationChangedListener listener)
	{
		super(context);
		initUI(listener);
	}
	
	private void initUI(TimeDurationPicker.OnDurationChangedListener listener)
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.duration_picker_dialog);
		TimeDurationPicker durationPicker =
				(TimeDurationPicker)findViewById(R.id.durationPicker);
		durationPicker.setOnDurationChangeListener(listener);
		show();
	}
}
