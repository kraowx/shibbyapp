package io.github.kraowx.shibbyapp.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;

public class HttpResponse {
	private int status;
	private String body;
	private String responseUrl;
	private Map<String, List<String>> headers;
	private List<HttpCookie> cookies;
	
	public static HttpResponse fromConnection(HttpURLConnection connection, List<HttpCookie> cookies) {
		try {
			HttpResponse resp = new HttpResponse();
			resp.status = connection.getResponseCode();
			if (resp.status < 400 || resp.status >= 500)
			{
				resp.body = parseBody(connection.getInputStream());
			}
			else
			{
				resp.body = parseBody(connection.getErrorStream());
			}
			resp.responseUrl = connection.getURL().toString();
			resp.headers = connection.getHeaderFields();
			resp.cookies = cookies;
			return resp;
		} catch (IOException e) {
			return null;
		}
	}
	
	public int getStatus() {
		return status;
	}
	
	public String getBody() {
		return body;
	}
	
	public String getResponseUrl()
	{
		return responseUrl;
	}
	
	public Map<String, List<String>> getHeaders() {
		return headers;
	}
	
	public List<HttpCookie> getCookies() {
		return cookies;
	}
	
	private static String parseBody(InputStream stream) {
		try {
			StringBuilder sb = new StringBuilder();
			String line;
			BufferedReader in = new BufferedReader(new InputStreamReader(stream));
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
			return sb.toString();
		} catch (IOException e) {
			return null;
		}
	}
}
