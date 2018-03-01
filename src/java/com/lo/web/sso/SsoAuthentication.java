package com.lo.web.sso;

/**
 * this class is the main class of the AMAP authentication
 * 
 * @author mmocka
 * 
 */
public class SsoAuthentication {

	private WEBServicesValidateToken webService;

	public SsoAuthentication(String consumerKey, String token, String uid,
			String domain) {
		super();
		this.webService = new WEBServicesValidateToken();
		try {
			this.webService.validateToken(consumerKey, token, uid, domain);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public WEBServicesValidateToken getWebService() {
		return webService;
	}

	public void setWebService(WEBServicesValidateToken webService) {
		this.webService = webService;
	}
}
