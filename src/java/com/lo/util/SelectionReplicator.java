/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.util;

import com.korem.openlayers.IMapProvider;
import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.openlayers.kms.Feature;
import com.korem.openlayers.parameters.ISelectByAttributesParameters;
import com.lo.ContextParams;
import com.lo.analysis.Analysis;
import com.spinn3r.log5j.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;

/**
 *
 * @author YDumais
 */
public class SelectionReplicator {

    private static final Logger log = Logger.getLogger();
    private ContextParams contextParams;

    public SelectionReplicator(ContextParams contextParams) {
        this.contextParams = contextParams;
    }

    public String createResult(IMapProvider mapProvider, IBaseParameters params) throws Exception {
        //List<String> selectionPKs = new ArrayList<String>();
        
        List<String> selectionPKs = Arrays.asList("860.0");
           JSONBuilder json = new JSONStringer().array();
        for (Feature feature : mapProvider.getSelection(params)) {
            feature.appendJSON(json);
            //selectionPKs.add(feature.getPK());
        }
        save(selectionPKs);
        return json.endArray().toString();
    }

    public void reapply(IMapProvider mapProvider, IBaseParameters params) throws Exception {
        List<String> selectionPKs = contextParams.getSelectionPKs();
        if (selectionPKs != null) {
            log.debug("reapplying previous selection on mapInstance: " + params.mapInstanceKey());
            mapProvider.setSelectionByAttributes(toISelectByAttributesParameters(params.mapInstanceKey(), selectionPKs));
        } else {
            mapProvider.clearSelection(params);
        }
    }

    public void clear() {
        contextParams.setSelectionPKs(null);
    }

    private void save(List<String> selectionPKs) {
        contextParams.setSelectionPKs(selectionPKs);
    }
    
    private List<Double> toDoubleList(List<String> selection) {
        List<Double> selectionAsDoubles = new ArrayList<>(selection.size());
        for(String s : selection) {
            selectionAsDoubles.add(Double.valueOf(s));
        }
        return selectionAsDoubles;
    }

    private ISelectByAttributesParameters toISelectByAttributesParameters(final String mapInstanceKey,
            final List<String> selectionPKs) {
        
        final List<Double> doubleList = toDoubleList(selectionPKs);
        
        return new ISelectByAttributesParameters() {

            @Override
            public String[] getAttributeColumns() {
                return new String[]{"SPONSOR_LOCATION_KEY"};
            }

            @Override
            public Double[] getAttributeValues() {
                return doubleList.toArray(new Double[]{});
            }

            @Override
            public String getLayerName() {
                return Analysis.LOCATIONS.toString();
            }

            @Override
            public String mapInstanceKey() {
                return mapInstanceKey;
            }
        };
    }
}
