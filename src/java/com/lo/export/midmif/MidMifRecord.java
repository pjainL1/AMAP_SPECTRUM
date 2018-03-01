package com.lo.export.midmif;

import com.vividsolutions.jts.geom.Geometry;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author agilbert
 */
public class MidMifRecord {
    private String style;
    private List<String> styles = new ArrayList<>();
    private List<Object> descriptiveValues = new ArrayList<>();
    private Geometry geometry = null;

    public List<Object> getDescriptiveValues() {
        return descriptiveValues;
    }

    public void setDescriptiveValues(List<Object> descriptiveValues) {
        this.descriptiveValues = descriptiveValues;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public List<String> getStyles() {
        return styles;
    }

    public void setStyles(List<String> styles) {
        this.styles = styles;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }
    
    
}
