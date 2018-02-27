package com.korem.spectrum;

import com.pb.spectrum.services.gettravelboundary.*;
import com.pb.spectrum.services.gettravelboundary.OutputPortIsoRouteResponse.UserFields;
import com.korem.spectrum.exceptions.SpectrumException;
import com.mapinfo.midev.service.geometries.v1.*;
import com.spinn3r.log5j.Logger;
import java.util.*;

/**
 * Interface for the web services exposed by the Location Intelligence Module of
 * the Spectrum platform.
 *
 * @author Korem
 */
public class SpectrumHelper {

    private static final Logger log = Logger.getLogger();
    private static final int FIRST = 0;
    private static final String FAILURE = "F";
    public static final int ID = 0;
    public static final int DISTANCE = 1;
    public static final int TIME = 2;
    
    private static UserField getSpecificField(List<UserField> fields, String name) {
        for (UserField field : fields) {
            if (field != null && name.equals(field.getName())) {
                return field;
            }
        }
        
        log.error(String.format("Field name %s was not found in Route response.", name));
        return null;
    }

    public static Map<String, List<double[]>> getCoordinatesFromIsoRoute(GetTravelBoundaryResponse response) throws SpectrumException {
        List<OutputPortIsoRouteResponse> routeResponses = extractResponse(response);
        Map<String, List<double[]>> coordinates = new HashMap<String, List<double[]>>();
        for (OutputPortIsoRouteResponse route : routeResponses) {
            // user-defined name-value param to keep track of site
            UserFields fields = route.getUserFields();
            com.pb.spectrum.services.gettravelboundary.UserField field = getSpecificField(fields.getUserField(), SpectrumServiceSoap.ID_FIELD);
            List<double[]> points = getCoordinatesFromIsoRoute(route);
            if (!points.isEmpty()) {
                coordinates.put(field.getValue(), points);
            }
        }
        return coordinates;

    }

    public static List<double[]> getCoordinatesFromIsoRoute(OutputPortIsoRouteResponse route) throws SpectrumException {
        List<double[]> coordinates = new ArrayList<double[]>();
        if (route.getStatus() != null) {
            log.error("Route status warning: {} ", route.getStatusDescription());
            return coordinates;
//            throw new SpectrumException(route.getStatusCode() + " : " + route.getStatusDescription());
        }
        Geometry geometry = route.getIsoPolygonResponse();
        List<Polygon> polygons = ((MultiPolygon) geometry).getPolygon();
        Polygon polygon = polygons.get(FIRST);
        List<LineString> lines = polygon.getExterior().getLineString();
        LineString line = lines.get(FIRST);
        List<Pos> positions = line.getPos();
        log.debug("# of polygons = " + polygons.size() + " # of nodes = " + positions.size());
        for (Pos position : positions) {
            double[] point = {position.getX(), position.getY()};
            coordinates.add(point);
        }
        return coordinates;
    }

    protected static List<OutputPortIsoRouteResponse> extractResponse(GetTravelBoundaryResponse response) throws SpectrumException {
        if (response == null) {
            throw new SpectrumException("TravelBoundary response is null!");
        }
        List<OutputPortIsoRouteResponse> routeResponses = response.getOutputPort().getIsoRouteResponse();
        log.debug("# of route(s): " + routeResponses.size());
        if (routeResponses.isEmpty()) {
//            throw new SpectrumException("Response contains no route information!");
            log.error("Response contains no route information!");
        }
        return routeResponses;
    }

}

