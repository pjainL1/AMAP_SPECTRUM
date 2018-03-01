package com.korem.tests;

import com.korem.amap.tests.config.TestConfig;
import com.korem.config.ConfigManager;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.testng.annotations.BeforeClass;
import static org.testng.Assert.*;

/**
 *
 * @author jduchesne
 */
public abstract class BaseTest {

    protected TestConfig config;
    protected String testUrl;

    public BaseTest() {

    }

    protected void setTestUrl(String testUrl) {
        this.testUrl = testUrl;
    }

    @BeforeClass
    public void setUp() throws IOException {
        try {
            loadConfig();
            this.setTestUrl(getServerUrl());
        } catch (IOException ex) {
            Logger.getLogger(BaseTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.err.printf("Using server %s.\n", getServerUrl());
    }

    protected String getServerUrl() {
        return config.testServer();
    }

    private void loadConfig() throws IOException {
        String configPathFromEnv = System.getProperty("testConfigPath");
        System.err.printf("configPathFromEnv: %s\n", configPathFromEnv);
        String defaultConfigPath = getClass().getResource("/testConfig.properties").getFile();
        System.err.printf("defaultConfigPath: %s\n", defaultConfigPath);
        String configPath;
        if (configPathFromEnv != null && configPathFromEnv.length() > 0) {
            configPath = configPathFromEnv;
        } else {
            configPath = defaultConfigPath;
        }
        try {
            System.err.printf("configPath: %s\n", configPath);
            config = ConfigManager.get().getConfig(new File(configPath), TestConfig.class);
        } catch (Exception e) {
            System.err.printf("Path %s could not be used to load test config.\n", configPath);
            config = ConfigManager.get().getConfig(new File(defaultConfigPath), TestConfig.class);
        }
    }

    protected byte[] getFileContent(File file) throws FileNotFoundException {
        return getInputStreamContent(new FileInputStream(file), (int) file.length());
    }

    protected byte[] getInputStreamContent(InputStream is, int len) {
        int off = 0;
        int read;
        byte[] content = new byte[len];
        try {
            BufferedInputStream bis = new BufferedInputStream(is);
            while (len > 0 && (read = bis.read(content, off, len)) > -1) {
                off += read;
                len -= read;
            }
            bis.close();
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    protected byte[] getInputStreamContent(InputStream is) {
        final int LEN = 1024;
        int read;
        byte[] content = new byte[LEN];
        ArrayList<byte[]> allContent = new ArrayList<byte[]>();
        int totalSize = 0;
        try {
            BufferedInputStream bis = new BufferedInputStream(is);
            while ((read = bis.read(content, 0, LEN)) > -1) {
                allContent.add(Arrays.copyOfRange(content, 0, read));
                totalSize += read;
            }
            bis.close();

            byte[] totalContent = new byte[totalSize];
            int totalIndex = -1;
            for (byte[] part : allContent) {
                for (int i = 0; i < part.length; ++i) {
                    totalContent[++totalIndex] = part[i];
                }
            }
            return totalContent;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    protected byte[] getURLContent(String url) {
        try {
            URLConnection conn = new URL(url).openConnection();
            return getInputStreamContent(conn.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    private void writeBytes(String filename, byte[] bytes) {
        try {
            File file = new File("testTemp", filename);
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fos = new FileOutputStream(file);
            try {
                fos.write(bytes);
            } finally {
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected boolean areTheSame(byte[] first, byte[] second) {
        return areTheSame(first, second, null);
    }

    protected boolean areTheSame(byte[] original, byte[] current, String comparisonName) {
        if (comparisonName != null) {
            // Saves it for further validation on the build server.
            writeBytes(comparisonName, current);
        }
        return Arrays.equals(original, current);
    }

    protected void sleep(long timeInMilli) {
        try {
            Thread.sleep(timeInMilli);
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    protected void notImplemented() {
        fail("Not Implemented.");
    }
}
