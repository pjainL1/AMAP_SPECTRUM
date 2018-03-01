package com.lo.db.om;

public class LocationColor {

    int sponsorLocationKey;
    String sponsorLocationCode;
    String sponsorLocationName;
    String city = null;
    String postalCode = null;
    String nwatchColor;
    String taColor;

    public LocationColor(int sponsorLocationKey, String sponsorLocationCode, String sponsorLocationName, String city, String postalCode, String nwatchColor, String taColor) {
        this.sponsorLocationCode = sponsorLocationCode;
        this.sponsorLocationKey = sponsorLocationKey;
        this.sponsorLocationName = sponsorLocationName;
        this.city = city;
        this.postalCode = postalCode;
        this.nwatchColor = nwatchColor;
        this.taColor = taColor;
    }

    public String getSponsorLocationCode() {
        return sponsorLocationCode;
    }

    public void setSponsorLocationCode(String sponsorLocationCode) {
        this.sponsorLocationCode = sponsorLocationCode;
    }

    public String getSponsorLocationName() {
        return sponsorLocationName;
    }

    public void setSponsorLocationName(String sponsorLocationName) {
        this.sponsorLocationName = sponsorLocationName;
    }

    public int getSponsorLocationKey() {
        return sponsorLocationKey;
    }

    public void setSponsorLocationKey(int sponsorLocationKey) {
        this.sponsorLocationKey = sponsorLocationKey;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getNwatchColor() {
        return nwatchColor;
    }

    public void setNwatchColor(String nwatchColor) {
        this.nwatchColor = nwatchColor;
    }

    public String getTaColor() {
        return taColor;
    }

    public void setTaColor(String taColor) {
        this.taColor = taColor;
    }

}
