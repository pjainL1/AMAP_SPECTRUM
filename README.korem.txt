Access URLs with Korem LDAP setup:

Make sure the following settings are correctly set in config.properties:
 debug=true
 # warning: no access token validation will be done if koremInternalAccess is enabled.
 koremInternalAccess.enabled=true
 https.enabled=false

Admin: 
- http://localhost:8080/analytics/amap/secure/login.do?uid=loyalty_admin
Analyst:
- http://localhost:8080/analytics/amap/secure/init.do?uid=loyalty_analyst&role=Analyst&sponsorcode=met1&time=234234