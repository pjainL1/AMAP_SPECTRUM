package com.korem.amap.tests.app;

import com.korem.amap.tests.BaseTest;
import com.lo.analysis.Analysis;
import com.spinn3r.log5j.Logger;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.testng.Assert;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * Test that verify that Postal Code Layer is dynamically create on TA creation.
 * It create a simple distance TA, get the layers and verify that "Postal Code"
 * layer is present in the returned list.
 * 
 * @author Charles St-Hilaire for Korem inc.
 */
public class TestPostalCodeLayer extends BaseTest {
    private static final Logger LOGGER = Logger.getLogger();
    //URL-PATTERN
    private static final String APPLY_URL_PATTERN = "/apply.safe";
    private static final String GET_LAYER_URL = "/getLayers.safe";
    
    @Test(priority = 0)
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void testPostalCodeLayer() throws UnsupportedEncodingException{
        getOpenLayers();
        {
            String mapUrl = String.format(APPLY_URL_PATTERN
                    + "?methods=tradearea"
                    + "&mapInstanceKey=%s"
                    + "&locations="+config.testSingleSelectionKeys()
                    + "&locationsCode="+config.testSingleSelectionKeys()
                    + "&tradearea=distance"
                    + "&distance=60"
                    + "&from=" + config.testDatesFrom()
                    + "&to=" + config.testDatesTo(),
                    mapInstanceKey);
             HttpPost post = post(mapUrl);
             String output = exec(post);
             assertTrue(validateSuccess(output), "Postal Code Apply Distance TA not successful");

             waitForProgressDone("tradearea");
        }
        
        {
             String mapUrl = String.format(GET_LAYER_URL+"?mapInstanceKey=%s",mapInstanceKey);
             HttpPost post = post(mapUrl);
             String output = exec(post);
             
             JSONArray ja = JSONArray.fromObject(output);
             Iterator layers = ja.iterator();
             boolean missing = true;
             while (layers.hasNext()) {
                 JSONObject layer = (JSONObject) layers.next();
                 if (layer != null && Analysis.POSTAL_CODE.toString().equals(layer.get("name"))){
                     missing = false;
                     break;
                 }
             }
             assertTrue(!missing, Analysis.POSTAL_CODE.toString()+" information is missing");
        }
          
    }
}
