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
import io.github.kraowx.shibbyapp.models.ShibbyDexUserInfo;
import io.github.kraowx.shibbyapp.net.Request;
import io.github.kraowx.shibbyapp.net.RequestType;
import io.github.kraowx.shibbyapp.net.Response;
import io.github.kraowx.shibbyapp.net.ShibbyDexClient;

public class PatreonSessionManager
{
	private ShibbyDexUserInfo userInfo;
	private ShibbyDexClient shibbyDexClient;
	private MainActivity mainActivity;
	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;
	
	public PatreonSessionManager(MainActivity mainActivity)
	{
		this.mainActivity = mainActivity;
		shibbyDexClient = new ShibbyDexClient();
		prefs = PreferenceManager.getDefaultSharedPreferences(mainActivity);
		editor = prefs.edit();
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				authenticate();
			}
		}).start();
	}
	
	public String getCookie()
	{
		return shibbyDexClient.getAuthCookie();
	}
	
	public PatreonTier getTier()
	{
		return userInfo != null ? userInfo.getPatreonTier() :
				new PatreonTier(PatreonTier.FREE);
	}
	
	public ShibbyDexUserInfo getUserInfo()
	{
		return userInfo;
	}
	
	public boolean isAuthenticated()
	{
		String email = prefs.getString("shibbydexEmail", null);
		String password = prefs.getString("shibbydexPassword", null);
		if (email != null && password != null && shibbyDexClient.authenticate(email, password))
		{
			userInfo = shibbyDexClient.getUserInfo();
			return true;
		}
		return false;
	}
	
	public boolean authenticate(String email, String password)
	{
		if (email != null && password != null)
		{
			editor.putString("shibbydexEmail", email);
			editor.putString("shibbydexPassword", password);
			if (shibbyDexClient.authenticate(email, password))
			{
				editor.putString("shibbydexAuthCookie", shibbyDexClient.getAuthCookie());
				editor.commit();
				userInfo = shibbyDexClient.getUserInfo();
				return true;
			}
		}
		return false;
	}
	
	private boolean authenticate()
	{
		String email = prefs.getString("shibbydexEmail", null);
		String password = prefs.getString("shibbydexPassword", null);
		System.out.println("SHIBBYDEX CREDS: " + email + " " + password);
		return authenticate(email, password);
	}
	
//	public String generateCookie(String email, String password)
//	{
//		HttpRequest req = getLoginRequest(email, password);
//		List<String> cookies = req.headers().get("Set-Cookie");
//		sessionCookie = "";
//		for (int i = 0; i < cookies.size(); i++)
//		{
//			sessionCookie += cookies.get(i);
//			if (i < cookies.size()-1)
//			{
//				sessionCookie += "; ";
//			}
//		}
//		req.closeOutputQuietly();
//		editor.putString("patreonSessionCookie", sessionCookie);
//		editor.commit();
//		return sessionCookie;
//	}
//
//	/*
//	 * Connects to the server to check if the given credentials are valid.
//	 * Returns 1 if valid, 0 if invalid, 2 if email verification is required,
//	 * or 3/4 if too many requests are sent (3 for 10 min timeout or 4 for
//	 * 30 min timeout)
//	 */
//	public int verifyCredentials(String email, String password)
//	{
//		HttpRequest req = getLoginRequest(email, password);
//		int code = req.code();
//		if (code == 400)
//		{
//			return 0;
//		}
//		else if (code == 401)
//		{
//			return 2;
//		}
//		else if (code == 429)
//		{
//			try
//			{
//				JSONObject errorData = new JSONObject(req.body());
//				if (errorData.getJSONArray("errors")
//						.getJSONObject(0).getString("detail")
//						.contains("half an hour"))
//				{
//					return 4;
//				}
//			}
//			catch (JSONException je)
//			{
//				je.printStackTrace();
//			}
//			return 3;
//		}
//		/* Note: 403 means cloudflare block */
//		else if (code == 200)
//		{
//			final int VERIFICATION_POST_ID = 28614620;
//			req = HttpRequest.post(
//					"https://api.patreon.com/posts/" + VERIFICATION_POST_ID);
//			req.header("Connection", "keep-alive");
//			req.header("Upgrade-Insecure-Requests", 1);
//			req.userAgent("Mozilla/5.0 (Linux; <Android Version>; <Build Tag etc.>) AppleWebKit/<WebKit Rev> (KHTML, like Gecko) Chrome/<Chrome Rev> Mobile Safari/<WebKit Rev>");
////        req.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.86 Safari/537.36");
//			req.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
//			req.header("Accept-Language", "en-US,en;q=0.9");
//			req.header("Cookie", sessionCookie);
//			req.send("{\"data\":{\"email\":\"" + email + "\"," +
//					"\"password\":\"" + password + "\"}}");
//			req.closeOutputQuietly();
//			return 1;
//		}
//		req.closeOutputQuietly();
//		return 0;
//	}
//
//	private HttpRequest getLoginRequest(String email, String password)
//	{
//		HttpRequest req = HttpRequest.post("https://api.patreon.com/login");
//		addHeadersToRequest(req);
//		req.send("{\"data\":{\"email\":\"" + email + "\"," +
//				"\"password\":\"" + password + "\"}}");
//		req.closeOutputQuietly();
//		return req;
//	}
//
//	public void addHeadersToRequest(HttpRequest req)
//	{
//		req.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
//		req.header("accept-language", "en-US,en-CA;q=0.9,en-GB;q=0.8,en;q=0.7");
//		req.header("cache-control", "max-age=0");
//		req.header("sec-fetch-mode", "navigate");
//		req.header("sec-fetch-site", "none");
//		req.header("sec-fetch-user", "?1");
//		req.header("upgrade-insecure-requests", 1);
//		req.header("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36");
//		if (sessionCookie != null)
//		{
//			req.header("cookie", sessionCookie);
//		}
//		else
//		{
//			req.header("cookie", "");
//		}
//	}
}
