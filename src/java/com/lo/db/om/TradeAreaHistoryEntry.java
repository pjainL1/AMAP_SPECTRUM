package com.lo.db.om;

import com.vividsolutions.jts.geom.Geometry;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

/**
 *
 * @author rarif
 */
public class TradeAreaHistoryEntry {
    private static final String JSON_DATE_FORMAT = "YYYY-MM-dd HH:mm";
    public static final JsonConfig JSON_CONFIG = new JsonConfig();
    static {
        JSON_CONFIG.registerJsonValueProcessor(TradeAreaHistoryEntry.class, "creaDate", new JsonValueProcessor() {

            @Override
            public String processArrayValue(Object o, JsonConfig jc) {
                return TradeAreaHistoryEntry.toJsonDate((Date)o);
            }

            @Override
            public String processObjectValue(String string, Object o, JsonConfig jc) {
                return TradeAreaHistoryEntry.toJsonDate((Date)o);
            }
        });
    }

    private int id;
    private String sponsorLocationCode;
    private String type;
    private String typeDetail;
    private int sponsorLocationKey;
    private String style;
    private Geometry geom;
    private Date creaDate;
    private Date toDate;
    private Date fromDate;
    private String userLogin;
    private String rollupName;

    public TradeAreaHistoryEntry(String sponsorLocationCode,int id, Date creationDate, String type, String typeDetail, Date from, Date to, int sponsorLocationKey, String style, Geometry geom, String userLogin, String rollupName) {
        this.sponsorLocationCode = sponsorLocationCode;
        this.id = id;
        this.creaDate = creationDate;
        this.type = type;
        this.typeDetail = typeDetail;
        this.fromDate = from;
        this.toDate = to;
        this.sponsorLocationKey = sponsorLocationKey;
        this.style = style;
        this.geom = geom;
        this.userLogin = userLogin;
        this.rollupName = rollupName;
    }

    public String getSponsorLocationCode() {
        return sponsorLocationCode;
    }

    public void setSponsorLocationCode(String sponsorLocationCode) {
        this.sponsorLocationCode = sponsorLocationCode;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeDetail() {
        return typeDetail;
    }

    public void setTypeDetail(String typeDetail) {
        this.typeDetail = typeDetail;
    }

    public int getSponsorLocationKey() {
        return sponsorLocationKey;
    }

    public void setSponsorLocationKey(int sponsorLocation) {
        this.sponsorLocationKey = sponsorLocation;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public Geometry getGeom() {
        return geom;
    }

    public void setGeom(Geometry geom) {
        this.geom = geom;
    }

    public Date getCreaDate() {
        return creaDate;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public String getRollupName() {
        return rollupName;
    }

    @Override
    public String toString() {
        return "TradeAreaHistory{" + "id=" + id + ", creationDate=" + creaDate + ", type=" + type + ", typeDetail=" + typeDetail + ", from=" + fromDate + ", to=" + toDate + ", sponsorLocation=" + sponsorLocationKey + ", style=" + style + ", geom=" + geom + '}';
    }
    
    private static String toJsonDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat(JSON_DATE_FORMAT);
        
        return dateFormat.format(date);
    }
}
