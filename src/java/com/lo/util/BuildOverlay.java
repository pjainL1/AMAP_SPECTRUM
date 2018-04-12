package com.lo.util;

import com.mapinfo.midev.service.geometries.v1.Geometry;
import com.mapinfo.midev.service.geometries.v1.GeometryList;
import com.mapinfo.midev.service.mapping.v1.GeometryLayer;
import com.mapinfo.midev.service.mapping.v1.Layer;
import com.mapinfo.midev.service.mapping.v1.NamedLayer;
import com.mapinfo.midev.service.style.v1.NamedStyle;

public class BuildOverlay {
    public static GeometryLayer createGeometryLayer() throws Exception {
        GeometryLayer gl = new GeometryLayer();
        Geometry gt = BuildGeometry.buildPolygon();
        GeometryList geometrylist = new GeometryList();
        geometrylist.getGeometry().add(gt);
        gl.setGeometryList(geometrylist);
        return gl;
    }

    public static Layer[] createGeometryOverlay() throws Exception {
        GeometryLayer gl = createGeometryLayer();
        return new Layer[] { gl };
    }

    public static Layer[] createGeometryOverlaywithOverrideTheme() throws Exception {
        GeometryLayer gl = createGeometryLayer();
        NamedStyle ns = new NamedStyle();
        ns.setName("SolidFillBlue");
        // set the named style for the override theme.
        gl.setStyle(ns);

        return new Layer[] { gl };
    }

    public static Layer[] createNamedLayerOverlay() {
        NamedLayer nl = new NamedLayer();
        nl.setName("MyLayer");
        return new Layer[] { nl };
    }
}
