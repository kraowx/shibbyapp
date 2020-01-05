package io.github.kraowx.shibbyapp.tools;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.net.Request;
import io.github.kraowx.shibbyapp.net.RequestType;
import io.github.kraowx.shibbyapp.net.Response;

public class PatreonSessionManager
{
	private String sessionCookie;
	private MainActivity mainActivity;
	private SharedPreferences prefs;
	
	public PatreonSessionManager(MainActivity mainActivity)
	{
		this.mainActivity = mainActivity;
		prefs = PreferenceManager.getDefaultSharedPreferences(mainActivity);
	}
	
	public String getCookie()
	{
		return sessionCookie;
	}
	
	public boolean isAuthenticated()
	{
		return sessionCookie != null;
	}
	
	public String generateCookie(String email, String password)
	{
		HttpRequest req = HttpRequest.post("https://api.patreon.com/login");
		req.header("Connection", "keep-alive");
		req.header("Upgrade-Insecure-Requests", 1);
		req.userAgent("Mozilla/5.0 (Linux; <Android Version>; <Build Tag etc.>) AppleWebKit/<WebKit Rev> (KHTML, like Gecko) Chrome/<Chrome Rev> Mobile Safari/<WebKit Rev>");
//        req.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.86 Safari/537.36");
		req.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		req.header("Accept-Language", "en-US,en;q=0.9");
		req.send("{\"data\":{\"email\":\"" + email + "\"," +
				"\"password\":\"" + password + "\"}}");
		List<String> cookies = req.headers().get("Set-Cookie");
		sessionCookie = "";
		for (int i = 0; i < cookies.size(); i++)
		{
			sessionCookie += cookies.get(i);
			if (i < cookies.size()-1)
			{
				sessionCookie += "; ";
			}
		}
		return sessionCookie;
	}
	
	/*
	 * Connects to the server to check if the given credentials are valid.
	 * Returns 1 if valid, 0 if invalid, or 2 if the server does
	 * not support Patreon integration
	 */
	public int verifyCredentials(String email, String password)
	{
		try
		{
			String[] server = prefs.getString("server",
					"shibbyserver.ddns.net:2012").split(":");
			String hostname = server[0];
			int port = -1;
			try
			{
				port = Integer.parseInt(server[1]);
			}
			catch (NumberFormatException nfe)
			{
				nfe.printStackTrace();
			}
			if (!hostname.isEmpty() && port != -1)
			{
				Socket socket = new Socket(hostname, port);
				PrintWriter writer =
						new PrintWriter(socket.getOutputStream(), true);
				BufferedReader reader =
						new BufferedReader(
								new InputStreamReader(socket.getInputStream()));
				JSONObject requestData = new JSONObject();
				try
				{
					requestData.put("email", email);
					requestData.put("password", password);
				}
				catch (JSONException je)
				{
					je.printStackTrace();
				}
				writer.println(new Request(
						RequestType.VERIFY_PATREON_ACCOUNT, requestData));
				String data;
				while ((data = reader.readLine()) != null)
				{
					try
					{
						Response resp = Response.fromJSON(data);
						switch (resp.getType())
						{
							case VERIFY_PATREON_ACCOUNT:
								JSONObject json = resp.getData().getJSONObject(0);
								return json.getBoolean("verified") ? 1 : 0;
							case FEATURE_NOT_SUPPORTED:
								return 2;
						}
					}
					catch (JSONException je)
					{
						je.printStackTrace();
					}
				}
			}
			else
			{
				showToast("Server is invalid");
			}
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			showToast("Server connection error");
		}
		return 0;
	}
	
	private void showToast(final String message)
	{
		mainActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				Toast.makeText(mainActivity, message,
						Toast.LENGTH_LONG).show();
			}
		});
	}
}
