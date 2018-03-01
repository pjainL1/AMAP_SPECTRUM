package com.korem.amap.tests.app;

import com.korem.amap.tests.BaseTest;
import com.spinn3r.log5j.Logger;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.testng.Assert;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * This class is to test PC Extraction
 *
 * @author Charles St-Hilaire for Korem inc.
 */
@Test(singleThreaded = true)
public class TestPCExtraction extends BaseTest {

    //Static LOGGER
    private static final Logger LOGGER = Logger.getLogger();

    //URL-PATTERN
    private static final String APPLY_URL = "/apply.safe";
    private static final String DOWNLOAD_PC_URL = "/getPCInfo.safe";

    //Value to be sent as methods parameter on CSV generation call
    private static final String METHODS_GENERATE_PC = "generatePC";

    /**
     * This method test the PC Extraction
     */
    @Test(priority = 0)
    void testPCExtractionWithLocations() throws UnsupportedEncodingException {
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
            LOGGER.info("testInfowindowCollectorInfo() " + output + " < of > " + APPLY_URL);
            assertTrue(validateSuccess(output), "Apply TA not successful");

            waitForProgressDone("tradearea");
        }

        {
            //Launch PC extraction
            HttpPost post = post(APPLY_URL);
            addPostKeyValuePairs(post, 
                    "methods", METHODS_GENERATE_PC,
                    "mapInstanceKey", mapInstanceKey,
                    "locations", config.testSingleSelectionKeys());
            String output = exec(post);
            assertTrue(validateSuccess(output));

            //Wait for CSV generation terminate
            waitForProgressDone("generatePC");
        }
        
        {
            //Get the CSV and check Content-Type and Content-Length
            HttpPost post = post(DOWNLOAD_PC_URL);
            HttpResponse hr = plainExec(post);
            HttpEntity he = hr != null ? hr.getEntity() : null;
            if (he != null) {
                LOGGER.info(he.getContentType() + ", Content-Length: " + he.getContentLength());
                assertTrue("text/csv".equals(he.getContentType().getValue()) && he.getContentLength() > 1L);
            } else {
                Assert.fail("HttpEntity is null");
            }
        }
    }

    @Override
    protected void getOpenLayers() {
        doGetOpenLayers(config.testTradeAreaCustomBbox(), config.testTradeAreaCustomWidth(), config.testTradeAreaCustomHeight());
    }
}
