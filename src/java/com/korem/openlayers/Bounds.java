package com.korem.openlayers;

/**
 *
 * @author jduchesne
 */
public class Bounds {
    private String xmin;
    private String ymin;
    private String xmax;
    private String ymax;

    public Bounds(String xmin, String ymin, String xmax, String ymax) {
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;
    }

    public String getXMin() {
        return xmin;
    }

    public String getYMin() {
        return ymin;
    }

    public String getXMax() {
        return xmax;
    }

    public String getYMax() {
        return ymax;
    }
}
