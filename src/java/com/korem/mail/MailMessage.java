/*
 * MailMessage.java
 *
 * Created on 2 novembre 2001, 08:30
 */
package com.korem.mail;

import java.util.Date;
import java.util.Properties;
import java.util.Vector;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 *
 * @author  antoine
 * @version 
 */
public class MailMessage {

    private static final String KEY_PROTO = "mail.smtp.";
    private static final String KEY_HOST = KEY_PROTO + "host";
    private static final String KEY_PORT = KEY_PROTO + "port";
    private static final String KEY_AUTH = KEY_PROTO + "auth";
    private static final String VALUE_TRUE = "true";
    private static final String VALUE_FALSE = "false";
    private Vector parts = new Vector();
    private Message msg = null;
    private Session session = null;

    public MailMessage(String to, String subject, String from, String mailhost, String mailPort)
            throws Exception {
        setMessage(getSession(mailhost, mailPort), to, subject, from);
    }

    public MailMessage(String to, String subject, String from, String mailhost,
            String username, String password, String mailPort)
            throws Exception {
        setMessage(getSession(mailhost, mailPort, username, password), to, subject, from);
    }

    private void setMessage(Session session, String to, String subject, String from) throws MessagingException {
        this.session = session;
        msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
        msg.setSubject(subject);
        msg.setSentDate(new Date());
    }

    private static Session getSession(String mailHost, String mailPort) {
        return Session.getDefaultInstance(getProperties(mailHost, mailPort, VALUE_FALSE));
    }

    private static Session getSession(String mailHost, String mailPort,
            final String username, final String password) {
        return Session.getDefaultInstance(getProperties(mailHost, mailPort, VALUE_TRUE),
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
    }

    private static Properties getProperties(String mailHost, String mailPort, String auth) {
        Properties props = System.getProperties();
        props.put(KEY_HOST, mailHost);
        props.put(KEY_PORT, mailPort);
        props.put(KEY_AUTH, auth);
        return props;
    }

    public void setCc(String cc) throws MessagingException {
        msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc, false));
    }

    public void setBcc(String bcc) throws MessagingException {
        msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc, false));
    }

    public void send() throws Exception {
        msg.setContent(getMimeMultipart());
        Transport.send(msg);
    }

    public void addBodyPart(BodyPart part) {
        parts.addElement(part);
    }

    private MimeMultipart getMimeMultipart() throws Exception {
        MimeMultipart mp = new MimeMultipart();
        for (int i = 0; i < parts.size(); i++) {
            mp.addBodyPart((BodyPart) parts.elementAt(i));
        }
        return mp;
    }
}
