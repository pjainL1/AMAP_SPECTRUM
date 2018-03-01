/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.pdf;

import com.spinn3r.log5j.Logger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 *
 * @author mdube
 */
public class PDFProcessProgressListenerKey implements Serializable {

    private static final Logger log = Logger.getLogger(PDFProcessProgressListenerKey.class);
    private long id;
    private String name;

    public PDFProcessProgressListenerKey(long id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @param bb the ProgressListenerKey in byte to deserialize
     * @return the ProgressListenerKey deserialized
     */
    public static PDFProcessProgressListenerKey deserialize(byte[] bb) {
        if (bb != null) {
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(bb);
                ObjectInputStream ois = new ObjectInputStream(bais);
                return (PDFProcessProgressListenerKey) ois.readObject();
            } catch (IOException ex) {
                log.warn("Unable to deserialize ProgressListenerKey [IOException]", ex);
            } catch (ClassNotFoundException ex) {
                log.warn("Unable to deserialize ProgressListenerKey [ClassNotFoundException]", ex);
            }
        }
        return null;
    }

    /**
     *
     * @param br the ProgressListenerKey in byte to serialize
     * @return the ProgressListenerKey serialized
     */
    public static byte[] serialize(PDFProcessProgressListenerKey br) {
        if (br != null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(br);
                oos.close();
                byte[] obj = baos.toByteArray();
                log.debug("ProgressListenerKey size [" + obj.length + "]");
                return obj;
            } catch (IOException ex) {
                log.warn("Error serialize ProgressListenerKey", ex);
            }
        }
        return null;
    }
}
