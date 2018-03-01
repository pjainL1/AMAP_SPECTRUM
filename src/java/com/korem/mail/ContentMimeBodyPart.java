/*
 * ContentMimeBodyPart.java
 *
 * Created on 2 novembre 2001, 08:27
 */

package com.korem.mail;

import javax.mail.BodyPart;
import javax.mail.internet.MimeBodyPart;

/**
 *
 * @author  antoine
 * @version 
 */
public class ContentMimeBodyPart {
    private BodyPart part = null;
    /** Creates new ContentMimeBodyPart */
    public ContentMimeBodyPart(String content,String contentType) throws Exception{
        MimeBodyPart mbp1= new MimeBodyPart();
        mbp1.setContent(content,contentType);      
        part = mbp1;
    }
    public BodyPart getBodyPart(){
        return part;
    }
}
