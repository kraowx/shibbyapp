package io.github.kraowx.shibbyapp.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;

@Deprecated
public class ServerSelectorDialog extends Dialog
{
	private ServerSelectedListener serverSelectedListener;
	
	public ServerSelectorDialog(MainActivity mainActivity)
	{
		super(mainActivity);
		initUI(mainActivity);
	}
	
	public interface ServerSelectedListener
	{
		void onServerSelected();
	}
	
	public void setServerSelectedListener(ServerSelectedListener listener)
	{
		serverSelectedListener = listener;
	}
	
	private void initUI(final MainActivity mainActivity)
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.server_selector_dialog);
		setTitle("Select Server");
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mainActivity);
		final SharedPreferences.Editor editor = prefs.edit();
		Button btnDefault = findViewById(R.id.btnDefault);
		btnDefault.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				editor.putString("server", mainActivity.getString(R.string.main_server));
				editor.commit();
				if (serverSelectedListener != null)
				{
					serverSelectedListener.onServerSelected();
				}
				ServerSelectorDialog.this.dismiss();
			}
		});
		Button btnCustom = findViewById(R.id.btnCustom);
		btnCustom.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showCustomServerInputDialog(mainActivity);
			}
		});
		show();
	}
	
	private void showCustomServerInputDialog(final MainActivity mainActivity)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle("Server address");
		
		final EditText input = new EditText(getContext());
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		input.setHint("IP:port");
		builder.setView(input);
		
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(mainActivity);
				final SharedPreferences.Editor editor = prefs.edit();
				editor.putString("server", input.getText().toString());
				editor.commit();
				if (serverSelectedListener != null)
				{
					serverSelectedListener.onServerSelected();
				}
				ServerSelectorDialog.this.dismiss();
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.cancel();
			}
		});
		builder.show();
	}
}
