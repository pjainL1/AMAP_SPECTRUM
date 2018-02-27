package com.korem.heatmaps;

import com.korem.heatmaps.LegendItem.Format;
import com.lo.hotspot.HotSpotType;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;

/**
 *
 * @author jduchesne
 */
public class Legend {

    private static final ResourceBundle rb = ResourceBundle.getBundle("loLocalString");
    private double precision;
    private int totalCpt;
    private LinkedList<LegendItem> items;
    private final String type;
    private final String dataType;
    private final Format format;
    private final String sponsorCodes;
    private final String dateType;
    private final String comparisonType;

    public Legend(double precision, int totalCpt, String comparisonType, String dateType, String type, String dataType, LegendItem.Format format, String sponsorCodes) {
        items = new LinkedList<>();
        this.precision = precision;
        this.totalCpt = totalCpt;
        this.type = type;
        this.dataType = dataType;
        this.format = format;
        this.sponsorCodes = sponsorCodes;
        this.dateType = dateType;
        this.comparisonType = comparisonType;
    }

    public int getTotalCpt() {
        return totalCpt;
    }

    public LegendItem add(Color color, int min, int max) {
        LegendItem item = new LegendItem(color, min, max, format);
        items.add(item);
        return item;
    }

    public LegendItem push(Color color, int min, int max) {
        LegendItem item = new LegendItem(color, min, max, format);
        items.push(item);
        return item;
    }

    @Override
    public String toString() {
        String title = rb.getString("hotspot.legend."+this.dateType+".title." + this.dataType);
        if(comparisonType!=null){
            title = String.format(title, rb.getString("hotspot.legend." + this.comparisonType));
        }
        JSONBuilder json = new JSONStringer().object().
                key("title").value(title).
                key("subtitle").value((HotSpotType.valueOf(type)==HotSpotType.sponsor ? sponsorCodes + " " : "") + rb.getString("hotspot.legend." + type)).
                key("precision").value(precision).
                key("items").array();
        for (LegendItem item : items) {
            item.appendTo(json);
        }
        return json.endArray().endObject().toString();
    }

    public double getPrecision() {
        return precision;
    }

    public List<LegendItem> getItems() {
        return items;
    }
}
