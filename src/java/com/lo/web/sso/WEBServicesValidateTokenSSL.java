package com.lo.web.sso;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import net.sf.json.JSONObject;

import com.spinn3r.log5j.Logger;

/**
 * This class access to the partnerportal webservice and manage the response of
 * the webservice
 * 
 * @author mmocka
 * 
 */
public class WEBServicesValidateTokenSSL {

	private boolean isValid;
	private String message;
	private String code;

	private static final Logger log = Logger.getLogger();

	private static final String ERROR_UNKNOWN = "4000";

	private String url = Config.getInstance().getValue("ws.validatetoken");

	public WEBServicesValidateTokenSSL() {
		this.isValid = false;
		this.message = "";
		this.code = "200";
	}

	public void validateToken(String consumerKey, String token)
			throws Exception {
		URL service = null;
		String testRequest = "";
		try {
			SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory
					.getDefault();
			// create the web service URL
			service = new URL(url);
			testRequest = AuthToken(consumerKey, token);

			HttpsURLConnection conn = (HttpsURLConnection) service
					.openConnection();
			conn.setRequestMethod("POST");
			conn.setSSLSocketFactory(sf);
			conn.setFixedLengthStreamingMode(testRequest.getBytes("UTF8").length);
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-type", "application/json");
			OutputStream out = conn.getOutputStream();
			Writer writer = new OutputStreamWriter(out, "UTF-8");
			writer.write(testRequest);
			writer.close();

			InputStream inputstream = conn.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(
					inputstream);
			BufferedReader bufferedreader = new BufferedReader(
					inputstreamreader);

			String string = null;
			String allReturn = "";
			while ((string = bufferedreader.readLine()) != null) {
				System.out.println("Received " + string);
				log.debug(string);
				allReturn += string;
			}
			getJSON(allReturn);

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			log.debug(ex.getMessage());
		}
	}

	/**
	 * function to return the json object
	 * @param json
	 */
	private void getJSON(String json) {
		try {
			JSONObject jsonObject = JSONObject.fromObject(json);

			if (jsonObject.get("IsValid") != null
					&& jsonObject.get("Message") != null
					&& jsonObject.get("Code") != null) {
				setValid(jsonObject.getBoolean("IsValid"));
				setMessage(jsonObject.getString("Message"));
				setCode(jsonObject.getString("Code"));
			} else {
				invalidReturn();
			}

		} catch (Exception e) {
			invalidReturn();
			log.debug(e.getMessage());
		}
	}

	private void invalidReturn() {
		setValid(false);
		setCode(ERROR_UNKNOWN);
	}

	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * function to create the json message for the web service
	 * @param consumerKey
	 * @param token
	 * @return
	 */
	private static String AuthToken(String consumerKey, String token) {
		StringBuffer buff = new StringBuffer();
		buff.append("{");
		buff.append("\"ConsumerKey\":\"" + consumerKey + "\",");
		buff.append("\"Token\":\"" + token + "\"");
		buff.append("}");
		System.out.println("Request: " + buff.toString());
		return buff.toString();
	}

}
