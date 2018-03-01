
package com.korem.amap.tests.app;

import com.korem.amap.tests.BaseTest;
import java.io.UnsupportedEncodingException;
import org.apache.http.client.methods.HttpPost;
import org.testng.annotations.Test;

/**
 * before start the test please create your file in D:\data\ftp_directories\inbox
 * your file name have to respect the format define in com\lo\hosting\config.properties (e.g: sponsor_location_[12 number].csv.gz)
 * the error will send by email config.properties loading.alert.toEmail
 * @author smukena
 */
public class TestTransactions  extends BaseTest {
    
    //URL-PATTERN
    private static final String LOAD_URL = "/secure/forceLoad.do";
    @Test
    public void testLoader() throws UnsupportedEncodingException{
        HttpPost post = post(LOAD_URL);
            addPostKeyValuePairs(post, 
                    "password", config.testTransactionPassword());
            exec(post);
    }
}
