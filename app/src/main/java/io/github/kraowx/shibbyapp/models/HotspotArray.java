package io.github.kraowx.shibbyapp.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HotspotArray
{
	private String fileId;
	private Hotspot[] hotspots;
	
	public HotspotArray(String fileId, Hotspot[] hotspots)
	{
		this.fileId = fileId;
		this.hotspots = hotspots;
	}
	
	public String getFileId()
	{
		return fileId;
	}
	
	public void setFileId(String fileId)
	{
		this.fileId = fileId;
	}
	
	public Hotspot[] getHotspots()
	{
		return hotspots;
	}
	
	public void setHotspots(Hotspot[] hotspots)
	{
		this.hotspots = hotspots;
	}
	
	public String toJSON()
	{
		JSONObject obj = new JSONObject();
		try
		{
			obj.put("id", fileId);
			JSONArray arr = new JSONArray();
			for (int i = 0; i < hotspots.length; i++)
			{
				arr.put(new JSONObject(hotspots[i].toJSON()));
			}
			obj.put("hotspots", arr);
		}
		catch (JSONException je)
		{
			je.printStackTrace();
		}
		return obj.toString();
	}
	
	public static HotspotArray fromJSON(JSONObject json)
	{
		HotspotArray hotspotArray = new HotspotArray(null, null);
		try
		{
			if (json.has("id"))
			{
				hotspotArray.fileId = json.getString("id");
			}
			if (json.has("hotspots"))
			{
				JSONArray arr = json.getJSONArray("hotspots");
				hotspotArray.hotspots = new Hotspot[arr.length()];
				for (int i = 0; i < arr.length(); i++)
				{
					hotspotArray.hotspots[i] = Hotspot.fromJSON(arr.getJSONObject(i));
				}
			}
		}
		catch (JSONException je)
		{
			je.printStackTrace();
		}
		return hotspotArray;
	}
}
