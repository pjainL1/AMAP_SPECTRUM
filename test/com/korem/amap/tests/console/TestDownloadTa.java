package com.korem.amap.tests.console;

import com.korem.amap.tests.app.TestCustomTA;
import com.spinn3r.log5j.Logger;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.client.methods.HttpPost;
import org.testng.Assert;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author rarif
 */
@Test(singleThreaded = true)
public class TestDownloadTa extends ConsoleBaseTest {

    private static final Logger LOGGER = Logger.getLogger();

    public TestDownloadTa() {
        super("console/TradeAreaHistoryManagement.safe");
    }

    @Override
    public void setUp() throws IOException {
        super.setUp();
    }

    @Test
    void testLoadTaHistoryDataGrid() throws UnsupportedEncodingException {
        testLoadDataGrid();
    }

    @Test
    void testTaHistorySearchDataGrid() throws UnsupportedEncodingException {
        testSearchDataGrid("BOST");
    }

    @Test
    void testTaHistorySearchDataGridInvalidSearch() throws UnsupportedEncodingException {
        testSearchDataGridInvalidSearch();
    }

    @Test
    void testFilterTaHistoryDataGridEqualityOnString() throws UnsupportedEncodingException {
        testFilterDataGridEqualityOnString("ADM", "userLogin", "tahistory");
    }

    @Test
    void testFilterTaHistoryDataGridOnTwoColumn() throws UnsupportedEncodingException {
        testFilterDataGridOnTwoColumn(75445, "DIS", "sponsorLocationCode", "type", "tahistory");
    }

    @Test
    void testSortingTaHistoryDataGridASC() throws UnsupportedEncodingException {
        testSortingDataGridASC("type", "tahistory");
    }

    @Test
    void testSortingTaHistoryDataGridDESC() throws UnsupportedEncodingException {
        testSortingDataGridDESC("type", "tahistory");
    }

    @Test
    void testBeforeDateFilteringDataGrid() throws UnsupportedEncodingException, java.text.ParseException {
        String columnOne = "creaDate";
        String jsonResultName = "tahistory";
        HttpPost post = post(this.servletName);
        addPostKeyValuePairs(post,
                "page", 1,
                "start", 0,
                "limit", 25,
                "search", "",
                "filter", "[{\"type\":\"date\",\"comparison\":\"gt\",\"value\":\" " + config.consoleDownloadTaDateBefore() + " \",\"field\":\"" + columnOne + "\"}]");
        String output = exec(post);
        assertTrue(validateSuccess(output), this.servletName + " failed");
        JSONObject json = toJSONObject(output);
        Assert.assertTrue(json.getInt("totalCount") > 0, this.servletName + " failed");
        JSONArray jArray = json.getJSONArray(jsonResultName);

        SimpleDateFormat sdfOutput = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfTest = new SimpleDateFormat("MM/dd/yyyy");

        for (int i = 0; i < jArray.size(); i++) {
            Assert.assertTrue((sdfOutput.parse(jArray.getJSONObject(i).getString(columnOne))).compareTo(sdfTest.parse(config.consoleDownloadTaDateBefore())) >= 0);
        }
    }
    
    private Date dayBefore(Date date) {
        Calendar cal = Calendar.getInstance();
        
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        return cal.getTime();
    }

    @Test
    void testAfterDateFilteringDataGrid() throws UnsupportedEncodingException, java.text.ParseException {
        SimpleDateFormat sdfTest = new SimpleDateFormat("MM/dd/yyyy");
        
        Date yesterday = dayBefore(new Date());
        
        String dateAfter = sdfTest.format(yesterday);
        String columnOne = "creaDate";
        String jsonResultName = "tahistory";

        HttpPost post = post(servletName);
        addPostKeyValuePairs(post,
                "page", 1,
                "start", 0,
                "limit", 25,
                "search", "",
                "filter", "[{\"type\":\"date\",\"comparison\":\"gt\",\"value\":\" " + dateAfter + " \",\"field\":\"" + columnOne + "\"}]");
        String output = exec(post);
        assertTrue(validateSuccess(output), servletName + " success false");
        JSONObject json = toJSONObject(output);
        assertTrue(json.getInt("totalCount") >= 0, servletName + " totalCount is 0");
        JSONArray jArray = json.getJSONArray(jsonResultName);
        SimpleDateFormat sdfOutput = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        
        for (int i = 0; i < jArray.size(); i++) {
            String jsonDate = jArray.getJSONObject(i).getString(columnOne);
            assertTrue((sdfOutput.parse(jsonDate)).compareTo(yesterday) >= 0, String.format("%s not >= %s", jsonDate, yesterday));
        }
    }

    @Test
    void testOnDateFilteringDataGrid() throws UnsupportedEncodingException, java.text.ParseException {
        String dateFrom = yesterdayAsString();
        String dateTo = todayAsString();
        
        String columnOne = "creaDate";
        String jsonResultName = "tahistory";

        HttpPost post = post(this.servletName);
        addPostKeyValuePairs(post,
                "page", 1,
                "start", 0,
                "limit", 25,
                "search", "",
                "filter", "[{\"type\":\"date\",\"comparison\":\"gt\",\"value\":\""+dateFrom+"\",\"field\":\"creaDate1\"},{\"type\":\"date\",\"comparison\":\"lt\",\"value\":\""+dateTo+"\",\"field\":\"creaDate2\"}]");
        String output = exec(post);
        assertTrue(validateSuccess(output), this.servletName + " failed");
        JSONObject json = toJSONObject(output);
        Assert.assertTrue(json.getInt("totalCount") >= 0, this.servletName + " failed");
        JSONArray jArray = json.getJSONArray(jsonResultName);
        for (int i = 0; i < jArray.size(); i++) {
            Date date = parseDate(jArray.getJSONObject(i).getString(columnOne));
            Assert.assertTrue(date.after(yesterday()) || date.equals(yesterday()));
            Assert.assertTrue(date.before(tomorrow()));
        }
    }
}
