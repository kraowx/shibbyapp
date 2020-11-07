package io.github.kraowx.shibbyapp.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ShibbyAudioInfo {
	private String fileType;
	private String audioType;
	private String audioUrl;
	private String freeAudioUrl;
	private String effects;
	private String background;
	
	public static ShibbyAudioInfo fromHTML(Document doc) {
		Elements tableInfo = doc.select("dd[class*=col-sm-3]");
		ShibbyAudioInfo audioInfo = new ShibbyAudioInfo();
		audioInfo.fileType = tableInfo.get(8).text();
		audioInfo.audioType = tableInfo.get(9).text();
		try {
			audioInfo.freeAudioUrl = doc.select("source").attr("src");
		}
		catch (Exception e) {
			audioInfo.freeAudioUrl = null; // always null for patreon files
		}
		audioInfo.effects = tableInfo.get(10).text();
		audioInfo.background = tableInfo.get(11).text();
		return audioInfo;
	}
	
	public String getFileType() {
		return fileType;
	}
	
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	
	public String getAudioType() {
		return audioType;
	}
	
	public void setAudioType(String audioType) {
		this.audioType = audioType;
	}
	
	public String getAudioURL() {
		return audioUrl;
	}
	
	public void setAudioURL(String audioUrl) {
		this.audioUrl = audioUrl;
	}
	
	public String getFreeAudioURL() {
		return freeAudioUrl;
	}
	
	public void setFreeAudioURL(String freeAudioUrl) {
		this.freeAudioUrl = freeAudioUrl;
	}
	
	public String getEffects() {
		return effects;
	}
	
	public void setEffects(String effects) {
		this.effects = effects;
	}
	
	public String getBackground() {
		return background;
	}
	
	public void setBackground(String background) {
		this.background = background;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("file_type", fileType);
			json.put("audio_type", audioType);
			json.put("audio_url", audioUrl);
			json.put("free_audio_url", freeAudioUrl);
			json.put("effects", effects);
			json.put("background", background);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
	
	public static ShibbyAudioInfo fromJSON(JSONObject json) {
		ShibbyAudioInfo audioInfo = new ShibbyAudioInfo();
		try {
			audioInfo.fileType = json.has("file_type") ? json.getString("file_type") : null;
			audioInfo.audioType = json.has("audio_type") ? json.getString("audio_type") : null;
			audioInfo.audioUrl = json.has("audio_url") ? json.getString("audio_url") : null;
			audioInfo.freeAudioUrl = json.has("free_audio_url") ? json.getString("free_audio_url") : null;
			audioInfo.effects = json.has("effects") ? json.getString("effects") : null;
			audioInfo.background = json.has("background") ? json.getString("background") : null;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return audioInfo;
	}
}
