package com.korem.amap.tests.console;

import com.korem.amap.tests.BaseTest;
import com.spinn3r.log5j.Logger;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpPost;
import org.testng.Assert;
import static org.testng.Assert.assertTrue;

/**
 *
 * @author rarif
 */
public class ConsoleBaseTest extends BaseTest {
    private static final String EXTJS_DATE_FORMAT = "MM/dd/yyyy";
    private static final String JSON_DATE_FORMAT = "yyyy-MM-dd";

    @Override
    protected String getServerUrl() {
        return config.testConsoleServer();
    }

    private static final Logger LOGGER = Logger.getLogger();

    String servletName;

    public ConsoleBaseTest(String servletName) {
        this.servletName = servletName;
    }
    
    @Override
    protected String initMap() throws UnsupportedEncodingException {
        login();
        return "";
    }
    
    protected Object[] getOtherParams() {
        return new Object[] {};
    }
    
    @Override
    protected void login() throws UnsupportedEncodingException{
	HttpPost post = post(config.testConsoleLogin());
        exec(post);
    }

    void testLoadDataGrid() throws UnsupportedEncodingException {

        HttpPost post = post(this.servletName);
        addPostKeyValuePairs(post,
                getOtherParams(),
                "page", 1,
                "start", 0,
                "limit", 25);
        String output = exec(post);
        assertTrue(validateSuccess(output), this.servletName+" failed");
        JSONObject json = toJSONObject(output);
        Assert.assertTrue(json.getInt("totalCount") > 0);
    }

    
    void testSearchDataGrid(String search) throws UnsupportedEncodingException {
        HttpPost post = post(this.servletName);
        addPostKeyValuePairs(post,
                getOtherParams(),
                "page", 1,
                "start", 0,
                "limit", 25,
                "search", search.toUpperCase());
        String output = exec(post);
        assertTrue(validateSuccess(output), this.servletName+" failed");
        JSONObject json = toJSONObject(output);
        Assert.assertTrue(json.getInt("totalCount") >= 0);
    }

    
    void testSearchDataGridInvalidSearch() throws UnsupportedEncodingException {
        HttpPost post = post(this.servletName);
        addPostKeyValuePairs(post,
                getOtherParams(),
                "page", 1,
                "start", 0,
                "limit", 25,
                "search", "xyz");
        String output = exec(post);
        assertTrue(validateSuccess(output), this.servletName+" failed");
        JSONObject json = toJSONObject(output);
        Assert.assertTrue(json.getInt("totalCount") == 0);
    }

    
    void testFilterDataGridWithEqualityOnNumeric(int ID, String columnOne, String jsonResultName) throws UnsupportedEncodingException {
        HttpPost post = post(this.servletName);
        addPostKeyValuePairs(post,
                getOtherParams(),
                "page", 1,
                "start", 0,
                "limit", 25,
                "search", "",
                "filter", "[{\"type\":\"numeric\",\"comparison\":\"eq\",\"value\":"+ID+",\"field\":\""+columnOne+"\"}]");
        String output = exec(post);
        assertTrue(validateSuccess(output), this.servletName+" failed");
        JSONObject json = toJSONObject(output);
        Assert.assertTrue(json.getInt("totalCount") >= 0);
        JSONArray jArray = json.getJSONArray(jsonResultName);
        Assert.assertTrue(jArray.getJSONObject(0).getInt(columnOne) == ID);
    }

    
    void testFilterDataGridWithLowerThanOnNumeric(int ID, String columnOne, String jsonResultName) throws UnsupportedEncodingException {
        HttpPost post = post(this.servletName);
        addPostKeyValuePairs(post,
                getOtherParams(),
                "page", 1,
                "start", 0,
                "limit", 25,
                "search", "",
                "filter", "[{\"type\":\"numeric\",\"comparison\":\"lt\",\"value\":"+ID+",\"field\":\""+columnOne+"\"}]");
        String output = exec(post);
        assertTrue(validateSuccess(output), this.servletName+" failed");
        JSONObject json = toJSONObject(output);
        Assert.assertTrue(json.getInt("totalCount") >= 0);
        JSONArray jArray = json.getJSONArray(jsonResultName);
        for (int i = 0; i < jArray.size(); i++) {
            Assert.assertTrue(jArray.getJSONObject(i).getInt(columnOne) <= ID);
        }
    }

    
    void testFilterDataGridWithGreaterThanOnNumeric(int ID, String columnOne, String jsonResultName) throws UnsupportedEncodingException {
        HttpPost post = post(this.servletName);
        addPostKeyValuePairs(post,
                getOtherParams(),
                "page", 1,
                "start", 0,
                "limit", 25,
                "search", "",
                "filter", "[{\"type\":\"numeric\",\"comparison\":\"gt\",\"value\":"+ID+",\"field\":\""+columnOne+"\"}]");
        String output = exec(post);
        assertTrue(validateSuccess(output), this.servletName+" failed");
        JSONObject json = toJSONObject(output);
        Assert.assertTrue(json.getInt("totalCount") >= 0, this.servletName+" failed");
        JSONArray jArray = json.getJSONArray(jsonResultName);
        for (int i = 0; i < jArray.size(); i++) {
            Assert.assertTrue(jArray.getJSONObject(i).getInt(columnOne) >= ID);
        }
    }

    
    void testFilterDataGridEqualityOnString(String search, String columnTwo, String jsonResultName) throws UnsupportedEncodingException {
        HttpPost post = post(this.servletName);
        addPostKeyValuePairs(post,
                getOtherParams(),
                "page", 1,
                "start", 0,
                "limit", 5,
                "search", "",
                "filter", "[{\"type\":\"string\",\"value\":\""+search+"\",\"field\":\""+columnTwo+"\"}]");
        String output = exec(post);
        assertTrue(validateSuccess(output), this.servletName+" failed");
        JSONObject json = toJSONObject(output);
        Assert.assertTrue(json.getInt("totalCount") >= 0, this.servletName+" failed");
        JSONArray jArray = json.getJSONArray(jsonResultName);
        for (int i = 0; i < jArray.size(); i++) {
            Assert.assertTrue(jArray.getJSONObject(i).getString(columnTwo).startsWith(search.toUpperCase()) || jArray.getJSONObject(i).getString(columnTwo).startsWith(search.toLowerCase()));
        }
    }

    
    void testFilterDataGridOnTwoColumn(int ID, String search, String columnOne, String columnTwo, String jsonResultName) throws UnsupportedEncodingException {
        HttpPost post = post(this.servletName);
        addPostKeyValuePairs(post,
                getOtherParams(),
                "page", 1,
                "start", 0,
                "limit", 25,
                "search", "",
                "filter", "[{\"type\":\"numeric\",\"comparison\":\"lt\",\"value\":"+ID+",\"field\":\""+columnOne+"\"},{\"type\":\"string\",\"value\":\""+search+"\",\"field\":\""+columnTwo+"\"}]");
        String output = exec(post);
        assertTrue(validateSuccess(output), this.servletName+" failed");
        JSONObject json = toJSONObject(output);
        Assert.assertTrue(json.getInt("totalCount") >= 0, this.servletName+" failed");
        JSONArray jArray = json.getJSONArray(jsonResultName);
        for (int i = 0; i < jArray.size(); i++) {
            Assert.assertTrue(jArray.getJSONObject(i).getString(columnTwo).startsWith(search.toUpperCase()) || jArray.getJSONObject(i).getString(columnTwo).startsWith(search.toLowerCase()) );
            Assert.assertTrue(jArray.getJSONObject(i).getInt(columnOne) <= ID);
        }
    }

    
    void testSortingDataGridASC(String columnOne, String jsonResultName) throws UnsupportedEncodingException {
        HttpPost post = post(this.servletName);
        addPostKeyValuePairs(post,
                getOtherParams(),
                "page", 1,
                "start", 0,
                "limit", 25,
                "search", "",
                "sort", "[{\"property\":\""+columnOne+"\",\"direction\":\"ASC\"}]");
        String output = exec(post);
        assertTrue(validateSuccess(output), this.servletName+" failed");
        JSONObject json = toJSONObject(output);
        JSONArray jArray = json.getJSONArray(jsonResultName);
        for (int i = 0; i < jArray.size() - 1; i++) {
            String postal1 = jArray.getJSONObject(i).getString(columnOne);
            String postal2 = jArray.getJSONObject(i + 1).getString(columnOne);
            Assert.assertTrue(postal1.compareTo(postal2) <= 0);
        }
    }

    
    void testSortingDataGridDESC(String columnOne, String jsonResultName) throws UnsupportedEncodingException {
        HttpPost post = post(this.servletName);
        addPostKeyValuePairs(post,
                getOtherParams(),
                "page", 1,
                "start", 0,
                "limit", 25,
                "search", "",
                "sort", "[{\"property\":\""+columnOne+"\",\"direction\":\"DESC\"}]");
        String output = exec(post);
        assertTrue(validateSuccess(output), this.servletName+" failed");
        JSONObject json = toJSONObject(output);
        Assert.assertTrue(json.getInt("totalCount") >= 0);
        JSONArray jArray = json.getJSONArray(jsonResultName);
        for (int i = 0; i < jArray.size() - 1; i++) {
            String postal1 = jArray.getJSONObject(i).getString(columnOne);
            String postal2 = jArray.getJSONObject(i + 1).getString(columnOne);
            Assert.assertTrue(postal1.compareTo(postal2) >= 0);
        }
    }

    
    void testSortingDataGridWithSearch(String search, String sortColumn, String searchColumn, String jsonResultName) throws UnsupportedEncodingException {
        HttpPost post = post(this.servletName);
        addPostKeyValuePairs(post,
                getOtherParams(),
                "page", 1,
                "start", 0,
                "limit", 25,
                "search", search,
                "sort", "[{\"property\":\""+sortColumn+"\",\"direction\":\"ASC\"}]");
        String output = exec(post);
        assertTrue(validateSuccess(output), this.servletName+" failed");
        JSONObject json = toJSONObject(output);
        Assert.assertTrue(json.getInt("totalCount") >= 0);
        JSONArray jArray = json.getJSONArray(jsonResultName);
        for (int i = 0; i < jArray.size() - 1; i++) {
            Assert.assertTrue(jArray.getJSONObject(i).getInt(sortColumn) <= jArray.getJSONObject(i + 1).getInt(sortColumn));
            Assert.assertTrue(jArray.getJSONObject(i).getString(searchColumn).startsWith(search.toUpperCase()) || jArray.getJSONObject(i).getString(searchColumn).startsWith(search.toLowerCase()));
        }
    }

    
    void testPagingDataGrid(String columnOne, String jsonResultName) throws UnsupportedEncodingException {
        HttpPost post = post(this.servletName);
        addPostKeyValuePairs(post,
                getOtherParams(),
                "page", 1,
                "start", 0,
                "limit", 25);
        String outputFirstPage = exec(post);
        JSONObject jsonFirstPage = toJSONObject(outputFirstPage);

        addPostKeyValuePairs(post,
                getOtherParams(),
                "page", 2,
                "start", 25,
                "limit", 50);
        String outputSecondPage = exec(post);
        JSONObject jsonSecondPage = toJSONObject(outputSecondPage);

        JSONArray jArrayFirst = jsonFirstPage.getJSONArray(jsonResultName);
        JSONArray jArraySecond = jsonSecondPage.getJSONArray(jsonResultName);

        for (int i = 0; i < 24; i++) {
            Assert.assertTrue(jArrayFirst.getJSONObject(i).getInt(columnOne) != jArraySecond.getJSONObject(i).getInt(columnOne));
        }
    }
    
    protected Date parseDate(String date) throws java.text.ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(JSON_DATE_FORMAT);
        SimpleDateFormat sdf2 = new SimpleDateFormat(EXTJS_DATE_FORMAT);
        
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            return sdf2.parse(date);
        }
        
    }
    
    protected Date today() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.getTime();
    }
    
    protected String todayAsString() {
        SimpleDateFormat format = new SimpleDateFormat(EXTJS_DATE_FORMAT);
        return format.format(today());
    }
    
    protected Date yesterday() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(today());
        
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.getTime();
    }
    
    protected String yesterdayAsString() {
        SimpleDateFormat format = new SimpleDateFormat(EXTJS_DATE_FORMAT);
        return format.format(yesterday());
    }
    
    protected Date tomorrow() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }
}
