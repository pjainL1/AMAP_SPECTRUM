REF:
	-HTTPS certificate: 
	run keytool.exe under <JDK_HOME>\bin
	example: keytool -genkey -alias shell -keyalg RSA -keystore D:\appl\apache-tomcat-6.0.29\conf\tomcat.keystore


1. Copier le keystore dans r�pertoire de votre choix

2. Editer la section SSL HTTP/1.1 Connector dans le fichier server.xml du Tomcat

TOMCAT INSTALL� AVEC DLL/SO natif:

    <Connector port="8443" protocol="org.apache.coyote.http11.Http11Protocol" SSLEnabled="true"
               maxThreads="150" scheme="https" secure="true"
               clientAuth="false" sslProtocol="TLS"
               keystoreFile="D:\appl\apache-tomcat-6.0.29\conf\tomcat.keystore" keystorePass="loyaltyone" keyAlias="loyaltyone" />


TOMCAT INSTALL� SANS DLL/SO natif:

    <Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true"
               maxThreads="150" scheme="https" secure="true"
               clientAuth="false" sslProtocol="TLS"
               keystoreFile="D:\appl\apache-tomcat-6.0.29\conf\tomcat.keystore" keystorePass="loyaltyone" keyAlias="loyaltyone" />