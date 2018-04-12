package com.korem.openlayers;

import com.korem.openlayers.kms.Feature;
import com.korem.openlayers.kms.Layer;
import com.korem.openlayers.parameters.ILonLatSelectionParameters;
import com.korem.openlayers.parameters.IMousePositionParameters;
import com.korem.openlayers.parameters.IPixelSelectionParameters;
import com.korem.openlayers.parameters.IBoundsParameters;
import com.korem.openlayers.parameters.IPositionParameters;
import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.openlayers.parameters.IDeviceBoundsParameters;
import com.korem.openlayers.parameters.IImageParameters;
import com.korem.openlayers.parameters.IInfoWithinRegion;
import com.korem.openlayers.parameters.IInitParameters;
import com.korem.openlayers.parameters.ILayerNameParameters;
import com.korem.openlayers.parameters.ILayerVisibilityParameters;
import com.korem.openlayers.parameters.ISelectByAttributesParameters;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author jduchesne
 */
public interface IMapProvider extends Serializable {

    void init(IInitParameters parameters, HttpServletRequest request) throws Exception;

    
    String getKeyFromWorspaceName(String workspaceName) throws Exception;

    void setBounds(IBoundsParameters parameters) throws Exception;

    void writeImage(IImageParameters parameters, HttpServletResponse response) throws Exception;

    byte[] getImage(String mapInstanceKey,IBoundsParameters boundsParams,HttpServletRequest request, String format, int width, int height) throws Exception;

    Bounds getBounds(IBaseParameters parameters) throws Exception;

    void setDeviceBounds(IDeviceBoundsParameters parameters) throws Exception;

    int setSelection(ILonLatSelectionParameters parameters) throws Exception;

    int setSelection(IPixelSelectionParameters parameters, Boolean append) throws Exception;

    void removeSelection(IBaseParameters parameters) throws Exception;

    Map<String, Collection<Map<String, Object>>> getInfo(IMousePositionParameters parameters, IFilter filter) throws Exception;

    Collection<Feature> getSelection(IBaseParameters parameters) throws Exception;

    Collection<Map<String, Map<String, Object>>> getInfoWithinRegion(IInfoWithinRegion parameters,
            IInfoFilter filter) throws Exception;

    void clearSelection(IBaseParameters parameters) throws Exception;

    void setZoomAndCenter(IPositionParameters parameters) throws Exception;

    void setSelectionByAttributes(ISelectByAttributesParameters parameters) throws Exception;

    String getLayerId(ILayerNameParameters parameters) throws Exception;

    long addMarker(IPositionParameters parameters, String layerName, String label) throws Exception;

    void removeMarker(IBaseParameters parameters, long uniqueId, String layerId) throws Exception;

    void viewEntireLayer(IBaseParameters parameters) throws Exception;

    Collection<Layer> getLayers(IBaseParameters parameters, IFilter filter, HttpServletRequest request) throws Exception;

    void setLayerVisibility(ILayerVisibilityParameters params,HttpServletRequest request) throws Exception;

    boolean isLabelOnly(String layerId) throws Exception;

    void setLabelVisibility(ILayerVisibilityParameters params) throws Exception;
}
