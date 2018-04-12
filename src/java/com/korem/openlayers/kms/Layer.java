package com.korem.openlayers.kms;

import com.korem.XMLHelper;
import com.lo.db.proxy.LayerGroupProxy.LayerGroupDTO;
import com.mapinfo.midev.service.namedresource.v1.NamedResource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.xml.xpath.XPathExpressionException;
import net.sf.json.util.JSONBuilder;
import org.w3c.dom.Element;

/**
 *
 * @author jduchesne
 */
public class Layer {

    public static final String[] ACCEPTED_SUFFIX = new String[]{
        "_CUSTOMLAYER", "_CL"
    };

    private static final String A_ID = "id";
    private static final String A_NAME = "name";
    private static final String A_ISTHEME = "isTheme";
    private static final String A_VISIBILITY = "visibility";
    private static final String A_ZOOM_VISIBILITY = "zoomVisibility";
    private static final String A_IS_FSA = "isFSA";
    private static final String A_SPONSOR = "sponsor";
    private static final String A_GROUP_ID = "groupId";
    private static final String A_GROUP_IS_OTHER = "isOther";
    private static final String A_GROUP_NAME = "groupName";
    private static final String A_GROUP_ORDER = "groupOrder";
    private static final String A_LAYER_NAME = "layerName";
    private static final String A_LAYER_ORDER = "layerOrder";
    private static final String A_PARENT = "parent";
    private static final String A_LABEL_DISPLAYS = "labelDisplays";
    private static final String A_LABEL_FIELDS = "labelFields";
    
    private static final String XPATH_VISIBILITY = ".//Visibility/text()";
    private static final String XPATH_IS_ZOOM_ACTIVE = ".//ZoomConstraints/@isActive";
    private static final String XPATH_MINZOOM = ".//MinZoom/text()";
    private static final String XPATH_MAXZOOM = ".//MaxZoom/text()";

    private String id;
    private String name;
    private String path;
    private boolean isTheme;
    private boolean visibility;
    private boolean zoomVisibility;
    private  String parent;
    private List<String> labelDisplays = new ArrayList<>();
    private List<String> labelFields = new ArrayList<>();


    Layer(Element layer, double zoomLevel) throws Exception {
        id = layer.getAttribute(A_ID);
        name = layer.getAttribute(A_NAME);
        parent = XMLHelper.get().getString(layer, ".//ThematicInfo/@parent");
        layer.getAttributes();
        isTheme = Boolean.parseBoolean(layer.getAttribute(A_ISTHEME));
        visibility = Boolean.parseBoolean(XMLHelper.get().getString(layer, XPATH_VISIBILITY));
        setZoomVisibility(layer, zoomLevel);
    }
    
    Layer(NamedResource layer) throws Exception {
        id = layer.toString();
        path = layer.getPath();
        name = path.substring(path.lastIndexOf('/') + 1).trim();
        
        parent = "0";
        visibility = false;
        isTheme = false;
        //layer.toString();
        //setZoomVisibility((Element) layer, zoomLevel);
    }

    private void setZoomVisibility(Element layer, double zoomLevel) throws XPathExpressionException {
        zoomVisibility = true;
        if (Boolean.parseBoolean(XMLHelper.get().getString(layer, XPATH_IS_ZOOM_ACTIVE))) {
            double minZoom = Double.parseDouble(XMLHelper.get().getString(layer, XPATH_MINZOOM));
            double maxZoom = Double.parseDouble(XMLHelper.get().getString(layer, XPATH_MAXZOOM));

            zoomVisibility = zoomLevel >= minZoom && zoomLevel <= maxZoom;
        }
    }

    void setVisibilityBasedOnLabel(Element layer) throws XPathExpressionException {
        visibility = false;
    }

    public String getId() {
        return id;
    }
    
    public String getPath() {
        return path;
    }
    
    public Boolean getVisibility() {
        return visibility;
    }
    
    public void setVisibility(Boolean visible) {
        this.visibility = visible;
    }

    public String getName() {
        return name;
    }

    public void appendJSON(JSONBuilder builder, LayerGroupDTO lg) {
        String displayName = getDisplayName();
        builder.object().
                key(A_ID).value(getId()).
                key(A_NAME).value(displayName).
                key(A_ISTHEME).value(isTheme).
                key(A_VISIBILITY).value(visibility).
                key(A_ZOOM_VISIBILITY).value(zoomVisibility).
                key(A_IS_FSA).value("CN06DA".equals(displayName)).
                key(A_PARENT).value(parent).
                key(A_LABEL_DISPLAYS).array();
        
        for (String elt : getLabelDisplays()) {
              builder.value(elt);
        }
        builder.endArray().key(A_LABEL_FIELDS).array();

        for (String elt : getLabelFields()) {
              builder.value(elt);
        }
        builder.endArray();
                
        
        if (lg != null){
            builder.key(A_SPONSOR).value(lg.getSponsor()).
                    key(A_GROUP_ID).value(lg.getGroupId()).
                    key(A_GROUP_IS_OTHER).value(lg.isOther()).
                    key(A_GROUP_NAME).value(lg.getGroupName()).
                    key(A_GROUP_ORDER).value(lg.getGroupOrder()).
                    key(A_LAYER_NAME).value(lg.getLayerName()).
                    key(A_LAYER_ORDER).value(lg.getLayerOrder());
        }
        builder.endObject();
    }

    private String getDisplayName() {
        //return removeSuffix(name);
        return name;
    }

    public static String removeSuffix(String name) {
        for(String suffix : ACCEPTED_SUFFIX){
            int indexOf = name.indexOf(suffix);
            if (indexOf >= 0) {
                return name.substring(0, indexOf);
            }
        }
        return name;
    }

    public boolean isTheme() {
        return isTheme;
    }

    public void appendZoomJSON(JSONBuilder builder) {
        builder.object().
                key(A_ID).value(getId()).
                key(A_ZOOM_VISIBILITY).value(zoomVisibility).
            endObject();
    }
    
    public List<String> getLabelDisplays() {
        return labelDisplays;
    }

    public void setLabelDisplays(List<String> labelDisplays) {
        this.labelDisplays = labelDisplays;
    }

    public List<String> getLabelFields() {
        return labelFields;
    }

    public void setLabelFields(List<String> labelFields) {
        this.labelFields = labelFields;
    }    
}
