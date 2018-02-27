package com.korem.amap.tests.console;

import com.korem.amap.tests.BaseTest;
import com.spinn3r.log5j.Logger;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;
import org.apache.http.client.methods.HttpPost;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * This class test the Servlet relate to LayerGroup functionality.
 * It perform 5 tests that depends of each other.
 * 
 * @author Charles St-hilaire for Korem inc.
 */
@Test(singleThreaded = true)
public class TestLayerGroup extends ConsoleBaseTest {
    //Static LOGGER
    private static final Logger LOGGER = Logger.getLogger();
    
    //URL-PATTERN
    private static final String ADD_URL_PATTERN = "console/AddLayerGroup.safe";
    private static final String GET_URL_PATTERN = "console/GetLayerGroup.safe";
    private static final String RENAME_URL_PATTERN = "console/RenameLayerGroup.safe";
    private static final String MOVE_URL_PATTERN = "console/MoveLayerGroup.safe";
    private static final String DELETE_URL_PATTERN = "console/DeleteLayerGroup.safe";
    
    JSONObject layerControl = null;
    private int firstGroupId = -1, secondGroupId = -1;
    
    public TestLayerGroup() {
        super(null);
    }
    
    @Test (priority = 0)
    void testAddLayerGroup(){
        try{
            String mapUrl = String.format(ADD_URL_PATTERN+"?sponsor=%s"+"&groupName=%s", config.testSponsors(), config.consoleLayerGroupsTestGroupFrom());
            HttpPost post = post(mapUrl);
            String output = exec(post);
            assertTrue(validateSuccess(output));
            
            mapUrl = String.format(ADD_URL_PATTERN+"?sponsor=%s"+"&groupName=%s", config.testSponsors(), config.consoleLayerGroupsOtherTestGroup());
            post = post(mapUrl);
            output = exec(post);
            assertTrue(validateSuccess(output));
        }catch (Exception e){
            fail(String.format("Unexpected problem occured, %s",e.getMessage()));            
        }
    }
    
    @Test(priority = 1)
    void testGetLayerGroup(){
        String mapUrl = String.format(GET_URL_PATTERN+"?sponsor=%s", config.testSponsors());
        HttpPost post = post(mapUrl);
        String output = exec(post);
        layerControl = JSONObject.fromObject(output);
        if (layerControl != null){
            JSONArray restorants = (JSONArray)layerControl.get("children");
            Iterator resto = restorants.iterator();
            int groupFound = 0;
            while (resto.hasNext()) {
                JSONObject aResto = (JSONObject) resto.next();
                if (config.consoleLayerGroupsTestGroupFrom().equals(aResto.getString("text")) && !aResto.getBoolean("leaf")){
                    firstGroupId = aResto.getInt("groupId");
                    groupFound++;
                }else if (config.consoleLayerGroupsOtherTestGroup().equals(aResto.getString("text")) && !aResto.getBoolean("leaf")){
                    secondGroupId = aResto.getInt("groupId");
                    groupFound++;
                }
            }
            if (groupFound < 2){
                fail("Missing test groups");
            }
        } else {
            fail(String.format("Unable to get layers for sponsor key: %s", config.testSponsors()));
        }
    }
    
    @Test(priority = 2)
    void testMoveLayerGroup(){
        try{
            if (firstGroupId != -1 && secondGroupId != -1){
                if (layerControl != null){
                    JSONBuilder jb = new JSONStringer().array();
                    JSONArray restorants = (JSONArray)layerControl.get("children");
                    Iterator group = restorants.iterator();
                    JSONObject first = null, second = null;
                    boolean needMove = true;
                    int groupCount = 0, layerCount;
                    while (group.hasNext()) {
                        JSONObject aGroup = (JSONObject) group.next();
                        if (config.consoleLayerGroupsTestGroupFrom().equals(aGroup.getString("text")) && !aGroup.getBoolean("leaf")){
                            first = aGroup;
                        }else if (config.consoleLayerGroupsOtherTestGroup().equals(aGroup.getString("text")) && !aGroup.getBoolean("leaf")){
                            second = aGroup;
                        }
                        if (first != null && second != null && needMove){
                            appendIt(jb, second, groupCount++);
                            appendIt(jb, first, groupCount++);
                            needMove = false;
                        }else if (aGroup != first && aGroup != second){
                            appendIt(jb, aGroup, groupCount++);
                            layerCount = 0;
                            Iterator layer = aGroup.getJSONArray("children").iterator();
                            while (layer.hasNext()) {
                                appendIt(jb, (JSONObject)layer.next(), layerCount++);
                            }
                        }
                    }
                    jb.endArray();
                    String mapUrl = MOVE_URL_PATTERN+"?hierarchy="+URLEncoder.encode(jb.toString(), "UTF-8");
                    HttpPost post = post(mapUrl);
                    String output = exec(post);
                    assertTrue(validateSuccess(output));
                }else{
                    fail("Unable to move layers, hierarchy is unavailable");
                }
            }else{
                fail("Unable to move layers, id(s) is/are not available for test groups");
            }
        }catch(UnsupportedEncodingException uee){
            LOGGER.error(uee.getMessage(), uee);
            fail(String.format("Unexpected problem occured, %s", uee.getMessage()));
        }
    }
    
    private void appendIt(JSONBuilder jb, JSONObject jo, int idx){
        jb.object().key("isLayer").value(jo.getBoolean("leaf")).
                    key("groupId").value(jo.getInt("groupId")).
                    key("oldGroupId").value(jo.getInt("groupId")).
                    key("index").value(idx).
                    key("name").value(jo.getString("realText")).
                    key("sponsor").value(config.testSponsors()).
           endObject();
    }
    
    @Test(priority = 3)
    void testRenameLayerGroup() throws Exception{
        if (firstGroupId != -1){
            String mapUrl = String.format(RENAME_URL_PATTERN+"?groupId=%s&groupName=%s", String.valueOf(firstGroupId), config.consoleLayerGroupsTestGroupTo());
            HttpPost post = post(mapUrl);
            String output = exec(post);
            assertTrue(validateSuccess(output));
        }else{
            throw new Exception(String.format("Impossible to rename, id is not available for group: %s", config.consoleLayerGroupsTestGroupFrom()));
        }
    }
    
    @Test (priority = 4)
    void testDeleteLayerGroup() throws Exception {
        if (firstGroupId != -1){
            String mapUrl = String.format(DELETE_URL_PATTERN+"?groupId=%s", String.valueOf(firstGroupId));
            HttpPost post = post(mapUrl);
            String output = exec(post);
            assertTrue(validateSuccess(output));
        }else{
            throw new Exception(String.format("Impossible to delete, id is not available for group: %s", config.consoleLayerGroupsTestGroupFrom()));
        }
        if (secondGroupId != -1){
            String mapUrl = String.format(DELETE_URL_PATTERN+"?groupId=%s", String.valueOf(secondGroupId));
            HttpPost post = post(mapUrl);
            String output = exec(post);
            assertTrue(validateSuccess(output));
        }else{
            throw new Exception(String.format("Impossible to delete, id is not available for group: %s", config.consoleLayerGroupsOtherTestGroup()));
        }
    }   
}
