package com.lo.db.om;

import java.util.List;
import org.apache.commons.lang.WordUtils;

/**
 *
 * @author ydumais
 */
public class Location {

    private final char[] delimiters = new char[]{
        '-', ' ', '.', ','
    };
    private double key;
    private String code;
    private String locationName;
    private double longitude;
    private double latitude;
    private String city;
    private String provinceCode;
    private String postalCode;
    private double isodistance;
    private int count;
    private double totalSpend;
    private int sponsorKey;
    private String sponsorCode;
    private String customerLocationCode;
    
    private List<Collector> collectors;

    public Location copy() {
        Location copy = new Location();
        copy.setKey(getKey());
        copy.setLocationName(getLocationName());
        copy.setLongitude(getLongitude());
        copy.setLatitude(getLatitude());
        copy.setCity(getCity());
        copy.setProvinceCode(getProvinceCode());
        copy.setPostalCode(getPostalCode());
        copy.setIsodistance(getIsodistance());
        copy.setCount(getCount());
        copy.setSponsorKey(getSponsorKey());
        copy.setCollectors(getCollectors());
        copy.setCustomerLocationCode(getCustomerLocationCode());
        return copy;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getCity() {
        return nicer(city);
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getKey() {
        return key;
    }

    public void setKey(double key) {
        this.key = key;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getLocationName() {
        return nicer(locationName);
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public double getIsodistance() {
        return isodistance;
    }

    public void setIsodistance(double isodistance) {
        this.isodistance = isodistance;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public double getTotalSpend() {
        return totalSpend;
    }

    public void setTotalSpend(double totalSpend) {
        this.totalSpend = totalSpend;
    }

    public int getSponsorKey() {
        return sponsorKey;
    }

    public void setSponsorKey(int sponsorKey) {
        this.sponsorKey = sponsorKey;
    }

    public void setCollectors(List<Collector> collectors) {
        this.collectors = collectors;
    }

    public List<Collector> getCollectors() {
        return collectors;
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

    public String getSponsorCode() {
        return sponsorCode;
    }

    public void setSponsorCode(String sponsorCode) {
        this.sponsorCode = sponsorCode;
    }
    
}
