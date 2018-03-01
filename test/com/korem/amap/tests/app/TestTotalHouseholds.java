package com.korem.amap.tests.app;

import com.korem.amap.tests.BaseTest;
import com.spinn3r.log5j.Logger;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.http.client.methods.HttpPost;
import org.testng.Assert;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * This class is to check is Total Households.
 *
 * @author Charles St-Hilaire for Korem inc.
 */
@Test(singleThreaded = true)
public class TestTotalHouseholds extends BaseTest {

    //Static LOGGER
    private static final Logger LOGGER = Logger.getLogger();

    //URL-PATTERN
    private static final String APPLY_URL = "/apply.safe";
    private static final String INFO_TOOL_URL = "/getInfo.safe";

    /**
     * Check if total households is in the returned InfoTool.
     *
     * @throws IOException
     */
    @Test(priority = 0)
    void testTotalHouseholds() throws UnsupportedEncodingException {
        getOpenLayers();
        {
            LOGGER.info("Selection is " + setSelection(config.testSingleSelectionGeom()));
            HttpPost post = post(APPLY_URL);
            addPostKeyValuePairs(post, 
                    "methods", "tradearea,",
                    "mapInstanceKey", mapInstanceKey,
                    "locations", config.testSingleSelectionKeys(),
                    "locationsCode", config.testSingleSelectionCodes(),
                    "tradearea", "custom,",
                    "polygon", config.testTradeAreaCustomGeom(),
                    "from", config.testDatesFrom(),
                    "to", config.testDatesTo());
            String output = exec(post);
            LOGGER.info("testCreateCustomTAWithLocations() " + output + " < of > " + APPLY_URL);
            assertTrue(validateSuccess(output), "Apply TA not successful");

            waitForProgressDone("tradearea");
        }

        String mapUrl = String.format(INFO_TOOL_URL
                + "?mapInstanceKey=%s"
                + "&x=" + config.testTradeAreaCustomInfoX()
                + "&y=" + config.testTradeAreaCustomInfoY()
                + "&from=" + config.testDatesFrom()
                + "&to=" + config.testDatesTo(),
                mapInstanceKey);
        HttpPost post = post(mapUrl);
        String output = exec(post);
        LOGGER.info("testTotalHouseholds() " + output + " < of > " + mapUrl);

        JSONObject jo = JSONObject.fromObject(output);
        JSONArray ja = (JSONArray) jo.get("Trade Area");
        Iterator iter = ja.iterator();
        while (iter.hasNext()) {
            JSONObject aSubJo = (JSONObject) iter.next();
            if (aSubJo == null || aSubJo.getString("Households (Can. Post)").isEmpty()) {
                Assert.fail("Total Households is not there.");
            }
        }
    }

    @Override
    protected void getOpenLayers() {
        doGetOpenLayers(config.testTradeAreaCustomBbox(), config.testTradeAreaCustomWidth(), config.testTradeAreaCustomHeight());
    }
}
