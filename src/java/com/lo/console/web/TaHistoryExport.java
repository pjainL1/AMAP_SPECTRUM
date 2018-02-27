package com.lo.console.web;

import com.korem.requestHelpers.GenericDBBoundJSONServlet;
import com.lo.config.Confs;
import com.lo.db.dao.AirMilesDAO;
import com.lo.db.dao.LocationDAO;
import com.lo.db.proxy.GridProxy;
import com.lo.db.proxy.TaHistoryProxy;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONArray;
import com.lo.export.midmif.MidMifExport;
import com.lo.export.midmif.TabZipper;
import com.lo.util.GridParamsUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.servlet.annotation.WebServlet;

/**
 *
 * @author rarif
 */
@WebServlet("/console/TaHistoryExport.safe")
public class TaHistoryExport extends GenericDBBoundJSONServlet<TaHistoryProxy, TaHistoryExport.Params> {
    private static final String INSIDE_ZIP_BASE_NAME = "TradeAreas";
    
    public interface Params {
        String search();
        JSONArray filters();
        String property();
        String direction();
        JSONArray dateFilters();
    }
    
    @Override
    protected String getJSON(HttpServletRequest request, TaHistoryProxy proxy, Params params) throws Exception {
        LocationDAO locationDAO = new LocationDAO(new AirMilesDAO());

        String search = params.search();
        String property = params.property();
        String direction = params.direction();
        
        List<GridProxy.Filter> filtersValuesList = new ArrayList<>();
        GridParamsUtils.setFilterParams(params.filters(), filtersValuesList);
        GridParamsUtils.setFilterParams(params.dateFilters(), filtersValuesList);

        MidMifExport midMifExport = new MidMifExport(proxy, locationDAO, property, direction, search, filtersValuesList);
        midMifExport.setLocationDAO(locationDAO);
        String filesPath = Confs.CONFIG.consoleDownloadPathMidmif();
        String fileName = UUID.randomUUID().toString();

        File midFile = new File(filesPath, fileName + ".mid");
        File mifFile = new File(filesPath, fileName + ".mif");
        File abortFile = new File(filesPath, fileName + ".abort");
        File zippedFile = File.createTempFile("zipped", ".zip", new File(filesPath));

        int numberOfRecord = midMifExport.generate(mifFile, midFile, abortFile);
        System.out.println("records to download = " + numberOfRecord);

        try (FileOutputStream fos = new FileOutputStream(zippedFile)) {
            TabZipper zipper = new TabZipper(midFile, INSIDE_ZIP_BASE_NAME);
            zipper.writeZippedOutput(fos);
        }
        request.getSession().setAttribute(Confs.STATIC_CONFIG.tradeareaMidmifDownloadSessionAttrPath(), zippedFile.getAbsolutePath());
        midFile.delete();
        midFile.delete();
        abortFile.delete();
        return SUCCESS;

    }
}
