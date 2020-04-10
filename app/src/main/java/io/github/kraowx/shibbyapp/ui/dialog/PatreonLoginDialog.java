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
						final int EMAIL = 2;
						final int OVERLOAD_10 = 3;
						final int OVERLOAD_30 = 4;
						switch (patreonSessionManager.verifyCredentials(email, password))
						{
							case INVALID:
								setError(txtEmail, "Email or password is invalid");
								setError(txtPassword, "Email or password is invalid");
								break;
							case VALID:
								if (loginListener != null)
								{
									patreonSessionManager.generateCookie(email, password);
									loginListener.onLoginVerified(email, password);
								}
								dismiss();
								break;
							case EMAIL:
								showEmailVerificationDialog(mainActivity);
								break;
							case OVERLOAD_10:
								showOverloadDialog(mainActivity, 10);
								break;
							case OVERLOAD_30:
								showOverloadDialog(mainActivity, 30);
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
	
	private void showEmailVerificationDialog(MainActivity mainActivity)
	{
		showErrorDialog(mainActivity, "Email Verification",
				"You must confirm your email for this device/location " +
						"through the email sent by Patreon.");
	}
	
	private void showOverloadDialog(MainActivity mainActivity, int timeout)
	{
		showErrorDialog(mainActivity, "Too Many Requests",
				"You have made too many requests to the Patreon server. " +
						"Try again in " + timeout + " minutes.");
	}
	
	private void showUnsupportedDialog(MainActivity mainActivity)
	{
		showErrorDialog(mainActivity, "Unsupported",
				"The server you are connected to " +
						"has reported that it does not support Patreon " +
						"files at the moment.");
	}
	
	private void showErrorDialog(final MainActivity mainActivity,
								 final String title, final String message)
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
				builder.setTitle(title)
						.setMessage(message)
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
