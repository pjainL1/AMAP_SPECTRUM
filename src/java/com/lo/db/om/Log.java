/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.db.om;

import java.util.Date;

/**
 *
 * @author ydumais
 */
public class Log {

    private Date datetime;
    private String login;
    private String sponsor;
    private String description;

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSponsor() {
        return sponsor;
    }

    public void setSponsor(String sponsor) {
        this.sponsor = sponsor;
    }
}
