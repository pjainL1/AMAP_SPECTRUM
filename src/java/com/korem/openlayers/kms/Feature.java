package com.korem.openlayers.kms;

import com.korem.XMLHelper;
import javax.xml.xpath.XPathExpressionException;
import net.sf.json.util.JSONBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author jduchesne
 */
public class Feature {

    private NodeList attributes;

    Feature(Element element) throws XPathExpressionException {
        attributes = XMLHelper.get().getList(element, ".//AttValue/text()");
    }

    public void appendJSON(JSONBuilder builder) {
        builder.array();
        for (int i = 0; i < attributes.getLength(); ++i) {
            builder.value(attributes.item(i).getTextContent());
        }
        builder.endArray();
    }

    public String getPK(){
        return attributes.item(0).getTextContent();
    }
}
