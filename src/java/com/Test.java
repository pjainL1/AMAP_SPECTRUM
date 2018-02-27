package com;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssS");
		String timeStamp = dateFormat.format(new Date());
		System.out.println(timeStamp);
		
		String url ="http://test/loading/sdsds";
		
		
		String url2 = "https://devamap.loyalty.com/analytics/mlcc/secure/expired.do";
		String redirectUrl = getFixedUrl(url2,"/secure/expired.do");
		System.out.println(redirectUrl);
		
		String languagePref = "EN";
		if (languagePref.toUpperCase().matches(".*(EN|FR).*") ){
			System.out.println("ok");
		}else{
			System.out.println("nok");
		}
	}

	private static String getFixedUrl(String url, String suffix) {
		if (url.contains("analytics")) {
			String tab[] = url.split("/analytics/");
			String fixUrl = tab[0] + "/analytics/"
					+ tab[1].split("/")[0] + suffix;
			return fixUrl.replace("http://", "https://");
		}
		return url.replace("http://", "https://");
	}
}
