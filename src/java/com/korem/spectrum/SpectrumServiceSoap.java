package com.korem.spectrum;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import com.pb.spectrum.GetTravelBoundary;
import com.pb.spectrum.GetTravelBoundary_Service;
import com.pb.spectrum.services.gettravelboundary.CoordinateFormat;
import com.pb.spectrum.services.gettravelboundary.GetTravelBoundaryRequest;
import com.pb.spectrum.services.gettravelboundary.GetTravelBoundaryResponse;
import com.pb.spectrum.services.gettravelboundary.MajorRoads;
import com.pb.spectrum.services.gettravelboundary.Options;
import com.pb.spectrum.services.gettravelboundary.ResultType;
import com.pb.spectrum.services.gettravelboundary.UserField;
import com.korem.spectrum.exceptions.SpectrumException;
import com.lo.config.Confs;
import com.pb.spectrum.ServiceInvocationFault_Exception;
import com.pb.spectrum.services.gettravelboundary.InputPortIsoRouteRequest;
import com.pb.spectrum.services.gettravelboundary.InputPortIsoRouteRequest.UserFields;
import com.pb.spectrum.services.gettravelboundary.InputPortIsoRouteRequestList;
import com.spinn3r.log5j.Logger;
import java.net.MalformedURLException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

/**
 *
 * Provides access to the web services exposed by the Location Intelligence
 *
 * Module of the Spectrum platform.
 *
 *
 *
 * Find out areas of equal travel time or driving distance.
 *
 *
 *
 * See SpectrumServiceSoapTest.java for basic usage.
 *
 *
 *
 * @author Korem
 */
public class SpectrumServiceSoap {

    private static final Logger log = Logger.getLogger();

    private static GetTravelBoundary_Service boundaryService;

    private static final String G1_NAMESPACE = "http://spectrum.pb.com/"; // Group
    // One
    // Namespace

    private static final String GET_TRAVEL_BOUNDARY = "GetTravelBoundary";

    private static final QName qNameBoundary = new QName(G1_NAMESPACE,
            GET_TRAVEL_BOUNDARY);

    private static final String WSDL = "?wsdl";

    private static final String EPSG_4326 = "EPSG:4326";

    public static final String ID_FIELD = "ID";

    private static final String COST_FIELD = "COST";

    private static final String DATASET_NAME = "America-Driving";

    private String host;

    private String user;

    private String password;

    public SpectrumServiceSoap(String host, String user, String password) {

        this.host = host;

        this.user = user;

        this.password = password;

        init();

    }

    public void init() {

        URL wsdl = null;

        try {

            wsdl = new URL(host + GET_TRAVEL_BOUNDARY + WSDL);
            boundaryService = new GetTravelBoundary_Service(wsdl, qNameBoundary);
            
            

        } catch (Exception e) {
            log.error("Error creating web services client from " + wsdl, e);
            throw new WebServiceException("Invalid server host '" + host
                    + "' or wsdl: " + wsdl);

        }

    }
    
    private void setTimeouts(GetTravelBoundary port) {
        Map<String, Object> requestContext = ((BindingProvider)port).getRequestContext();
        requestContext.put("com.sun.xml.internal.ws.connect.timeout", Confs.CONFIG.wsSpectrumTimeout());
        requestContext.put("com.sun.xml.internal.ws.request.timeout", Confs.CONFIG.wsSpectrumTimeout());
        requestContext.put("com.sun.xml.ws.request.timeout", Confs.CONFIG.wsSpectrumTimeout());
        requestContext.put("com.sun.xml.ws.connect.timeout", Confs.CONFIG.wsSpectrumTimeout());
    }

    public Map<String, List<double[]>> getTravelBoundary(Double longitude,
            Double latitude, String cost, String unit)
            throws SpectrumException, MalformedURLException, ServiceInvocationFault_Exception {

        // use EPSG_4326 as default SRS...
        return getTravelBoundary(new BoundaryInput(longitude, latitude, cost),
                unit, EPSG_4326);

    }

    public Map<String, List<double[]>> getTravelBoundary(BoundaryInput input,
            String unit, String coordSys)
            throws SpectrumException, MalformedURLException, ServiceInvocationFault_Exception {

        return getTravelBoundary(new BoundaryInput[]{input}, unit, coordSys);

    }

    private InputPortIsoRouteRequest buildRouteRequest(BoundaryInput input, String unit,
            int index) {

        InputPortIsoRouteRequest iso = new InputPortIsoRouteRequest();
        iso.setLongitude(String.valueOf(input.getLongitude()));
        iso.setLatitude(String.valueOf(input.getLatitude()));
        iso.setTravelBoundaryCost(input.getCost());
        iso.setTravelBoundaryCostUnits(unit);

        // user-defined name-value param to keep track of site
        UserFields fields = new UserFields();

        UserField idField = new UserField();
        idField.setName(ID_FIELD);
        idField.setValue("" + index);
        fields.getUserField().add(idField);
        UserField costField = new UserField();
        costField.setName(COST_FIELD);
        costField.setValue(input.getCost());
        fields.getUserField().add(costField);
        iso.setUserFields(fields);

        return iso;

    }

    public Map<String, List<double[]>> getTravelBoundary(
            BoundaryInput[] inputs, String unit, String coordSys)
            throws SpectrumException, MalformedURLException, ServiceInvocationFault_Exception {

        long start = System.currentTimeMillis();
        
        log.debug("Preparing travel boudary request...");

        GetTravelBoundaryRequest request = new GetTravelBoundaryRequest();
        InputPortIsoRouteRequestList rows = new InputPortIsoRouteRequestList();
        
        int i = 0;
        for (BoundaryInput input : inputs) {
            rows.getIsoRouteRequest().add(buildRouteRequest(input, unit, i++));
        }

        if (rows.getIsoRouteRequest().isEmpty()) {
            log.warn("No valid site requested!");
            return new HashMap<String, List<double[]>>();

        }

        Options opts = new Options();
        opts.setDataSetResourceName(DATASET_NAME);
        opts.setCoordinateSystem(coordSys);
        opts.setCoordinateFormat(CoordinateFormat.DECIMAL);
        opts.setResultType(ResultType.GEOMETRY);
        opts.setMajorRoads(MajorRoads.N);
        
        request.setInputPort(rows);
        request.setOptions(opts);

        GetTravelBoundary port = boundaryService.getGetTravelBoundaryPort();
        
        BindingProvider bp = (BindingProvider)port;
        Map<String, Object> requestContext = bp.getRequestContext();
        
        Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>();
        requestContext.put(BindingProvider.USERNAME_PROPERTY, user);
        requestContext.put(BindingProvider.PASSWORD_PROPERTY, password);
        requestContext.put(MessageContext.HTTP_REQUEST_HEADERS, requestHeaders);
        
        setTimeouts(port);
        
        log.debug("Calling getTravelBoundary...");

        GetTravelBoundaryResponse response;
        response = port.getTravelBoundary(request);

        log.info("TravelBoundary execution time "
                + (System.currentTimeMillis() - start) + "ms");

        return SpectrumHelper.getCoordinatesFromIsoRoute(response);

    }

}
