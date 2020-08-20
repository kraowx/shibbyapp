package io.github.kraowx.shibbyapp.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ShibbyBasicInfo {
	private String author;
	private String artist;
	private String release;
	private String audience;
	private String tone;
	private String setting;
	private String consent;
	private String ds;
	private String orgasm;
	private String instructions;
	private String intendedEffect;
	
	public static ShibbyBasicInfo fromHTML(Document doc) {
		Elements tableInfo1 = doc.select("dd[class*=col-sm-3]");
		ShibbyBasicInfo basicInfo = new ShibbyBasicInfo();
		basicInfo.author = tableInfo1.get(0).text();
		basicInfo.artist = tableInfo1.get(1).text();
		basicInfo.release = tableInfo1.get(2).text();
		basicInfo.audience = tableInfo1.get(3).text();
		basicInfo.tone = tableInfo1.get(4).text();
		basicInfo.setting = tableInfo1.get(5).text();
		basicInfo.consent = tableInfo1.get(6).text();
		basicInfo.ds = tableInfo1.get(7).text();
		Elements tableInfo2 = doc.select("dd[class*=col-sm-9]");
		basicInfo.orgasm = tableInfo2.get(0).text();
		basicInfo.instructions = tableInfo2.get(1).text();
		basicInfo.intendedEffect = tableInfo2.get(2).text();
		return basicInfo;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getArtist() {
		return artist;
	}
	
	public void setArtist(String artist) {
		this.artist = artist;
	}
	
	public String getRelease() {
		return release;
	}
	
	public void setRelease(String release) {
		this.release = release;
	}
	
	public String getAudience() {
		return audience;
	}
	
	public void setAudience(String audience) {
		this.audience = audience;
	}
	
	public String getTone() {
		return tone;
	}
	
	public void setTone(String tone) {
		this.tone = tone;
	}
	
	public String getSetting() {
		return setting;
	}
	
	public void setSetting(String setting) {
		this.setting = setting;
	}
	
	public String getConsent() {
		return consent;
	}
	
	public void setConsent(String consent) {
		this.consent = consent;
	}
	
	public String getDS() {
		return ds;
	}
	
	public void setDS(String ds) {
		this.ds = ds;
	}
	
	public String getOrgasm() {
		return orgasm;
	}
	
	public void setOrgasm(String orgasm) {
		this.orgasm = orgasm;
	}
	
	public String getInstructions() {
		return instructions;
	}
	
	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}
	
	public String getIntendedEffect() {
		return intendedEffect;
	}
	
	public void setIntendedEffect(String intendedEffect) {
		this.intendedEffect = intendedEffect;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("author", author);
			json.put("artist", artist);
			json.put("release", release);
			json.put("audience", audience);
			json.put("tone", tone);
			json.put("setting", setting);
			json.put("consent", consent);
			json.put("ds", ds);
			json.put("orgasm", orgasm);
			json.put("instructions", instructions);
			json.put("intended_effect", intendedEffect);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
	
	public static ShibbyBasicInfo fromJSON(JSONObject json) {
		ShibbyBasicInfo basicInfo = new ShibbyBasicInfo();
		try {
			basicInfo.author = json.has("author") ? json.getString("author") : null;
			basicInfo.artist = json.has("artist") ? json.getString("artist") : null;
			basicInfo.release = json.has("release") ? json.getString("release") : null;
			basicInfo.audience = json.has("audience") ? json.getString("audience") : null;
			basicInfo.tone = json.has("tone") ? json.getString("tone") : null;
			basicInfo.setting = json.has("setting") ? json.getString("setting") : null;
			basicInfo.consent = json.has("consent") ? json.getString("consent") : null;
			basicInfo.ds = json.has("ds") ? json.getString("ds") : null;
			basicInfo.orgasm = json.has("orgasm") ? json.getString("orgasm") : null;
			basicInfo.instructions = json.has("instructions") ? json.getString("instructions") : null;
			basicInfo.intendedEffect = json.has("intended_effect") ? json.getString("intended_effect") : null;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return basicInfo;
	}
}
