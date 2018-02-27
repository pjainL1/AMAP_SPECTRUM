package com.korem.heatmaps;

import com.lo.util.Formatter;
import java.awt.Color;
import net.sf.json.util.JSONBuilder;

/**
 *
 * @author jduchesne
 */
public class LegendItem {

    public static enum Format {
        none,
        currency
    }

    private Color color;
    private String min;
    private String max;
    private Format format;

    LegendItem(Color color, int min, int max, Format format) {
        this.format = format;
        this.color = color;
        setMin(min);
        setMax(max);
    }

    private String format(int value) {
        Integer roundedValue = round(value);
        Formatter formatter = new Formatter();
        if (format == Format.currency) {
            return formatter.getCurrencyNumberFormat().format(roundedValue);
        } else {
            return formatter.getNumberNumberFormat().format(roundedValue);
        }
    }



    private Integer round(Integer number) {
        String str = Integer.toString(Math.abs(number.intValue()));
        int significative = Math.max(str.length() - 2, 0);
        return (int)((int)(number / Math.pow(10, significative)) * Math.pow(10, significative));
    }

    void appendTo(JSONBuilder json) {
        json.object().
                key("color").value(Integer.toHexString(color.getRGB()).substring(2)).
                key("min").value(min).
                key("max").value(max).
                endObject();
    }

    public Color getColor() {
        return color;
    }

    void setMin(int min) {
        this.min = format(min);
    }

    void setMax(int max) {
        this.max = format(max);
    }

    public String getMin() {
        return min;
    }

    public final String getMax() {
        return max;
    }
}
