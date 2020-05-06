package io.github.kraowx.shibbyapp.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import io.github.kraowx.shibbyapp.models.ShibbyFile;

public class PlayCountManager
{
	public static int getPlayCount(ShibbyFile file, Context context)
	{
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		JSONObject playcounts = getPlayCounts(prefs);
		try
		{
			return playcounts.has(file.getId()) ?
					playcounts.getInt(file.getId()) : 0;
		}
		catch (JSONException je)
		{
			je.printStackTrace();
		}
		return 0;
	}
	
	public static void setPlayCount(ShibbyFile file, int count, Context context)
	{
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		JSONObject playcounts = getPlayCounts(prefs);
		try
		{
			playcounts.put(file.getId(), count);
			setPlayCounts(playcounts, prefs);
		}
		catch (JSONException je)
		{
			je.printStackTrace();
		}
	}
	
	public static void incrementPlayCount(ShibbyFile file, Context context)
	{
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		JSONObject playcounts = getPlayCounts(prefs);
		try
		{
			int count = playcounts.has(file.getId()) ?
					playcounts.getInt(file.getId()) : 0;
			playcounts.put(file.getId(), count+1);
			setPlayCounts(playcounts, prefs);
		}
		catch (JSONException je)
		{
			je.printStackTrace();
		}
	}
	
	private static JSONObject getPlayCounts(SharedPreferences prefs)
	{
		JSONObject playcounts = new JSONObject();
		try
		{
			playcounts = new JSONObject(prefs.getString(
					"playCounts", "{}"));
		}
		catch (JSONException je)
		{
			je.printStackTrace();
		}
		return playcounts;
	}
	
	private static void setPlayCounts(JSONObject playcounts, SharedPreferences prefs)
	{
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("playCounts", playcounts.toString());
		editor.commit();
	}
}
