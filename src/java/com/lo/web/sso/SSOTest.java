package com.lo.web.sso;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;

public class SSOTest {

    public static void main(String[] args) {
        URL service = null;
        String testRequest = "";
        try {
            SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            if (args.length > 1) {
//				System.setProperty("javax.net.ssl.keyStore", args[0]);
                if (args[1].equals("preauth")) {

                    // /home/dbarkwell/java_certs/partnerportal.jks preauth D47ED33F7EFF434F87C85110EE9F6778 +-4h&fdh&jf78 dbarkwell loycorp

//					http://tor-cwc-0808:8080/analytics/loading/secure/login.do?uid=isuser&domain=loycorp&langPref=en-us&token=cpooU92Hmz5c241sL2q5&consumerKey=36B10D14-27C5-4437-A0B0-9895529217DD

//					service = new URL("https://devpartnerportal.loyalty.com/_vti_bin/LoyaltyOne/Authorization.svc/PreAuthorize");
//					service = new URL("https://partnerportal.loyalty.com/_vti_bin/LoyaltyOne/Authorization.svc/PreAuthorize");
                    service = new URL("https://uatpartnerportal.loyalty.com/_vti_bin/LoyaltyOne/Authorization.svc/PreAuthorize");

                    // (String consumerKey, String uid, String domain, String consumerSecret)
                    testRequest = PreAuth(args[2], args[4], args[5], args[3]);

//					String str = args[3].getBytes(Charsets.US_ASCII).toString();
//					testRequest = PreAuth(args[2], args[4], args[5],str);
                } else if (args[1].equals("validate")) {
//					service = new URL("https://devpartnerportal.loyalty.com/_vti_bin/LoyaltyOne/Authorization.svc/ValidateToken");
//					service = new URL("https://partnerportal.loyalty.com/_vti_bin/LoyaltyOne/Authorization.svc/ValidateToken");

                    service = new URL("https://uatpartnerportal.loyalty.com/_vti_bin/LoyaltyOne/Authorization.svc/ValidateToken");

                    testRequest = AuthToken(args[2], args[3], args[4], args[5]);
                }

//				HttpsURLConnection conn = (HttpsURLConnection) service.openConnection();
                HttpsURLConnection conn = (HttpsURLConnection) service.openConnection();
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
                InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
                BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

                String string = null;
                while ((string = bufferedreader.readLine()) != null) {
                    System.out.println("Received " + string);
                }

                bufferedreader.close();
                inputstreamreader.close();
                inputstream.close();

            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

//	private static String AuthToken(String consumerKey, String token, String uid, String domain) {
//		StringBuffer buff = new StringBuffer();
//		buff.append("{");
//		buff.append("\"ConsumerKey\":\"" + consumerKey + "\",");
//		buff.append("\"Token\":\"" + token + "\"");
//		buff.append("}");
//		System.out.println("Request: " + buff.toString());
//		return buff.toString();
//	}
//	
    private static String AuthToken(String consumerKey, String token, String uid, String domain) {
        StringBuffer buff = new StringBuffer();
        buff.append("{");
        buff.append("\"ConsumerKey\":\"" + consumerKey + "\",");
        buff.append("\"Token\":\"" + token + "\",");
        buff.append("\"Uid\":\"" + uid + "\",");
        buff.append("\"Domain\":\"" + domain + "\"");
        buff.append("}");
        System.out.println("Request: " + buff.toString());
        return buff.toString();
    }

    private static String PreAuth(String consumerKey, String uid, String domain, String consumerSecret) throws
            NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        StringBuffer buff = new StringBuffer();
        buff.append("{");
        buff.append("\"UId\":\"" + uid + "\",");
        buff.append("\"Domain\":\"" + domain + "\",");
        buff.append("\"ConsumerKey\":\"" + consumerKey + "\",");
        String nonce = GenerateNonce();
        String timeStamp = GenerateTimeStamp();
//		String nonce = "5783211";
//		String timeStamp = "1355853802";
        buff.append("\"Signature\":\"" + ComputeSignature(consumerKey, nonce, timeStamp, uid, domain, "1.0", consumerSecret) + "\",");
//		buff.append("\"Signature\":\"0nrvmeapg6HM0wSUHH81Bev9Ot0=\",");
        buff.append("\"TimeStamp\":\"" + timeStamp + "\",");
        buff.append("\"Nonce\":\"" + nonce + "\",");
        buff.append("\"Version\":\"1.0\"");
        buff.append("}");
        System.out.println("Request: " + buff.toString());

        return buff.toString();

    }

    private static String GenerateTimeStamp() {
        return Long.toString(System.currentTimeMillis() / 1000L);
    }

    private static String GenerateNonce() {
        return Integer.toString(123400 + (int) (Math.random() * ((9999999 - 123400) + 1)));
    }

    private static String ComputeSignature(String consumerKey,
            String nonce, String timeStamp, String uid, String domain, String version,
            String consumerSecret) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        StringBuilder data = new StringBuilder();
        data.append(consumerKey + "&");
        data.append(nonce + "&");
        data.append(timeStamp + "&");
        data.append(uid + "&");
        data.append(domain + "&");
        data.append(version);

        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(consumerSecret.getBytes(Charsets.US_ASCII), "HmacSHA1"));
        byte[] rawHmac = mac.doFinal(data.toString().getBytes(Charsets.US_ASCII));

        return Base64.encodeBase64String(rawHmac);

    }
}
