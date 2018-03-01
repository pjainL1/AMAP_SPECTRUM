/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.hosting;

import com.korem.mail.ContentMimeBodyPart;
import com.korem.mail.MailMessage;
import com.spinn3r.log5j.Logger;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import javax.mail.BodyPart;

/**
 *
 * @author YDumais
 */
public class Mail {

    private static final Logger log = Logger.getLogger();
    private final String msg;
    private static final String EMAIL_FORMAT = "text/html";
    private final String from;
    private final String subject;
    private final String to;

    public Mail(String from, String to, String subject, String msg) {
       
        this.from = from; //Config.getInstance().getValue("mail.from");
        this.to = to; //Config.getInstance().getValue("mail.to");
        this.subject = subject ;//Config.getInstance().getValue("mail.subject");
        this.msg = msg;
    }

    public void send() {
        try {
            MailMessage message = createMessageBase();
            message.addBodyPart(createContent());
            message.send();
            
        } catch (Exception e) {
            log.error("An error occured sending an email.", e); 
        }
    }

    private MailMessage createMessageBase() throws Exception {
        if (Config.getInstance().getMailUser() != null && Config.getInstance().getMailPass() != null) {
            return new MailMessage(to, subject, from,
                    Config.getInstance().getMailHost(),
                    Config.getInstance().getMailPort(),
                    Config.getInstance().getMailUser(),
                    Config.getInstance().getMailPass());
        } else {
            return new MailMessage(to, subject, from,
                    Config.getInstance().getMailHost(),
                    Config.getInstance().getMailPort());
        }
    }

    private BodyPart createContent() throws MalformedURLException, UnsupportedEncodingException, IOException, Exception {
        
        return new ContentMimeBodyPart(msg, EMAIL_FORMAT).getBodyPart();
    }
}
