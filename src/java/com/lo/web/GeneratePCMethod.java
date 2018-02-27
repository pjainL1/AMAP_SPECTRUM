package com.lo.web;

import au.com.bytecode.opencsv.CSVWriter;
import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.requestHelpers.RequestParser;
import com.lo.ContextParams;
import com.lo.analysis.Analysis;
import com.lo.analysis.tradearea.TradeArea;
import com.lo.config.Confs;
import com.lo.db.proxy.PostalCodeProxy;
import com.lo.util.WSClient;
import com.lo.web.Apply.IProgressAware;
import com.lo.web.Apply.ProgressListener;
import com.spinn3r.log5j.Logger;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * This class is about to generate a csv and save it on disk ready for download.
 *
 * @author Charles St-Hilaire for Korem inc.
 */
public class GeneratePCMethod implements IProgressAware {

    private static final Logger LOGGER = Logger.getLogger();

    public interface IParams extends IBaseParameters {

        String locations();
    }

    @Override
    public Object parseRequest(HttpServletRequest request) {
        GeneratePCMethod.IParams params = RequestParser.persistentParse(request, GeneratePCMethod.IParams.class);
        return new Object[]{params, request.getSession(), request.getServletContext(), ContextParams.get(request.getSession())};
    }

    @Override
    public void execute(ProgressListener listener, Object paramsObj) {
        Object[] objects = ((Object[]) paramsObj);
        GeneratePCMethod.IParams params = (GeneratePCMethod.IParams) objects[0];
        HttpSession hs = (HttpSession) objects[1];
        ServletContext sc = (ServletContext) objects[2];
        ContextParams contextParams = (ContextParams) objects[3];
        Map<Double, List<TradeArea>> tradeAreas = contextParams.getTradeAreas(); // per location
        Map<Double, List<String>> postalCodesSeen = new HashMap<>(); // per location

        try (PostalCodeProxy proxy = new PostalCodeProxy(contextParams.getSponsor())) {
            if (hs != null && sc != null && proxy != null && params != null) {
                String mik = params.mapInstanceKey();
                if (mik != null) {
                    List<String[]> info = new ArrayList<>();
                    String layerIDs[];
                    try {
                        layerIDs = WSClient.getMapService().getLayersIdByName(mik, Analysis.TRADE_AREA.toString());
                    } catch (RemoteException re) {
                        layerIDs = null;
                    }
                    if (layerIDs != null && layerIDs.length > 0) {
                        String locations = params.locations();
                        if (locations != null && locations.trim().length() > 0) {
                            locations = locations.endsWith(",") ? locations.substring(0, locations.length() - 1) : locations;
                            String[] someLocations = locations.split(",");
                            if (someLocations != null && someLocations.length > 0) {
                                double progress = 0;
                                double step = 90f / someLocations.length;
                                for (String location : someLocations) {
                                    Double locationKey = Double.parseDouble(location);
                                    List<TradeArea> locationTradeAreas = tradeAreas.get(locationKey);
                                    try {
                                        if (locationTradeAreas != null) {
                                            for (TradeArea ta : locationTradeAreas) {
                                                info.addAll(proxy.getPostalCodeDistance(locationKey, ta.getGeometry(), postalCodesSeen));
                                            }
                                        }
                                    } catch (Exception ex) {
                                        LOGGER.error("Error generating postal codes export.", ex);
                                    } finally {
                                        listener.update(progress += step);
                                    }
                                }
                            }
                        }

                        // add "null" locations (TAs without any location, could be custom or projected)
                        List<TradeArea> nullLocationTradeAreas = tradeAreas.get(null);
                        try {
                            if (nullLocationTradeAreas != null) {
                                for (TradeArea ta : nullLocationTradeAreas) {
                                    info.addAll(proxy.getPostalCodeOnlyOrProjected(ta, ta.getGeometry(), postalCodesSeen));
                                }
                            }
                        } finally {
                            listener.update(95);
                        }
                    }

                    String folderPath = Confs.CONFIG.tradeareaCsvDownloadFolderPath();
                    File folder = new File(folderPath);
                    if (!folder.exists()) {
                        LOGGER.error(folderPath + " doesn't exist, please review the property: 'tradearea.csv.download.folder.path' in config.properties file.");
                    }
                    if (folder.canWrite() && folder.canRead()) {
                        String fullPath = folderPath + File.separator + Confs.STATIC_CONFIG.tradeareaCsvDownloadFilePrefix() + System.currentTimeMillis() + ".csv";
                        File csv = new File(fullPath);
                        try {
                            if (csv.createNewFile() && csv.canWrite()) {
                                try (CSVWriter cw = new CSVWriter(new BufferedWriter(new FileWriter(fullPath)))) {
                                    cw.writeNext(new String[]{"POSTAL_CODE", "LOCATION_ID", "DISTANCE"});
                                    cw.writeAll(info);
                                    listener.update(98);
                                } finally {
                                    hs.setAttribute(Confs.STATIC_CONFIG.tradeareaCsvDownloadSessionAttrPath(), fullPath);
                                }
                            }
                        } catch (IOException err) {
                            LOGGER.error(String.format("Unable to performed behavior for the specified file of: \"%s\", with message: \"%s\"", fullPath, err.getMessage()));
                        }
                    } else {
                        LOGGER.error("CSV Download folder doesn't have R/W rights.");
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error(String.format("An error occurs: %s", e.getMessage()));
        } finally {
            listener.update(100);
        }
    }
}
