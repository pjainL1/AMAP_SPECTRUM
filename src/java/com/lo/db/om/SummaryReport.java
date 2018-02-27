/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.db.om;

import org.apache.commons.lang.WordUtils;

/**
 *
 * @author slajoie
 */
public class SummaryReport {

    private final char[] delimiters = new char[]{
        '-', ' ', '.', ','
    };
    private int collectors;
    private int totalCollectors;
    private int transactions;
    private int totalTransactions;
    private double spends;
    private double totalSpends;
    private double units;
    private double totalUnits;
    private double baseMile;
    private double locationKey;
    private String locationCode;
    private String locationName;
    private String sponsorCode;
    private double distance;
    private String customerLocationCode;
    
    public void setTotalSpends(double totalSpends) {
        this.totalSpends = totalSpends;
    }

    public double getTotalSpends() {
        return totalSpends;
    }
    
    public void setTotalTransactions(int totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public int getTotalTransactions() {
        return totalTransactions;
    }
    
    public void setTotalCollectors(int totalCollectors) {
        this.totalCollectors = totalCollectors;
    }

    public int getTotalCollectors() {
        return totalCollectors;
    }
    
    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
            this.distance = distance;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public String getLocationName() {
        return nicer(locationName);
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public double getLocationKey() {
        return locationKey;
    }

    public void setLocationKey(double locationKey) {
        this.locationKey = locationKey;
    }

    public double getSpends() {
        return spends;
    }

    public void setSpends(double spends) {
        this.spends = spends;
    }

    public int getCollectors() {
        return collectors;
    }

    public void setCollectors(int collectors) {
        this.collectors = collectors;
    }

    public int getTransactions() {
        return transactions;
    }

    public void setTransactions(int transactions) {
        this.transactions = transactions;
    }

    public double getBaseMile() {
        return baseMile;
    }

    public void setBaseMile(double baseMile) {
        this.baseMile = baseMile;
    }

    public double getUnits() {
        return units;
    }

    public void setUnits(double units) {
        this.units = units;
    }

    public double getTotalUnits() {
        return totalUnits;
    }

    public void setTotalUnits(double totalUnits) {
        this.totalUnits = totalUnits;
    }

    public String getSponsorCode() {
        return sponsorCode;
    }

    public void setSponsorCode(String sponsorCode) {
        this.sponsorCode = sponsorCode;
    }
    
    private String nicer(String string) {
        return WordUtils.capitalizeFully(string, delimiters);
    }
    
    public String getCustomerLocationCode() {
        return this.customerLocationCode ;
    }
    
    public void setCustomerLocationCode(String customerLocationCode) {
        this.customerLocationCode = customerLocationCode;
    }
    
}
