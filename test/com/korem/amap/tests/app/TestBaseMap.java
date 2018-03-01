package com.korem.amap.tests.app;

import com.korem.amap.tests.BaseTest;
import com.spinn3r.log5j.Logger;
import java.io.UnsupportedEncodingException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 *
 * @author jphoude
 */
@Test(singleThreaded = true)
public class TestBaseMap extends BaseTest {
    private static final Logger LOGGER = Logger.getLogger();
    
    @Test
    void testMapInstanceKey() {
        assertTrue(getMapInstanceKey() != null && getMapInstanceKey().length() > 0);
    }
    
    @Test(dependsOnMethods = "testMapInstanceKey")
    void testTradeArea() throws UnsupportedEncodingException {
        setSelection(config.testSingleSelectionGeom());
        String apply = apply("setDates,tradearea", 
                config.testSingleSelectionKeys(), config.testSingleSelectionCodes(),
                config.testDatesFormattedFrom(), config.testDatesFormattedTo(), 
                "tradearea", "issuance,",
                "issuance", "0.60",
                "from", config.testDatesFrom(),
                "to", config.testDatesTo());
        assertTrue(validateSuccess(apply));

        waitForProgressDone();
        JSONArray layerControl = getLayerControl();
        boolean found = false;
        for (int i = 0; i < layerControl.size(); i++) {
            JSONObject taLayer = layerControl.getJSONObject(i);
            found = found || taLayer.getString("name").equals("Trade Area");
        }
        assertTrue(found, "Trade Area layer not found.");
    }
}
