package io.github.kraowx.shibbyapp.net;

import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
//import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClientBuilder;
//import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import io.github.kraowx.shibbyapp.models.ShibbyDexUserInfo;

public class ShibbyDexClient {
	private final long COOKIE_LIFESPAN = 7200*1000;
	private final long COOKIE_REFRESH = COOKIE_LIFESPAN - 60*1000;
	private final String LOGIN_URL = "https://shibbydex.com/login";
	private final String PROFILE_URL = "https://shibbydex.com/profile";
	private final String HOME_URL = "https://shibbydex.com/home";
	
	private String authCookie;
	private CookieManager cookieManager;
	private Date lastUpdate;
	
	public ShibbyDexClient() {
		cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);
	}
	
	/*
	 * Authenticates with ShibbyDex to allow access to restricted content
	 * including user account info and Patreon files.
	 */
	public boolean authenticate(String email, String password)
	{
		try {
			return updateAuthenticatedCookie(email, password);
		} catch (IOException e) {
			return false;
		}
	}
	
	public String getAuthCookie() {
		return authCookie;
	}
	
	public Document getHTMLResource(String url) throws IOException {
		if (authCookie != null) {
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("cookie", authCookie);
			HttpResponse response = httpGet(url, headers);
			Document doc = null;
			ByteArrayInputStream stream = new ByteArrayInputStream(
					response.getBody().getBytes());
			doc = Jsoup.parse(stream, "utf-8", url);
			return doc;
		}
		throw new IOException("Client is not authenticated!");
	}
	
	public ShibbyDexUserInfo getUserInfo() {
		try {
			return ShibbyDexUserInfo.fromHTML(getHTMLResource(PROFILE_URL));
		}
		catch (IOException | NullPointerException e) {
			return null;
		}
	}
	
	public boolean updateNeeded() {
		Date now = Calendar.getInstance().getTime();
		return lastUpdate == null || lastUpdate.compareTo(now) >= COOKIE_REFRESH;
	}
	
	/*
	 * Creates an authenticated session cookie.
	 * This gives the client access to all areas of ShibbyDex.
	 */
	public boolean updateAuthenticatedCookie(String email, String password) throws IOException {
		String newSessionCookie = getNewSessionCookie();
		String csrfToken = getCSRFToken(newSessionCookie);
		List<NameValuePair> authData = new ArrayList<NameValuePair>();
		authData.add(new BasicNameValuePair("_token", csrfToken));
		authData.add(new BasicNameValuePair("email", email));
		authData.add(new BasicNameValuePair("password", password));
		HttpResponse resp = httpPost(LOGIN_URL,
				getLoginHeaders(newSessionCookie), authData);
//		System.out.println(resp.getBody());
//		System.out.println(resp.getStatus());
//		System.out.println(resp.getCookies());
		lastUpdate = Calendar.getInstance().getTime();
		authCookie = parseAuthCookie(resp);
		System.out.println("RESPONSE_URL: " + resp.getResponseUrl() + "  " + resp.getResponseUrl().equals(HOME_URL) + "  " + email + " " + password);
		return resp.getResponseUrl().equals(HOME_URL);
	}
	
	/*
	 * Parses the CSRF token from the client HTML.
	 * This token will always be the same as long as the same session cookies are sent.
	 */
	private String getCSRFToken(/*CloseableHttpClient httpclient, */String newSessionCookie) throws IOException {
		Map<String, String> headers = getCSRFTokenHeaders(newSessionCookie);
		HttpResponse resp = httpGet(LOGIN_URL, headers);
		String body = resp.getBody();
		if (body != null) {
			return body.substring(body.indexOf("csrf-token")+21, body.indexOf("csrf-token")+61);
		}
		return null;
	}
	
	/*
	 * Requests a new cookie so that all subsequent requests appear from the same client.
	 */
	private String getNewSessionCookie() throws IOException {
		HttpResponse resp = httpGet(LOGIN_URL, null);
		return parseAuthCookie(resp);
	}
	
	private String parseAuthCookie(HttpResponse response) {
		List<HttpCookie> cookies = response.getCookies();
		String cookie = "";
		for (HttpCookie c : cookies) {
			if (c.getName().startsWith("XSRF-TOKEN") || c.getName().startsWith("__Secure-shibbydex_session")) {
				cookie += String.format("%s=%s;", c.getName(), c.getValue());
			}
		}
		return cookie;
	}
	
	private Map<String, String> getCSRFTokenHeaders(String cookie) {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36");
		headers.put("Accept", "*/*");
		headers.put("Cookie", cookie);
		return headers;
	}
	
	private Map<String, String> getLoginHeaders(String cookie) {
		Map<String, String> headers = getCSRFTokenHeaders(cookie);
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		headers.put("Accept-Encoding", "gzip, deflate");
		return headers;
	}
	
	private HttpResponse httpGet(String urlStr, Map<String, String> headers) throws IOException {
		URL url = new URL(urlStr);
		HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
		urlConnection.setRequestMethod("GET");
		HttpResponse packedResponse = null;
		urlConnection.getContent();
		try {
			packedResponse = HttpResponse.fromConnection(urlConnection,
					cookieManager.getCookieStore().getCookies());
		}
		finally {
			urlConnection.disconnect();
		}
		return packedResponse;
	}
	
	private HttpResponse httpPost(String urlStr, Map<String, String> headers,
								  List<NameValuePair> formData) throws IOException {
		URL url = new URL(urlStr);
		HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
		urlConnection.setDoOutput(true);
		urlConnection.setInstanceFollowRedirects(true);
		urlConnection.setRequestMethod("POST");
		String query = buildQuery(formData);
		urlConnection.setRequestProperty("Content-Length",
				Integer.toString(query.getBytes().length));
		if (headers != null) {
			for (String header : headers.keySet()) {
				urlConnection.setRequestProperty(header, headers.get(header));
			}
		}
		HttpResponse packedResponse = null;
		try {
			BufferedWriter out = new BufferedWriter(
					new OutputStreamWriter(urlConnection.getOutputStream()));
			out.write(query);
			out.flush();
			if (urlConnection.getResponseCode() < 400 || urlConnection.getResponseCode() >= 500) {
				urlConnection.getContent();
			}
			packedResponse = HttpResponse.fromConnection(urlConnection,
					cookieManager.getCookieStore().getCookies());
		}
		finally {
			urlConnection.disconnect();
		}
		return packedResponse;
	}
	
	private String buildQuery(List<NameValuePair> params)
			throws UnsupportedEncodingException
	{
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (NameValuePair pair : params)
		{
			if (first)
				first = false;
			else
				result.append("&");
			
			result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
		}
		return result.toString();
	}
}
