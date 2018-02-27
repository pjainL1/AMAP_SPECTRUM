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
public class WEBServicesValidateToken {

	private boolean isValid;
	private String message;
	private String code;

	private static final Logger log = Logger.getLogger();

	// private static final String ERROR_UNKNOWN = "4000";
	private static final String ERROR_UNKNOWN_ERROR = "5000";

	private String url = Config.getInstance().getValue("ws.validatetoken");

	public WEBServicesValidateToken() {
		this.isValid = false;
		this.message = "";
		this.code = "5000";
	}

	public void validateToken(String consumerKey, String token, String uid,
			String domain) throws Exception {
		URL service = null;
		String testRequest = "";
		try {
			SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory
					.getDefault();
			// service = new URL(
			// "https://devpartnerportal.loyalty.com/_vti_bin/LoyaltyOne/Authorization.svc/ValidateToken");
			service = new URL(url);
			testRequest = AuthToken(consumerKey, token, uid, domain);

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

			try {
				String string = null;
				String allReturn = "";
				while ((string = bufferedreader.readLine()) != null) {
					// System.out.println("Received " + string);
					// log.debug(string);
					allReturn += string;
				}

				bufferedreader.close();
				inputstreamreader.close();

				getJSON(allReturn);

			} catch (Exception ex) {
				// errorException(ex.getMessage());
				invalidReturn();
				System.out.println(ex.getMessage());
				log.debug(ex.getMessage());
			} finally {
				bufferedreader.close();
				inputstreamreader.close();
			}

		} catch (Exception ex) {
			// errorException(ex.getMessage());
			invalidReturn();
			System.out.println(ex.getMessage());
			log.debug(ex.getMessage());
		}
	}

	/**
	 * function to return the json object
	 * 
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
		setCode(ERROR_UNKNOWN_ERROR);
	}

	// private void unknownError() {
	// setValid(false);
	// setCode(ERROR_UNKNOWN_ERROR);
	// }

	// private void errorException(String e) {
	// setValid(false);
	// setCode(ERROR_UNKNOWN_ERROR);
	// setMessage(e);
	// }

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

	private static String AuthToken(String consumerKey, String token,
			String uid, String domain) {
		StringBuffer buff = new StringBuffer();
		buff.append("{");
		buff.append("\"ConsumerKey\":\"" + consumerKey + "\",");
		buff.append("\"Domain\":\"" + domain + "\",");
		buff.append("\"Token\":\"" + token + "\",");
		buff.append("\"UId\":\"" + uid + "\"");
		buff.append("}");
		System.out.println("Request: " + buff.toString());
		return buff.toString();
	}

}
