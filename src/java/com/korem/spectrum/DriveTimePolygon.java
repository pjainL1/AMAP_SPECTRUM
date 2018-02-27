package com.korem.spectrum;

import com.korem.spectrum.exceptions.SpectrumException;
import com.lo.Config;
import com.spinn3r.log5j.Logger;
import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author slajoie
 */
public class DriveTimePolygon {
    public static final int SRID = 8307;
    public static final String SRS = "EPSG:4326";
    private static final String UNIT = "Meters";

    private static final Logger log = Logger.getLogger();
    private Double longitude;
    private Double latitude;
    private Double[] driveDistances;
    private BoundaryInput[] boundaryInputs;
    private GeometryFactory factory;
    private SpectrumServiceSoap spectrumServiceSoap = new SpectrumServiceSoap(
            Config.getInstance().getSpectrumBaseUrl(), 
            Config.getInstance().getSpectrumUsername(), 
            Config.getInstance().getSpectrumPassword());

    public DriveTimePolygon(Double longitude, Double latitude, Double[] driveDistances) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.driveDistances = driveDistances;
        init();
    }

    public DriveTimePolygon(Double longitude, Double latitude, Double driveDistance) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.driveDistances = new Double[] {driveDistance};
        init();
    }

    private void init() {
        PrecisionModel model = new PrecisionModel(PrecisionModel.FLOATING);
        factory = new GeometryFactory(model, SRID);
        this.setBoundaryInputs();
    }
    
    private void setBoundaryInputs() {
        BoundaryInput[] inputs = new BoundaryInput[driveDistances.length];
        int i = 0;
        for (double distance : driveDistances) {
            long meters = Math.round(distance * 1000);
            inputs[i++] = new BoundaryInput(longitude, latitude, "" + meters);
        }
        
        this.boundaryInputs = inputs;
    }

    public List<Geometry> getDriveTimePolygons() {
        List<Geometry> geoms = new ArrayList<Geometry>();
        Map<String, List<double[]>> coordinates;
        try {
            coordinates = spectrumServiceSoap.getTravelBoundary(boundaryInputs, UNIT, SRS);
            geoms.addAll(processResponse(coordinates));
        } catch (SpectrumException ex) {
            log.error("Error:" + ex.getMessage());
        }
        catch (Exception ex) {
            ex.printStackTrace();
            log.error("Error:" + ex.getMessage());
        }

        return geoms;
    }

    private List<Geometry> processResponse(Map<String, List<double[]>> coordinateResults) throws Exception {
        List<Geometry> geoms = new ArrayList<Geometry>();
        
        for (int i = 0; i < boundaryInputs.length; i++) {
            geoms.add(getJtsPolygon(coordinateResults.get("" + i)));
        }
        
        return geoms;
    }
    
    private Geometry getJtsPolygon(List<double[]> srcCoordinates) throws Exception {
        Coordinate[] coords = new Coordinate[srcCoordinates.size()];
        int i = 0;
        for (double[] lngLat : srcCoordinates) {
            double lng = lngLat[0];
            double lat = lngLat[1];
            coords[i++] = new Coordinate(lng, lat, 0);
        }
        LinearRing shell = factory.createLinearRing(coords);
        Polygon polygon = factory.createPolygon(shell, null);
        
        ConvexHull ch = new ConvexHull(polygon);
        return ch.getConvexHull();
    }
}
