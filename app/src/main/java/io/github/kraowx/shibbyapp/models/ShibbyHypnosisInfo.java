package io.github.kraowx.shibbyapp.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ShibbyHypnosisInfo {
	private String style;
	private String level;
	private String induction;
	private String deepener;
	private String body;
	private boolean wakener;
	private boolean aftercare;
	
	public static ShibbyHypnosisInfo fromHTML(Document doc) {
		ShibbyHypnosisInfo hypnosisInfo = null;
		Elements headers = doc.select("h2[class*=shibbydex-font-accent text-light]");
		String hypnosisInfoHeader = headers.size() == 5 ? headers.get(2).text() : "";
		if (hypnosisInfoHeader.contains("Hypnosis Style:")) {
			hypnosisInfo = new ShibbyHypnosisInfo();
			hypnosisInfo.style = hypnosisInfoHeader.substring(hypnosisInfoHeader.indexOf(":")+2);
			Element container = doc.select("dl[class*=row text-light]").get(1);
			Elements tableInfo2 = container.select("dd[class*=col-sm-9]");
			hypnosisInfo.level = tableInfo2.get(0).text();
			hypnosisInfo.induction = tableInfo2.get(1).text();
			hypnosisInfo.deepener = tableInfo2.get(2).text();
			hypnosisInfo.body = tableInfo2.get(3).text();
			hypnosisInfo.wakener = tableInfo2.get(4).text().equals("Yes") ? true : false;
			hypnosisInfo.aftercare = tableInfo2.get(5).text().equals("Yes") ? true : false;
		}
		return hypnosisInfo;  // null if doesn't exist
	}
	
	public String getStyle() {
		return style;
	}
	
	public void setStyle(String style) {
		this.style = style;
	}
	
	public String getLevel() {
		return level;
	}
	
	public void setLevel(String level) {
		this.level = level;
	}
	
	public String getInduction() {
		return induction;
	}
	
	public void setInduction(String induction) {
		this.induction = induction;
	}
	
	public String getDeepener() {
		return deepener;
	}
	
	public void setDeepener(String deepener) {
		this.deepener = deepener;
	}
	
	public String getBody() {
		return body;
	}
	
	public void setBody(String body) {
		this.body = body;
	}
	
	public boolean hasWakener() {
		return wakener;
	}
	
	public void setWakener(boolean wakener) {
		this.wakener = wakener;
	}
	
	public boolean hasAftercare() {
		return aftercare;
	}
	
	public void setAftercare(boolean aftercare) {
		this.aftercare = aftercare;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("style", style);
			json.put("level", level);
			json.put("induction", induction);
			json.put("deepener", deepener);
			json.put("body", body);
			json.put("wakener", wakener);
			json.put("aftercare", aftercare);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
	
	public static ShibbyHypnosisInfo fromJSON(JSONObject json) {
		ShibbyHypnosisInfo hypnosisInfo = new ShibbyHypnosisInfo();
		try {
			hypnosisInfo.style = json.has("style") ? json.getString("style") : null;
			hypnosisInfo.level = json.has("level") ? json.getString("level") : null;
			hypnosisInfo.induction = json.has("induction") ? json.getString("induction") : null;
			hypnosisInfo.deepener = json.has("deepener") ? json.getString("deepener") : null;
			hypnosisInfo.body = json.has("body") ? json.getString("body") : null;
			hypnosisInfo.wakener = json.has("wakener") ? json.getBoolean("wakener") : false;
			hypnosisInfo.aftercare = json.has("aftercare") ? json.getBoolean("aftercare") : false;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return hypnosisInfo;
	}
}
