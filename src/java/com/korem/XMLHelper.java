package com.korem;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author jduchesne
 */
public class XMLHelper {
    private static final XMLHelper instance = new XMLHelper();
    public static XMLHelper get() {
        return instance;
    }

    private XMLHelper() {}

    public int getInt(Object el, String xPathQuery) throws XPathExpressionException {
        return Integer.parseInt(getString(el, xPathQuery));
    }

    public double getDouble(Object el, String xPathQuery) throws XPathExpressionException {
        return Double.parseDouble(getString(el, xPathQuery));
    }

    public String getString(Object el, String xPathQuery) throws XPathExpressionException {
        return (String) get(el, xPathQuery, XPathConstants.STRING);
    }

    public NodeList getList(Object el, String xPathQuery) throws XPathExpressionException {
        return (NodeList) get(el, xPathQuery, XPathConstants.NODESET);
    }

    public Object get(Object el, String xPathQuery, QName qName) throws XPathExpressionException {
        return newXPath(xPathQuery).evaluate(el, qName);
    }

    private DocumentBuilderFactory createFactory() {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        return builderFactory;
    }

    public Document newDocument(String xml) throws ParserConfigurationException, SAXException, IOException {
        return createFactory().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes()));
    }

    public Document newDocument(File file) throws ParserConfigurationException, SAXException, IOException {
        return createFactory().newDocumentBuilder().parse(file);
    }

    public XPathExpression newXPath(String query) throws XPathExpressionException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        return xPath.compile(query);
    }
}
