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

import com.mapinfo.midev.service.namedresource.ws.v1.ServiceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SpectrumUtilities {
	
	public static Element getXmlDocElement(String content) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
		return doc.getDocumentElement();
	}

	public static String SAMPLE_RESOURCE = "<NamedDataSourceDefinition version=\"MXP_NamedResource_1_5\" xmlns=\"http://www.mapinfo.com/mxp\">"
			+ "<ConnectionSet/>"
			+ "<DataSourceDefinitionSet>"
			+ "<TABFileDataSourceDefinition id=\"id3\" readOnly=\"false\">"
			+ "<DataSourceName>world</DataSourceName>"
			+ "<FileName>@TEMP_DATA@</FileName>"
			+ "</TABFileDataSourceDefinition>"
			+ "</DataSourceDefinitionSet>"
			+ "<DataSourceRef ref=\"id3\"/>"
			+ "</NamedDataSourceDefinition>";
	
    public static void printError(ServiceException se){
        System.err.println("Service Exception:");
        System.err.println("Error Code : " + se.getFaultInfo().getErrorCode());
        System.err.println("Error Type : " + se.getFaultInfo().getErrorType());
        System.err.println("Error Message : " + se.getMessage());
    }
}
