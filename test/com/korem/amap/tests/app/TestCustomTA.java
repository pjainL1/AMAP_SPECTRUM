package com.korem.amap.tests.app;

import com.korem.amap.tests.BaseTest;
import com.spinn3r.log5j.Logger;
import java.io.UnsupportedEncodingException;
import org.apache.http.client.methods.HttpPost;
import org.testng.Assert;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * This class is to test Servlet of Custom TA functional behavior. Confluence:
 * 3.2_Custom Trade Area (Priority 1) Jira: LOTY-78
 *
 * @author Charles St-Hilaire for Korem inc.
 */
@Test(singleThreaded = true)
public class TestCustomTA extends BaseTest {

    //Static LOGGER
    private static final Logger LOGGER = Logger.getLogger();

    //URL-PATTERN
    private static final String APPLY_URL = "/apply.safe";
    private static final String INFO_TOOL_URL = "/getInfo.safe";

    /**
     * Attempt to create a Custom TA for specific locations.
     *
     * @throws UnsupportedEncodingException
     */
    @Test
    public void testCreateCustomTAWithLocations() throws UnsupportedEncodingException {
        getOpenLayers();
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

    /**
     * Attempt to call InfoTool on a Custom TA for specific locations.
     *
     * @throws UnsupportedEncodingException
     */
    @Test(dependsOnMethods = "testCreateCustomTAWithLocations")
    void testInfoToolOnCustomTAWithLocations() throws UnsupportedEncodingException {
        getOpenLayers();
        HttpPost post = post(INFO_TOOL_URL);

        addPostKeyValuePairs(post,
                "mapInstanceKey", mapInstanceKey,
                "x", config.testTradeAreaCustomInfoX(),
                "y", config.testTradeAreaCustomInfoY(),
                "from", config.testDatesFrom(),
                "to", config.testDatesTo());
        String output = exec(post);
        LOGGER.info("testInfoToolOnCustomTAWithLocations() " + output + " < of > " + INFO_TOOL_URL);
        //Loop over LOCATIONS_CODE and verify that each code are in output (HTTP response)
        boolean ok = true;
        String[] codes = config.testSingleSelectionCodes().split("[,]");
        for (String code : codes) {
            if (output.indexOf("\"Location code\":\"" + code + "\"") == -1) {
                ok = false;
                break;
            }
        }
        assertTrue(codes != null, "Location codes NULL");
        assertTrue(codes.length > 0, "Location codes length is 0.");
        assertTrue(ok, "Location codes not OK.");
    }

    /**
     * Attempt to create a Custom TA without any specific locations.
     */
    @Test(dependsOnMethods = "testInfoToolOnCustomTAWithLocations")
    void testCreateCustomTAWithoutLocations() {
        try {
            getOpenLayers();
            HttpPost post = post(APPLY_URL);
            addPostKeyValuePairs(post, 
                "methods", "tradearea,",
                "mapInstanceKey", mapInstanceKey,
                "locations", "",
                "locationsCode", "",
                "tradearea", "custom,",
                "polygon", config.testTradeAreaCustomGeom(),
                "from", config.testDatesFrom(),
                "to", config.testDatesTo());
            String output = exec(post);
            LOGGER.info("testCreateCustomTAWithoutLocations() " + output + " < of > " + APPLY_URL);
            assertTrue(validateSuccess(output), "Apply TA not successful");
            waitForProgressDone("tradearea");
        } catch (UnsupportedEncodingException uee) {
            Assert.fail(uee.getMessage(), uee);
            LOGGER.error(uee.getMessage(), uee);
        }
    }

    /**
     * Attempt to call InfoTool on a Custom TA without any specific locations.
     */
    @Test(dependsOnMethods = "testCreateCustomTAWithoutLocations")
    void testInfoToolOnCustomTAWithoutLocations() {
        getOpenLayers();
        String mapUrl = String.format(INFO_TOOL_URL
                + "?mapInstanceKey=%s"
                + "&x=" + config.testTradeAreaCustomInfoX()
                + "&y=" + config.testTradeAreaCustomInfoY()
                + "&from=" + config.testDatesFrom()
                + "&to=" + config.testDatesTo(),
                mapInstanceKey);
        HttpPost post = post(mapUrl);
        String output = exec(post);
        LOGGER.info("testInfoToolOnCustomTAWithoutLocations() " + output + " < of > " + mapUrl);
        assertTrue(output != null && output.indexOf("\"Location code\":\"custom\"") != -1);
    }

    /**
     * Attempt to clear a previously set Custom TA
     */
    @Test(dependsOnMethods = "testInfoToolOnCustomTAWithoutLocations")
    void testClearCustomTA() {
        getOpenLayers();
        String mapUrl = String.format(APPLY_URL
                + "?methods=tradearea"
                + "&mapInstanceKey=%s"
                + "&locations="
                + "&locationsCode="
                + "&tradearea="
                + "&polygon="
                + "&from=" + config.testDatesFrom()
                + "&to=" + config.testDatesTo(),
                mapInstanceKey);
        HttpPost post = post(mapUrl);
        String output = exec(post);
        LOGGER.info("testClearCustomTA() " + output + " < of > " + mapUrl);
        assertTrue(validateSuccess(output));
    }

    @Override
    protected void getOpenLayers() {
        doGetOpenLayers(config.testTradeAreaCustomBbox(), config.testTradeAreaCustomWidth(), config.testTradeAreaCustomHeight());
    }
}
