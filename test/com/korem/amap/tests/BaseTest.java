package com.korem.amap.tests;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;


/**
 *
 * @author jphoude
 */
public class BaseTest extends com.korem.tests.BaseTest {
    private HttpClient httpClient;
    private HttpContext httpContext;
    
    protected String mapInstanceKey;
    private JSONObject dates;
    private JSONArray layers;
    
    @BeforeClass
    @Override
    public void setUp() throws IOException {
        super.setUp();
        
        httpClient = new DefaultHttpClient();
        CookieStore cookieStore = new BasicCookieStore();
        httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        
        initMap();
    }
    
    protected HttpGet get(String path) {
        HttpGet get = new HttpGet(testUrl + path);
        return get;
    }
    
    protected HttpPost post(String path) {
        HttpPost post = new HttpPost(testUrl + path);
        return post;
    }
    
    @AfterClass
    public void cleanUp() {
        httpClient.getConnectionManager().shutdown();
    }
    
    protected String exec(HttpUriRequest request) {
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
            return httpClient.execute(request, responseHandler, httpContext);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }
    
    protected HttpResponse plainExec(HttpUriRequest request){
        try {
            return httpClient.execute(request, httpContext);
        }catch (IOException ioe){
            ioe.printStackTrace();
            return null;
        }
    }
    
    protected void login() throws UnsupportedEncodingException{
	HttpPost post = post(config.testServerLogin());
        exec(post);
    }
    
    protected String getNewMapInstanceKey() throws UnsupportedEncodingException{
	HttpPost post = post("getMapInstanceKey.safe");
        addPostKeyValuePairs(post, "workspaceKey", "null");
        JSONObject json = JSONObject.fromObject(exec(post));
        
        return json.getString("mapInstanceKey");
    }
    
    protected void addPostKeyValuePairs(HttpPost request, Object... keyValues) throws UnsupportedEncodingException {
        addPostKeyValuePairs(request, new Object[] {}, keyValues);
    }
    
    private Object[] copyArrays(Object[] a, Object[] b) {
        Object[] result = new Object[a.length + b.length];
        System.arraycopy(a, 0, result, 0,  a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        
        return result;
    }
    
    protected void addPostKeyValuePairs(HttpPost request, Object[] baseKeyValues, Object... keyValues) throws UnsupportedEncodingException {
        Object[] allKeyValues = copyArrays(baseKeyValues, keyValues);
        addPostKeyValuePairsFromArray(request, allKeyValues);
    }
    
    protected void addPostKeyValuePairsFromArray(HttpPost request, Object[] keyValues) throws UnsupportedEncodingException {
        List<NameValuePair> formparams = new ArrayList<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            formparams.add(new BasicNameValuePair((String)keyValues[i], String.valueOf(keyValues[i + 1])));
        }
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        request.setEntity(entity);
    }
    
    protected void doGetOpenLayers(String bbox, int width, int height) {
        String mapUrl = String.format("getOpenLayers.safe?FORMAT=image%%2Fpng&LAYERS=%s&SERVICE=WMS&"
                + "VERSION=1.1.1&REQUEST=GetMap&STYLES=&EXCEPTIONS=application%%2Fvnd.ogc.se_inimage&SRS=EPSG%%3A900913&"
                + "BBOX=%s&"
                + "WIDTH=%s&HEIGHT=%s",
                // LAYERS : (mapInstanceKey)
                // BBOX : -16817368.612493,4141051.9329739,-3080717.3877684,8680799.9160739
                // WIDTH : 1404
                // HEIGHT : 464
                mapInstanceKey, bbox,
                width, height);
        HttpGet get = get(mapUrl);
        exec(get);
    }
    
    protected void getOpenLayers() {
        doGetOpenLayers(config.testDefaultBbox(), config.testDefaultWidth(), config.testDefaultHeight());
    }
    
    protected String getUsefulDates() throws UnsupportedEncodingException {
        HttpPost post = post("getUsefulDates.safe");
        addPostKeyValuePairs(post, "mapInstanceKey", mapInstanceKey);
        return exec(post);
    }
    
    protected JSONArray getLayerControl() throws UnsupportedEncodingException {
        HttpPost post = post("getLayers.safe");
        addPostKeyValuePairs(post, "mapInstanceKey", mapInstanceKey);
        return toJSONArray(exec(post));
    }
    
    protected String setSelection(String selection) throws UnsupportedEncodingException {
        HttpPost post = post("setSelection.safe");
        addPostKeyValuePairs(post, 
                "mapInstanceKey", mapInstanceKey,
                "geometry", selection,
                "append", "true"
        );
        return exec(post);
    }
    
    protected void waitForProgressDone() throws UnsupportedEncodingException {
        waitForProgressDone(null);
    }
    protected void waitForProgressDone(String method) throws UnsupportedEncodingException {
        while (getProgress(method) < 100) {
            sleep(1000);
        }
    }
    protected int getProgress() throws UnsupportedEncodingException {
        return getProgress(null);
    }
    protected int getProgress(String methods) throws UnsupportedEncodingException {
        HttpPost post = post("getProgress.safe");
        if(methods != null){
            addPostKeyValuePairs(post, "mapInstanceKey", mapInstanceKey,
                                       "methods", methods);
        }else{
            addPostKeyValuePairs(post, "mapInstanceKey", mapInstanceKey);
        }
        JSONObject json = toJSONObject(exec(post));
        return json.getInt("progress");
    }
    
    protected String apply(String methods, String locations, String locationsCode, 
            String startDate, String endDate, Object... otherParams) throws UnsupportedEncodingException {
        HttpPost post = post("apply.safe");
        addPostKeyValuePairs(post, 
                otherParams,
                "mapInstanceKey", mapInstanceKey,
                "methods", methods,
                "locations", locations,
                "locationsCode", locationsCode,
                "startDate", startDate,
                "endDate", endDate
        );
        return exec(post);
    }
    
    protected String initMap() throws UnsupportedEncodingException {
        login();
        mapInstanceKey = getNewMapInstanceKey();
        dates = toJSONObject(getUsefulDates());
        layers = getLayerControl();
        getOpenLayers();
        
        return mapInstanceKey;
    }
    
    protected JSONObject toJSONObject(String value) {
        return JSONObject.fromObject(value);
    }
    
    protected JSONArray toJSONArray(String value) {
        return JSONArray.fromObject(value);
    }

    public String getMapInstanceKey() {
        return mapInstanceKey;
    }

    public JSONObject getDates() {
        return dates;
    }

    public JSONArray getLayers() {
        return layers;
    }
    
    protected boolean validateSuccess(String response) {
        JSONObject json = toJSONObject(response);
        
        return Boolean.parseBoolean(json.getString("success"));
    }
    
}
