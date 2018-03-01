/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.lo.console;



import java.io.ByteArrayInputStream;
import java.io.InputStream;
import com.lo.util.WSClient;
import java.util.*;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;


/**
 *
 * @author slajoie
 */
public class ConsoleControler {

     public static List<String> getWorkSpaceList() throws Exception {     
        String projectListXML = WSClient.getProjectService().getProjectList(null, null);
        SAXReader xmlReader = new SAXReader();
        ByteArrayInputStream bais = new ByteArrayInputStream(projectListXML.getBytes("UTF-8"));
        org.dom4j.Document doc = xmlReader.read((InputStream) bais);
        List<Node> projectNodes = doc.selectNodes("Projects/Project"); 
        List<String> projects = new ArrayList<String>();
        for (Iterator<Node> it = projectNodes.iterator(); it.hasNext();) {
            Node projetNode = it.next();
            projects.add(projetNode.selectSingleNode("Name").getText());
         }
        return projects;
    }

}
