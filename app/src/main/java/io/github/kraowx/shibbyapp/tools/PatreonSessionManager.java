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
		HttpRequest req = getLoginRequest(email, password);
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
		req.closeOutputQuietly();
		return sessionCookie;
	}
	
	/*
	 * Connects to the server to check if the given credentials are valid.
	 * Returns 1 if valid, 0 if invalid, 2 if email verification is required,
	 * or 3 if too many requests are sent
	 */
	public int verifyCredentials(String email, String password)
	{
		HttpRequest req = getLoginRequest(email, password);
		int code = req.code();
		if (code == 400)
		{
			return 0;
		}
		else if (code == 401)
		{
			return 2;
		}
		else if (code == 429)
		{
			return 3;
		}
		else if (code == 200)
		{
			final int VERIFICATION_POST_ID = 28614620;
			req = HttpRequest.post(
					"https://api.patreon.com/posts/" + VERIFICATION_POST_ID);
			req.header("Connection", "keep-alive");
			req.header("Upgrade-Insecure-Requests", 1);
			req.userAgent("Mozilla/5.0 (Linux; <Android Version>; <Build Tag etc.>) AppleWebKit/<WebKit Rev> (KHTML, like Gecko) Chrome/<Chrome Rev> Mobile Safari/<WebKit Rev>");
//        req.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.86 Safari/537.36");
			req.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
			req.header("Accept-Language", "en-US,en;q=0.9");
			req.header("Cookie", sessionCookie);
			req.send("{\"data\":{\"email\":\"" + email + "\"," +
					"\"password\":\"" + password + "\"}}");
			req.closeOutputQuietly();
			return 1;
		}
		req.closeOutputQuietly();
		return 0;
	}
	
	private HttpRequest getLoginRequest(String email, String password)
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
		req.closeOutputQuietly();
		return req;
	}
}
