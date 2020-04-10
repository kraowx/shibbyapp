package io.github.kraowx.shibbyapp.ui.dialog;

import android.app.Dialog;
import android.view.Window;
import android.widget.TextView;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;

public class PatreonRefreshInfoDialog extends Dialog
{
	private TextView txtDescriptionText;
	
	public PatreonRefreshInfoDialog(MainActivity mainActivity)
	{
		super(mainActivity);
		initUI();
	}
	
	public void setDescriptionText(String text)
	{
		txtDescriptionText.setText(text);
	}
	
	private void initUI()
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.patreon_refresh_info_dialog);
		txtDescriptionText = findViewById(R.id.txtDescription);
	}
}
