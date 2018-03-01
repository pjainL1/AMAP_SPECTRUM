package com.lo.analysis.tradearea;

import static com.lo.analysis.tradearea.TradeArea.Type.projected;
import com.vividsolutions.jts.geom.Geometry;
import java.io.Serializable;

/**
 *
 * @author ydumais
 */
public class TradeArea implements Serializable {

    public enum Type {

        issuance, units, distance, projected, custom
    };
    private final String locationCode;
    private final String customerLocationCode;
    private final String sponsorCode;
    private final Double locationKey;
    private final Integer sponsorKey;
    private final Geometry geometry;
    private final int cardinality;
    private final Type type;
    private String rendition;
    private Double projectedLatitude;
    private Double projectedLongitude;

    public TradeArea(String locationCode, Double locationKey, Geometry geometry, Type type, Integer sponsorKey, String customerLocationCode, String sponsorCode) {
        this(locationCode, locationKey, geometry, Integer.MAX_VALUE, type, sponsorKey, customerLocationCode, sponsorCode);
    }

    public TradeArea(String locationCode, Double locationKey, Geometry geometry, int cardinality, Type type, Integer sponsorKey, String customerLocationCode, String sponsorCode) {
        this.locationCode = locationCode;
        this.locationKey = locationKey;
        this.geometry = geometry;
        this.cardinality = cardinality;
        this.type = type;
        this.sponsorKey = sponsorKey;
        this.customerLocationCode = customerLocationCode;
        this.sponsorCode = sponsorCode;
    }

    public String getRendition() {
        return rendition;
    }

    public void setRendition(String rendition) {
        this.rendition = rendition;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public Double getLocationKey() {
        return locationKey;
    }

    public int getCardinality() {
        return cardinality;
    }

    public Type getType() {
        return type;
    }

    public Integer getSponsorKey() {
        return sponsorKey;
    }

    public Double getProjectedLatitude() {
        return projectedLatitude;
    }

    public void setProjectedLatitude(Double projectedLatitude) {
        this.projectedLatitude = projectedLatitude;
    }

    public Double getProjectedLongitude() {
        return projectedLongitude;
    }

    public void setProjectedLongitude(Double projectedLongitude) {
        this.projectedLongitude = projectedLongitude;
    }

    public boolean isProjected() {
        return this.getType() == projected;
    }

    public String getCustomerLocationCode() {
        return customerLocationCode;
    }

    public String getSponsorCode() {
        return sponsorCode;
    }

}
