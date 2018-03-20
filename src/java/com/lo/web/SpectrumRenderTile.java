/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.web;

import java.awt.geom.AffineTransform;
import com.mapinfo.midev.service.namedresource.v1.ListNamedResourceResponse;
import com.sun.media.jai.codec.ByteArraySeekableStream;
import com.sun.media.jai.codec.SeekableStream;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.media.jai.JAI;
import javax.servlet.ServletException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.util.EntityUtils;
import com.mapinfo.midev.service.namedresource.v1.NamedResource;
import java.util.Base64;
import com.spinn3r.log5j.Logger;
import java.awt.Graphics2D;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

/**
 *
 * @author pjain
 */
public class SpectrumRenderTile {
    
    private static final Logger LOGGER = Logger.getLogger();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//    int corePoolSize = 10;
//    int maxPoolSize = 1000;
//    int keepAliveTime = 120;
//    BlockingQueue<Runnable> workQueue = new SynchronousQueue<Runnable>();
// 
//    ThreadPoolExecutor scheduler = new ThreadPoolExecutor(corePoolSize,
//                         maxPoolSize,
//                         keepAliveTime,
//                         TimeUnit.SECONDS,
//                         workQueue);
    
    private static List<Callable<Object>> tasks;

    private ListTiles tiledObj = ListTiles.getInstance();
    private Tile[] tiles = null;
    byte[] imageInByte = null;
    
    private SpectrumRenderTile() {

    }
    
    private static class Holder {
       private static final SpectrumRenderTile INSTANCE = new SpectrumRenderTile();
    }

    public static SpectrumRenderTile getInstance() {
        return Holder.INSTANCE;
    }
    
    
    

    public byte[] createImageFromTiles(String level1, String x1, String y1) throws ServletException, IOException {

        ListNamedResourceResponse layers = tiledObj.retriveTiles();

        tasks = new ArrayList<>(layers.getNamedResource().size());

        final Tile[] tiles = new Tile[layers.getNamedResource().size()];
        int i = -1;
        final String spectrumServerUrl = "http://dev-amap-lb-492711557.ca-central-1.elb.amazonaws.com";
        final String authorization = Base64.getEncoder().encodeToString(("admin" + ":" + "admin").getBytes());
        final String level = level1;
        final String x = x1;
        final String y = y1;

        for (NamedResource layer : layers.getNamedResource()) {
            final int localI = ++i;
            final NamedResource layerName = layer;
            tasks.add(new Callable<Object>() {
             public Object call() throws Exception {
            String imgUrl = "http://dev-amap-lb-492711557.ca-central-1.elb.amazonaws.com/rest/Spatial/MapTilingService" + layerName.getPath() + "/" + level + "/" + x + ":" + y + "/tile.png";
            System.out.println(imgUrl);
            try {
                CloseableHttpClient httpclient = HttpClients.custom().build();
                HttpUriRequest tileRequest = RequestBuilder.get().setUri(imgUrl).addHeader("Authorization", "Basic " + authorization).build();
                long httpRequestTime = System.currentTimeMillis();
                //LOGGER.debug("Requesting tile from {}, layer {} at zoom {} (x={}/y={}) [{} queued reqs]", spectrumServerUrl, layerName, level, x, y);
                LOGGER.debug("Requesting tile from {}, layer {} at zoom {} (x={}/y={}) [{} queued reqs]", spectrumServerUrl, layerName, "6", "11", "11");

                CloseableHttpResponse imgResponse = httpclient.execute(tileRequest);
                if (imgResponse.getStatusLine().getStatusCode() != 200) {
                    System.out.println("HTTP ERROR" + imgResponse.getStatusLine().getStatusCode());
                    LOGGER.warn("Invalid Tile for layer {} received from Spectrum, it will be skipped when merging (code {})", layerName, imgResponse.getStatusLine().getStatusCode());
                    tiles[localI] = null;
                    return null;
                }
                byte[] content = EntityUtils.toByteArray(imgResponse.getEntity());
                httpRequestTime = System.currentTimeMillis() - httpRequestTime;
                //LOGGER.debug("Got tile in {} ms [layer {} at zoom {}](x={}/y={})", httpRequestTime, layerName, level, x, y);
                SeekableStream ss = new ByteArraySeekableStream(content);
                tiles[localI] = new Tile((RenderedImage) JAI.create("stream", ss));
                ss.close();
                return null;
            } catch (Exception e) {
                tiles[localI] = null;
                System.out.print(e);
                LOGGER.warn("An error occured while getting a tile from Spectrum", e);
                throw e;
            }
        }
        });}

        try {
            scheduler.invokeAll(tasks);
        } catch (Exception e) {
            LOGGER.error(e);
            throw new ServletException(e);
        }
        byte[] mergedTile = getMergedTiles(tiles);

        return mergedTile;

    }

    long threadingTime = System.currentTimeMillis();

    public byte[] getMergedTiles(Tile[] tiles) throws ServletException, IOException {

        threadingTime = System.currentTimeMillis() - threadingTime;
        //check that we got all tiles
        for (Tile tile : tiles) {
            if (tile == null) {
                throw new ServletException("At least one layer failed to render.");
            }
        }

        long mergingTime = System.currentTimeMillis();
        BufferedImage finalTile = mergeTiles(tiles);
        mergingTime = System.currentTimeMillis() - mergingTime;

        byte[] imageInByte = null;
        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(finalTile, "png", baos);
            baos.flush();
            imageInByte = baos.toByteArray();
            baos.close();

        } catch (IOException e) {
            LOGGER.error(e);
        }
//        for (byte b: imageInByte) {
//            
//            // converts byte to character
//            char c = (char)b;
//            System.out.print("Img Bytes");
//            System.out.print(c);
//         }
        // LOGGER.info("Merged {} level {} tiles in {}ms+{}ms [x={}/y={}]", tasks.size(), "7", threadingTime, mergingTime, x, y);
        return imageInByte;
    }

    public BufferedImage mergeTiles(Tile[] tiles) {
        try {
            BufferedImage finalTile = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = finalTile.createGraphics();
            AffineTransform noTransform = new AffineTransform();
            AffineTransform resizeTransform = new AffineTransform();
            resizeTransform.scale(256, 256);

            for (int i = 0; i < tiles.length; i++) {
                if (null != tiles[i]) {
                    RenderedImage image = tiles[i].getTile();
                    System.out.println("Merging");
                    System.out.println(i);
                    if (image.getWidth() == 1) {
                        g2d.drawRenderedImage(image, resizeTransform);
                    } else {
                        g2d.drawRenderedImage(image, noTransform);
                    }
                }
                //saveImage(finalTile, i);
            }
            return finalTile;
        } catch (Exception e) {
            LOGGER.error("Error while merging tiles", e);
            return null;
        }
    }

    public void saveImage(BufferedImage image, int i) {

        try {
            File output = new File(i + "output.png");
            System.out.println(output.getAbsolutePath());
            ImageIO.write(image, "png", output);
        } catch (IOException log) {
            System.out.println(log);
        }

    }

//    public static void main(String[] args) throws ServletException, IOException {
//        SpectrumRenderTile tile = new SpectrumRenderTile();
//       byte[] a = tile.createImageFromTiles();
//        //byte[] a = tile.getMergedTiles();
//        System.out.println(Arrays.toString(a));
//    }
}
