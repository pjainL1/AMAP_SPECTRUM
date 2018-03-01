/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.db.om;

import com.lo.pdf.PDFBean;
import com.lo.pdf.PDFProcessProgressListenerKey;

/**
 *
 * @author mdube
 */
public class PDFReport {

    private String userName;
    private PDFProcessProgressListenerKey processKey;
    private PDFBean bean;

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the bean
     */
    public PDFBean getBean() {
        return bean;
    }

    /**
     * @param bean the bean to set
     */
    public void setBean(PDFBean bean) {
        this.bean = bean;
    }

    /**
     * @return the processKey
     */
    public PDFProcessProgressListenerKey getProcessKey() {
        return processKey;
    }

    /**
     * @param processKey the processKey to set
     */
    public void setProcessKey(PDFProcessProgressListenerKey processKey) {
        this.processKey = processKey;
    }
    
    /**
     * 
     * @return 
     */
    public String getStatus() {
        if (processKey != null) {
            return Status.process.toString();
        }
        if (bean != null) {
            return Status.ready.toString();
        }
        return Status.none.toString();
    }
    
    private enum Status {
        process,
        none,
        ready;
    }
}
