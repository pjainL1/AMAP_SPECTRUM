/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.pdf;

import com.spinn3r.log5j.Logger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 *
 * @author slajoie
 */
public class PDFBean implements Serializable {

    private static final Logger log = Logger.getLogger(PDFBean.class);
    private String pdfId;
    private File file;
    private String name;

    public String getPdfId() {
        return pdfId;
    }

    public void setPdfId(String pdfId) {
        this.pdfId = pdfId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    /**
     *
     * @param bb the PDFBean in byte to deserialize
     * @return the PDFBean deserialized
     */
    public static PDFBean deserialize(byte[] bb) {
        if (bb != null) {
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(bb);
                ObjectInputStream ois = new ObjectInputStream(bais);
                return (PDFBean) ois.readObject();
            } catch (IOException ex) {
                log.warn("Unable to deserialize PDFBean [IOException]", ex);
            } catch (ClassNotFoundException ex) {
                log.warn("Unable to deserialize PDFBean [ClassNotFoundException]", ex);
            }
        }
        return null;
    }

    /**
     *
     * @param br the PDFBean in byte to serialize
     * @return the PDFBean serialized
     */
    public static byte[] serialize(PDFBean br) {
        if (br != null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(br);
                oos.close();
                byte[] obj = baos.toByteArray();
                log.debug("PDFBean size [" + obj.length + "]");
                return obj;
            } catch (IOException ex) {
                log.warn("Error serialize PDFBean", ex);
            }
        }
        return null;
    }
}
