package io.github.kraowx.shibbyapp.models;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.github.kraowx.shibbyapp.tools.PatreonTier;

public class ShibbyDexUserInfo {
	private PatreonTier patreonTier;
	private Date patreonLastVerifyDate;
	private Date patreonPledgeStartDate;
	private boolean activePatron;
	private Date patreonLastChargeDate;
	
	public static ShibbyDexUserInfo fromHTML(Document doc) {
		ShibbyDexUserInfo userInfo = new ShibbyDexUserInfo();
		Elements cardTexts = doc.select("p[class*=card-text]");
		String tier = getTextByKey("your tier is:", cardTexts);
		tier = tier.substring(tier.indexOf("your tier is:")+14).toLowerCase();
		userInfo.patreonTier = tier != null ? PatreonTier.fromString(tier) : new PatreonTier(PatreonTier.FREE);
		
		String lastVerifyDateStr = getTextByKey("Last verify date:", cardTexts);
		lastVerifyDateStr = lastVerifyDateStr.substring(lastVerifyDateStr.indexOf("Last verify date:")+18);
		userInfo.patreonLastVerifyDate = parseDate(lastVerifyDateStr);
		
		String pledgeStartDateStr = getTextByKey("Pledge start date:", cardTexts);
		pledgeStartDateStr = pledgeStartDateStr.substring(pledgeStartDateStr.indexOf("Pledge start date:")+19);
		userInfo.patreonPledgeStartDate = parseDate(pledgeStartDateStr);
		
		String status = getTextByKey("Patron status:", cardTexts);
		status = status.substring(status.indexOf("Patron status:")+15);
		userInfo.activePatron = status.equals("active_patron");
		
		String lastChargeDateStr = getTextByKey("Last charge date:", cardTexts);
		lastChargeDateStr = lastChargeDateStr.substring(lastChargeDateStr.indexOf("Last charge date:")+18);
		userInfo.patreonLastChargeDate = parseDate(lastChargeDateStr);
		return userInfo;
	}
	
	private static String getTextByKey(String key, Elements cardTexts) {
		for (Element text : cardTexts) {
			if (text.text().contains(key)) {
				return text.text();
			}
		}
		return null;
	}
	
	private static Date parseDate(String date) {
		if (date == null)
			return null;
		try {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
		}
		catch (ParseException e) {
			return null;
		}
	}
	
	public PatreonTier getPatreonTier() {
		return patreonTier;
	}
	
	public Date getPatreonLastVerifyDate() {
		return patreonLastVerifyDate;
	}
	
	public Date getPatreonPledgeStartDate() {
		return patreonPledgeStartDate;
	}
	
	public boolean isActivePatron() {
		return activePatron;
	}
	
	public Date getPatreonLastChargeDate() {
		return patreonLastChargeDate;
	}
}
