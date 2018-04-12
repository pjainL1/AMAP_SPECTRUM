package com.lo.web;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.korem.openlayers.IMapProvider;
import com.korem.openlayers.kms.Layer;
import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.openlayers.parameters.IInitParameters;
import com.korem.openlayers.parameters.ILayerVisibilityParameters;
import com.lo.ContextParams;
import com.lo.analysis.Analysis;
import com.lo.db.dao.AirMilesDAO;
import com.lo.db.dao.PDFReportDAO;
import com.lo.db.dao.SponsorDAO;
import com.lo.db.dao.UserAttributesDAO;
import com.lo.db.om.SponsorGroup;
import com.lo.db.om.PDFReport;
import com.lo.db.om.User;
import com.lo.ldap.AMAPGroup;
import com.lo.util.LoggingUtil;
import com.lo.util.SelectionReplicator;
import com.lo.util.ProgressListenerUtils;
import com.lo.web.sso.LoginActionSSO;
import com.spinn3r.log5j.Logger;

/**
 *
 * @author ydumais
 */
public class InitAction extends Action {

    private static final Logger log = Logger.getLogger();

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        SponsorGroup sponsor;
        HttpSession session = request.getSession();
        String rollupGroupCode = request.getParameter("sponsors");
        ContextParams cp = ContextParams.get(request.getSession());
        
        if (rollupGroupCode == null && request.getParameter("sponsorcode") == null) {
            sponsor = cp.getSponsor();
        } else if (request.getParameter("sponsorcode") != null) {
            SponsorDAO sdao = new SponsorDAO(new AirMilesDAO());
            sponsor = sdao.getSponsorByCode(request.getParameter("sponsorcode")
                            .toUpperCase());
        } else {
            SponsorDAO sdao = new SponsorDAO(new AirMilesDAO());
            sponsor = sdao.getSponsor(rollupGroupCode.toUpperCase());
        }
        
        if (!urlLoading(request)) {
            User u = new User();
            u.setLogin(request.getParameter("uid"));
            AMAPGroup g = new AMAPGroup(request.getParameter("role"));
            u.setRole(g.getRole());
            cp.setUser(u);

            /**
             * to push session timeout interval to client side
             */
            session.setAttribute(LoginActionSSO.SESSION_TIMEOUT,
                    session.getMaxInactiveInterval());
        }

        UserAttributesDAO userDAO = new UserAttributesDAO(new AirMilesDAO());
        cp.getUser().setAttributes(userDAO.getAttributes(request.getParameter("uid")));
        
        cp.setSponsor(sponsor);
        
        cp.set(session);

        LoggingUtil.log(cp.getUser(), sponsor, LoggingUtil.getLoginMessage());

        /**
         * Load PDFBean and ProgressListener from persistence if exist
         */
        PDFReport pr = new PDFReportDAO().find(cp);
        if (pr != null) {
            if (pr.getBean() != null) {
                cp.setPdf(pr.getBean());
            }
            if (pr.getProcessKey() != null) {
                ProgressListenerUtils.set(request, pr.getProcessKey());
                cp.setPdfProcessing(true);
            }
            request.setAttribute("pdfProcessingStatus", pr.getStatus());
        }

        if (!urlLoading(request)) {
            return mapping.findForward("default");
        } else {
            // to the sponsor url
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssS");
            String timeStamp = dateFormat.format(new Date());
            String url = request.getRequestURL().toString();
            String redirectUrl = url.substring(0, url.indexOf("/loading/"))
                    .replace("http://", "https://");
            redirectUrl += "/" + sponsor.getRollupGroupCode().toLowerCase()
                    + "/secure/init.do?uid=" + cp.getUser().getLogin()
                    + "&role=" + cp.getUser().getRole().name()
                    + "&sponsorcode=" + sponsor.getRollupGroupCode().toLowerCase()
                    + "&time=" + timeStamp;

            // write the encrypted AMAP token file
            writeEncodedToken(cp.getUser().getLogin(), sponsor.getRollupGroupCode(), timeStamp);

            ActionForward af = new ActionForward(redirectUrl, true);
            return af;
        }
    }

	

    private synchronized void writeEncodedToken(String uid, String code,
            String time) throws IOException {
		// log("Writing to file named " + fFileName + ". Encoding: " +
        // fEncoding);

        String fFileName = "/Users/pjain/NetBeansNew/FToken/FToken" + uid.toLowerCase()
                + code.toLowerCase() + time;
        FileOutputStream fo = new FileOutputStream(fFileName);
        Writer out = new OutputStreamWriter(fo, "UTF-8");
        try {
            out.write(uid.toLowerCase() + "&" + code.toLowerCase() + "&"
                    + System.currentTimeMillis());
        } finally {
            out.close();
            fo.close();
        }
    }

    private boolean urlLoading(HttpServletRequest req) {
        return req.getRequestURL().toString().contains("loading");
    }

    static void initMap(IInitParameters params, HttpServletRequest request) throws Exception {
        IMapProvider mapProvider = GetOpenLayers.getMapProvider();
        mapProvider.init(params, request);
        initLayers(mapProvider, params,request);
    }

    static void initLayers(IMapProvider mapProvider, IBaseParameters params,HttpServletRequest request)
            throws Exception {
        for (Layer layer : mapProvider
                .getLayers(params, GetLayers.LAYER_FILTER, request)) {
            mapProvider.setLayerVisibility(createParams(params, layer),request);
        }
    }

    private static ILayerVisibilityParameters createParams(
            final IBaseParameters params, final Layer layer) {
        return new ILayerVisibilityParameters() {
            @Override
            public String id() {
                return layer.getId();
            }

            @Override
            public Boolean visibility() {
                return Analysis.isDynamicLayer(layer.getName());
            }

            @Override
            public String mapInstanceKey() {
                return params.mapInstanceKey();
            }

            @Override
            public String name() {
                // return "";
                return layer.getName();
            }

            @Override
            public String parent() {
                return "";
            }

            @Override
            public String getLabelField() {
                return "";
            }
        };
    }
}
