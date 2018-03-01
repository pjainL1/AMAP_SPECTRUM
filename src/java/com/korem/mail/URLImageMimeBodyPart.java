/*
 * ImageMimeBodyPart.java
 *
 * Created on 1 novembre 2001, 17:45
 */

package com.korem.mail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.internet.MimeBodyPart;

/**
 *
 * @author  antoine
 * @version 
 */
public class URLImageMimeBodyPart {
    private BodyPart part = null;
    /** Creates new ImageMimeBodyPart */
    public URLImageMimeBodyPart(URL imageURL,String imageHeaderValue) throws Exception{
        part = getImagePart(convertURLToFile(imageURL),imageHeaderValue);        
    }
    public BodyPart getBodyPart(){
        return part;
    }
    private MimeBodyPart getImagePart(File file,String imageHeaderValue)throws Exception{
        MimeBodyPart mbp2 = new MimeBodyPart();
        mbp2.setHeader("Content-ID",imageHeaderValue);
        mbp2.setDataHandler(new DataHandler(new FileDataSource(file)));        
        return mbp2;
    }    
    private File convertURLToFile(URL imageURL) throws Exception{
        FileOutputStream fos = null;
        InputStream is = null;
        File file = null;
        try{
            java.net.URLConnection con = imageURL.openConnection();
            is = con.getInputStream();
            String ext = ".gif";
            if(con.getContentType().toLowerCase().indexOf("gif")==-1)
                ext = ".jpg";
            file = File.createTempFile("sendToFriend"+System.currentTimeMillis(),ext);
            fos = new FileOutputStream(file);
            byte[] bufferArray = new byte[1024];
            if(is.available()<=0)
                Thread.sleep(500);
            int c;
            while ((c = is.read(bufferArray)) >= 0) {
                fos.write(bufferArray,0,c);
            }
            return file;
        }finally{
            if(is!=null)
                is.close();
            if(fos!=null)
                fos.close();            
        }
    }
}
