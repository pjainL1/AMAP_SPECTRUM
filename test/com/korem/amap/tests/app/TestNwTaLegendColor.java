/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.korem.amap.tests.app;

import com.spinn3r.log5j.Logger;
import java.io.UnsupportedEncodingException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.client.methods.HttpPost;
import org.testng.Assert;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author maitounejjar
 */
@Test(singleThreaded = true)
public class TestNwTaLegendColor extends com.korem.amap.tests.BaseTest {

    private static final Logger LOGGER = Logger.getLogger();
    private static final String GET_LEGEND_COLOR = "/getNwTaLegend";

    //expected properties of the JSON objects
    private static final String[] JSON_PROPS = {"locationCode", "nwColor", "taColor"};

    //expected length of the color fields: nwColor and taColor
    private static final int COLOR_LENGTH = 6;

    @Test
    public void testCreateCustomTAWithLocations() throws UnsupportedEncodingException {
        getOpenLayers();

        String apply = apply("tradearea",
                config.testMultipleSelectionKeys(), config.testMultipleSelectionCodes(),
                config.testDatesFormattedFrom(), config.testDatesFormattedTo(),
                "tradearea", "issuance,",
                "issuance", "0.60",
                "from", config.testDatesFrom(),
                "to", config.testDatesTo());

        assertTrue(validateSuccess(apply));

        waitForProgressDone("tradearea");

        // call getLegendColor
        String mapUrlGetColor = String.format(GET_LEGEND_COLOR + "?mapInstanceKey=%s", mapInstanceKey);
        HttpPost postGetColor = post(mapUrlGetColor);
        String outputGetColor = exec(postGetColor);

        /* Tests start here */
        // ensures the request was excuted and no exception was thrown
        Assert.assertTrue(!outputGetColor.isEmpty(), "An exception occurent in BaseTest.exec()");

        JSONArray jArray = null;
        jArray = toJSONArray(outputGetColor);

        // ensures the output is a JSON Array
        Assert.assertNotEquals(jArray, null, "Could not create a JSONArray from the servlet result");

        // ensures we received a result for every location in the request
        String[] locations = config.testMultipleSelectionCodes().split(",");
        Assert.assertEquals(locations.length, jArray.size());

        // ensures JSONobjects in the JSONArray are in the correct format
        for (int i = 0; i < jArray.size(); i++) {

            JSONObject obj = (JSONObject) jArray.get(i);
            Assert.assertNotEquals(obj, null, "Element at index " + i + " of the servlet resulting JSONArray is null");

            // ensure obj has all the expected properties (These are defined in JSON_OBJ_PROPS)
            for (String property : JSON_PROPS) {
                Assert.assertTrue(obj.has(property), "JSON Object doesn't have the property " + property);
            }

            // ensure properties are in valid format 
            int nwColorLength = obj.getString(JSON_PROPS[1]).length();
            int taColorLength = obj.getString(JSON_PROPS[2]).length();

            Assert.assertTrue(nwColorLength == COLOR_LENGTH, "nwColor length is not correct. Expected: " + COLOR_LENGTH + ". Got:" + nwColorLength);
            Assert.assertTrue(taColorLength == COLOR_LENGTH, "taColor length is not correct. Expected: " + COLOR_LENGTH + ". Got:" + taColorLength);
        }
    }

}
