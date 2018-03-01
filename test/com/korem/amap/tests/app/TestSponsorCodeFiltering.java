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
public class TestSponsorCodeFiltering extends BaseTest {

    //Static LOGGER
    private static final Logger LOGGER = Logger.getLogger();

    //URL-PATTERN
    private static final String APPLY_URL_PATTERN = "/apply.safe";
    private static final String GET_SPONSOR_CODES_URL = "/GetSponsorCodes.safe";
    private static final String UPDATE_LOCATIONS_URL = "/updateLocations.safe";
    private static final String RESET_SELECTION_URL = "/reSetSelection.safe";
    private static final String SET_SELECTION_URL = "/setSelection.safe";

    /**
     *
     * @throws IOException
     */
    @Test
    void testSponsorCodeFilteringOnLoad() {
        String mapUrl = String.format(GET_SPONSOR_CODES_URL);
        HttpPost post = post(mapUrl);
        String output = exec(post);
        JSONArray sponsorFilters = JSONArray.fromObject(output);
        for (int i = 0; i < sponsorFilters.size(); i++) {
            assertTrue(!JSONObject.fromObject(sponsorFilters.get(i)).get("code").equals(""));
        }
    }

    @Test
    void testSponsorCodeFilteringSelection() throws UnsupportedEncodingException {
        HttpPost post = post(UPDATE_LOCATIONS_URL);
        addPostKeyValuePairs(post,
                "filters", config.testFilteringSomeCodes(),
                "from", config.testDatesFormattedFrom(),
                "mapInstanceKey", mapInstanceKey);
        String output = exec(post);
        assertTrue(validateSuccess(output));
    }

    @Test(priority = 0)
    void testSponsorCodeFiltering() throws UnsupportedEncodingException {
        //Set the OpenLayers viewport
        getOpenLayers();
        //Set the selection of locations
        {
            String mapUrl = String.format(SET_SELECTION_URL
                    + "?geometry=" + config.testSingleSelectionGeom()
                    + "&mapInstanceKey=%s"
                    + "&append=" + "true",
                    mapInstanceKey);
            HttpPost post = post(mapUrl);
            String output = exec(post);
            
            assertTrue(toJSONArray(output).size() > 0);
        }

        //Create a TA with locations
        {
            HttpPost post = post(APPLY_URL_PATTERN);
            addPostKeyValuePairs(post, 
                    "methods", "setDates,nwatch,tradearea,",
                    "mapInstanceKey", mapInstanceKey,
                    "locations", config.testSingleSelectionKeys(),
                    "locationsCode", config.testSingleSelectionCodes(),
                    "tradearea", "issuance,",
                    "issuance", "0.60",
                    "from", config.testDatesFormattedFrom(),
                    "to", config.testDatesFormattedTo());
            String output = exec(post);
            
            assertTrue(validateSuccess(output));
        }
        //update locations 
        {
            HttpPost post = post(UPDATE_LOCATIONS_URL);
            addPostKeyValuePairs(post,
                    "filters", config.testFilteringSomeCodes(),
                    "from", "08/23/2012",
                    "to", "08/22/2013",
                    "mapInstanceKey", mapInstanceKey);

            String output = exec(post);
            assertTrue(validateSuccess(output));
        }
        //reset selection  
        {
            HttpPost post = post(RESET_SELECTION_URL);
            addPostKeyValuePairs(post, "mapInstanceKey", mapInstanceKey);
            String output = exec(post);
            String array[] = config.testSingleSelectionKeys().split(",");
            JSONArray sponsorFilters = JSONArray.fromObject(output);
            boolean found = false;
            for (int i = 0; i < sponsorFilters.size(); i++) {
                for (int j = 0; j < sponsorFilters.size(); j++) {
                    double key = Double.parseDouble((String)JSONArray.fromObject(sponsorFilters.get(i)).get(0));
                    if (key == Double.parseDouble(array[j])) {
                        found = true;
                        break;
                    }
                }
            }
            assertTrue(found);
        }
    }
}
