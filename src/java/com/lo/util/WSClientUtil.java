/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.lo.util;

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;

/**
 *
 * @author slajoie
 */
public class WSClientUtil {

     public static double[] getBounds(String mapInstanceKey) throws Exception, ParserConfigurationException, ParserConfigurationException, ParserConfigurationException, ParserConfigurationException, ParserConfigurationException {
        return getBoundsFromXML(WSClient.getMapService().getBounds(mapInstanceKey));
    }

     public static double[] getBounds(String mapInstanceKey, String srs) throws Exception, ParserConfigurationException, ParserConfigurationException, ParserConfigurationException, ParserConfigurationException, ParserConfigurationException {
        return getBoundsFromXML(WSClient.getMapService().getBounds(mapInstanceKey, srs));
    }

     public static double[] getBoundsFromXML(String boundsXML) throws Exception, ParserConfigurationException, ParserConfigurationException, ParserConfigurationException, ParserConfigurationException, ParserConfigurationException {
        Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + boundsXML)));
        return new double[]{
                    Double.parseDouble(dom.getElementsByTagName("X").item(0).getFirstChild().getNodeValue()),
                    Double.parseDouble(dom.getElementsByTagName("Y").item(0).getFirstChild().getNodeValue()),
                    Double.parseDouble(dom.getElementsByTagName("X").item(1).getFirstChild().getNodeValue()),
                    Double.parseDouble(dom.getElementsByTagName("Y").item(1).getFirstChild().getNodeValue())};
     }
}
