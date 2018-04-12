/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.web;

/**
 *
 * @author pjain
 */
import com.lo.config.Confs;
import com.mapinfo.midev.service.mapping.ws.v1.MappingService;
import com.mapinfo.midev.service.mapping.ws.v1.MappingServiceInterface;
import com.mapinfo.midev.service.namedresource.ws.v1.NamedResourceService;
import com.mapinfo.midev.service.namedresource.ws.v1.NamedResourceServiceInterface;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;

import java.net.URL;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPBinding;

import java.util.ResourceBundle;

public class Preference {
    
    //private static ResourceBundle rbSpectrum = ResourceBundle.getBundle("com.lo.web.spectrum");
    private static final String HOST = Confs.CONFIG.spectrumHost();
    private static final int PORT = Integer.parseInt(Confs.CONFIG.spectrumPort());
    private static final String NAMESPACE = "http://www.mapinfo.com/midev/service/namedresource/ws/v1";
    private static final String SERVICENAME = "NamedResourceService";
    private static final String DEFAULT_SERVICENAME = "http://" + HOST + ":" + PORT + "/soap/NamedResourceService/?wsdl";
    private static final String SPECTRUM_TOKEN_NS = "http://token.security.common.server.platform.spectrum.pb.com/";
    private static final String SPECTRUM_TOKEN_WSDL_URL = "http://" + HOST + ":" + PORT + "/security/TokenManagerService?wsdl";
    private static final String SPECTRUM_TOKEN_SERVICE_URL = "http://" + HOST + ":" + PORT + "/security/TokenManagerService";
    private static final String SPECTRUM_TOKEN_LOGOUT_WSDL_URL = "http://" + HOST + ":" + PORT + "/security/TokenLogoutService?wsdl";
    private static final String SPECTRUM_TOKEN_LOGOUT_SERVICE_URL = "http://" + HOST + ":" + PORT + "/security/TokenLogoutService";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";
    private static final String TOKEN_LIFE_MINUTES = "5";

    public static NamedResourceServiceInterface getServiceInterface() throws Exception {
        NamedResourceService service;
        NamedResourceServiceInterface serviceInterface;
        service = new NamedResourceService(new URL(DEFAULT_SERVICENAME), new QName(NAMESPACE, SERVICENAME));
        serviceInterface = service.getNamedResourceServiceInterface();

        //This shows how to use basic authentication secured service
        //Preference.setBasicAuthCredentials((BindingProvider) serviceInterface, USERNAME, PASSWORD);

        //This shows how to use spectrum token based authentication secured service
        Preference.setSpectrumTokenAuthCredentials((BindingProvider) serviceInterface, USERNAME, PASSWORD);

        return serviceInterface;
    }
    
    
    public static void logout(NamedResourceServiceInterface serviceInterface) throws Exception {
        BindingProvider serviceProvider = (BindingProvider) serviceInterface;
        Map headers = (Map) serviceProvider.getRequestContext().get(MessageContext.HTTP_REQUEST_HEADERS);
        if (headerContainsTokenAuth(headers)) {
            Dispatch<SOAPMessage> tokenLogoutServiceDispatch = createTokenLogoutServiceDispatch();
            tokenLogoutServiceDispatch.getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, headers);
            tokenLogoutServiceDispatch.invoke(createTokenLogoutRequest());
        }
    }
    
    

    private static boolean headerContainsTokenAuth(Map header) {
        if (header != null) {
            List cookies = (List) header.get("Cookie");
            if (cookies != null) {
                for (Object cookie : cookies) {
                    if (((String) cookie).startsWith("spectrum.authentication.token")) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static void setBasicAuthCredentials(BindingProvider serviceInterface, String userName, String password) {
        serviceInterface.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, userName);
        serviceInterface.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
    }

    static void setSpectrumTokenAuthCredentials(BindingProvider serviceInterface, String userName, String password) throws IOException, SOAPException {
        Dispatch<SOAPMessage> dispatch = createTokenServiceDispatch(userName, password);
        SOAPMessage request = createTokenRequest();
        SOAPMessage response = dispatch.invoke(request);

        //get session and token values from response
        String token = response.getSOAPBody().getElementsByTagName("token").item(0).getTextContent();

        //populate cookies for session and token
        serviceInterface.getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS,
                Collections.singletonMap("Cookie", Collections.singletonList("spectrum.authentication.token=" + token))
        );
    }

    private static Dispatch<SOAPMessage> createTokenServiceDispatch(String userName, String password) throws MalformedURLException {
        URL wsdlLocation = new URL(SPECTRUM_TOKEN_WSDL_URL);
        QName serviceName = new QName(SPECTRUM_TOKEN_NS, "TokenManagerServiceImplService");
        Service service = Service.create(wsdlLocation, serviceName);
        QName portName = new QName(SPECTRUM_TOKEN_NS, "getAccessExpiringToken");
        service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, SPECTRUM_TOKEN_SERVICE_URL);
        Dispatch<SOAPMessage> dispatch = service.createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE);
        dispatch.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, userName);
        dispatch.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
        return dispatch;
    }

    private static SOAPMessage createTokenRequest() throws SOAPException {
        MessageFactory mf = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPMessage request = mf.createMessage();
        request.getSOAPBody().addBodyElement(new QName(SPECTRUM_TOKEN_NS, "getAccessExpiringToken", "tok")).addChildElement("tokenLifeInMinutes").addTextNode(TOKEN_LIFE_MINUTES);
        return request;
    }

    private static Dispatch<SOAPMessage> createTokenLogoutServiceDispatch() throws MalformedURLException {
        URL wsdlLocation = new URL(SPECTRUM_TOKEN_LOGOUT_WSDL_URL);
        QName serviceName = new QName(SPECTRUM_TOKEN_NS, "TokenLogoutServiceImplService");
        Service service = Service.create(wsdlLocation, serviceName);
        QName portName = new QName(SPECTRUM_TOKEN_NS, "getAccessExpiringToken");
        service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, SPECTRUM_TOKEN_LOGOUT_SERVICE_URL);
        return service.createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE);
    }

    private static SOAPMessage createTokenLogoutRequest() throws SOAPException {
        MessageFactory mf = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPMessage request = mf.createMessage();
        request.getSOAPBody().addBodyElement(new QName(SPECTRUM_TOKEN_NS, "logout", "tok"));
        return request;
    }
}
