package com.korem.heatmaps;

import com.korem.XMLHelper;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author jduchesne
 */
public class RulesLoader {

    private File path;

    public RulesLoader(String path) {
        this.path = new File(path);
    }

    public HeatMapRules load() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        XMLHelper xml = XMLHelper.get();
        HeatMapRules rules = new HeatMapRules();
        
        Document doc = xml.newDocument(path);
        NodeList nodes = xml.getList(doc, "/rules/rule");

        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);
            rules.addRule(xml.getInt(node, "@zoomLevel"),
                    xml.getDouble(node, "@pointRadiusInKM"),
                    xml.getDouble(node, "@groupByCountWeight"),
                    xml.getInt(node, "@groupByRound"));
        }

        return rules;
    }
}
