
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.report;

import com.korem.openlayers.parameters.IApplyParameters;
import com.korem.requestHelpers.RequestParser;
import com.lo.ContextParams;
import com.lo.db.dao.PDFReportDAO;
import com.lo.web.Apply.IProgressAware;
import com.lo.web.Apply.ProgressListener;
import com.spinn3r.log5j.Logger;
import javax.servlet.http.HttpSession;
import com.lo.hotspot.HotSpotFactory;
import com.lo.hotspot.HotSpotMethod;
import com.lo.pdf.PDFBean;
import com.lo.pdf.PDFGenerator;
import com.lo.util.LoggingUtil;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONArray;

/**
 *
 * @author ydumais
 */
public class ReportMethod implements IProgressAware  {

    private static final int INDEX_PARAMS = 0;
    private static final int INDEX_FACTORY = 1;
    private static final int INDEX_SESSION = 2;
    private static final int INDEX_BASE_HREF = 3;
    private static final Logger log = Logger.getLogger();
    private PDFBean pdfBean;

    /**
     * @return the pdfBean
     */
    public PDFBean getPdfBean() {
        return pdfBean;
    }

    public static interface IParams extends IApplyParameters, HotSpotMethod.IParams {
        //agilbert, iparams should extends all other iparams needed instead of redefining everything.. that sucks... eventually I will clean that...
        
        boolean isTaTextHidden();

        String methods();

        String report();

        String tradearea();

        String taLayerId();

        Double issuance();

        Double distance();

        String projected();

        Double longitude();

        Double latitude();

        String polygon();
        
        String hotspot();

        //String density();

        String nwatch();

        String centerX();

        String centerY();

        String zoom();

        Double xmin();

        Double ymin();

        Double xmax();

        Double ymax();

        String locationKeys();

        String layers();

        String googleType();

        Integer width();

        Integer height();

        String formatTo();

        String formatFrom();

        String themeLayerIds();

        Double opacity();
        
        JSONArray rollupCodesFilters();
        
        String slaTransactionValue();
    }

    @Override
    public Object parseRequest(HttpServletRequest request) {
        IParams params = RequestParser.persistentParse(request, IParams.class);
        String baseHREF = com.lo.Config.getBaseUrl(request);
        return new Object[]{params, HotSpotMethod.getHotSpotFactory(request.getSession(), params), request.getSession(), baseHREF};
    }

    @Override
    public void execute(ProgressListener listener, Object paramsObj) {

        Object[] paramAsArray = (Object[]) paramsObj;
        IParams params = (IParams) paramAsArray[INDEX_PARAMS];
        HttpSession session = (HttpSession) paramAsArray[INDEX_SESSION];
        ContextParams cp = ContextParams.get(session);

        if ("report".equals(params.methods())) {
            try {
                log.debug("report " + params.report());

                HotSpotFactory factory = (HotSpotFactory) paramAsArray[INDEX_FACTORY];
                String baseHREF = (String) paramAsArray[INDEX_BASE_HREF];

                PDFGenerator pGen = new PDFGenerator(factory, listener, session, params, baseHREF);
                pGen.run();
                pdfBean = pGen.getPdf();

                LoggingUtil.log(cp.getUser(), cp.getSponsor(), LoggingUtil.getReportMessage(params));
            } finally {
                listener.update(100);
            }
        }
    }
}
