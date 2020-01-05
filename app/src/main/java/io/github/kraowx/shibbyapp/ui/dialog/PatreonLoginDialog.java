package io.github.kraowx.shibbyapp.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.tools.PatreonSessionManager;

public class PatreonLoginDialog extends Dialog
{
	private LoginListener loginListener;
	
	public PatreonLoginDialog(MainActivity mainActivity)
	{
		super(mainActivity);
		initUI(mainActivity);
	}
	
	public interface LoginListener
	{
		void onLoginVerified(String email, String password);
	}
	
	public void setLoginListener(LoginListener loginListener)
	{
		this.loginListener = loginListener;
	}
	
	private void initUI(final MainActivity mainActivity)
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.patreon_login_dialog);
		final EditText txtEmail = findViewById(R.id.txtEmail);
		final EditText txtPassword = findViewById(R.id.txtPassword);
		Button btnSubmit = findViewById(R.id.btnSubmit);
		btnSubmit.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				final String email = txtEmail.getText().toString();
				final String password = txtPassword.getText().toString();
				final ProgressDialog progressDialog = new ProgressDialog(getContext());
				progressDialog.setMessage("Connecting...");
				progressDialog.show();
				new Thread()
				{
					@Override
					public void run()
					{
						PatreonSessionManager patreonSessionManager =
								mainActivity.getPatreonSessionManager();
						final int INVALID = 0;
						final int VALID = 1;
						final int UNSUPPORTED = 2;
						switch (patreonSessionManager.verifyCredentials(email, password))
						{
							case INVALID:
								setError(txtEmail, "Email or password is invalid");
								setError(txtPassword, "Email or password is invalid");
								break;
							case VALID:
								if (loginListener != null)
								{
									loginListener.onLoginVerified(email, password);
								}
								dismiss();
								break;
							case UNSUPPORTED:
								showUnsupportedDialog(mainActivity);
								break;
						}
						mainActivity.runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								progressDialog.cancel();
							}
						});
					}
				}.start();
			}
		});
		show();
	}
	
	private void showUnsupportedDialog(final MainActivity mainActivity)
	{
		mainActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(mainActivity);
				boolean darkModeEnabled = prefs.getBoolean("darkMode", false);
				AlertDialog.Builder builder;
				if (darkModeEnabled)
				{
					builder = new AlertDialog.Builder(mainActivity, R.style.DialogThemeDark);
				}
				else
				{
					builder = new AlertDialog.Builder(mainActivity);
				}
				builder.setTitle("Unsupported")
						.setMessage("The server you are connected to " +
								"has reported that it does not serve Patreon files.")
						.setCancelable(false)
						.setPositiveButton(android.R.string.ok, null)
						.show();
			}
		});
	}
	
	private void setError(final EditText txtbox, final String message)
	{
		txtbox.post(new Runnable()
		{
			@Override
			public void run()
			{
				txtbox.setError(message);
			}
		});
	}
}
