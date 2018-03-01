package com.lo.web;

import com.korem.IWMSParams;
import com.korem.heatmaps.DensityHeatMap;
import com.korem.heatmaps.HeatMapRule;
import com.korem.heatmaps.HeatMapRules;
import com.korem.heatmaps.Properties;
import com.korem.heatmaps.RulesLoader;
import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.requestHelpers.PlainGenericServlet;
import com.lo.hotspot.HotSpotFactory;
import com.lo.hotspot.HotSpotMethod;
import com.spinn3r.log5j.Logger;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.sf.json.JSONArray;

/**
 *
 * @author jduchesne
 */
public class GetHeatMap extends PlainGenericServlet<IWMSParams> {

    private static final Logger log = Logger.getLogger();

    public static interface IParams extends IBaseParameters {

        Double xmin();

        Double ymin();

        Double xmax();

        Double ymax();

        Integer width();

        Integer height();

        Integer zoom();
        
        JSONArray filters(); 
    }
    public static final int STEPS_COLOR = 240;
    private static final float ALPHA = .9f;
    private static HeatMapRules rules = createRules();

    @Override
    protected void execute(HttpServletRequest request, HttpServletResponse response, IWMSParams wmsParams) throws Exception {
        response.setContentType(Properties.get().getHttpImageFormat());
        OutputStream out = response.getOutputStream();

        IParams p = createHeatMapParams(wmsParams, request);
        double xmin = p.xmin();
        double ymin = p.ymin();
        double xmax = p.xmax();
        double ymax = p.ymax();
        List<Integer> selectedCodes = new ArrayList<>();
        JSONArray sponsorFilters = p.filters();
        String mapInstanceKey = p.mapInstanceKey();
        if (sponsorFilters != null) {
            if (!sponsorFilters.isEmpty()) {
                for (int i = 0; i < sponsorFilters.size(); i++) {
                    selectedCodes.add(sponsorFilters.getInt(i));
                }
            }
        }
        BufferedImage img = getBackbuffer(request, xmin, ymin, xmax, ymax, mapInstanceKey);
        if (img == null) {
            HeatMapRule rule = rules.getRule(p.zoom());
            HotSpotFactory factory = HotSpotMethod.getHotSpotFactory(request.getSession(), p);

            DensityHeatMap heatMap = factory.create(
                    p.width(), p.height(), ALPHA, xmin, ymin, xmax, ymax, rule, STEPS_COLOR,
                    Properties.get().getColors(), p.zoom(), selectedCodes);
            if (heatMap != null) {
                img = heatMap.paint();
                setBackbuffer(request, xmin, ymin, xmax, ymax, mapInstanceKey, img);
            }
        }
        if (img != null) {
            try {
                ImageIO.write(img, Properties.get().getImageFormat(), out);
                out.close();
            } catch (IOException ioe) {
                log.debug("", ioe);
            }
        }
    }

    private BufferedImage getBackbuffer(HttpServletRequest request, double xmin,
            double ymin, double xmax, double ymax, String mapInstanceKey) {
        HttpSession session = request.getSession();
        if (Double.valueOf(xmin).equals(session.getAttribute("previousXMin"))
                && Double.valueOf(ymin).equals(session.getAttribute("previousYMin"))
                && Double.valueOf(xmax).equals(session.getAttribute("previousXMax"))
                && Double.valueOf(ymax).equals(session.getAttribute("previousYMax"))
                && mapInstanceKey.equals(session.getAttribute("previousMapInstanceKey"))) {
            return (BufferedImage) session.getAttribute("backbuffer");
        }
        return null;
    }

    static void clearBackbuffer(HttpSession session) {
        session.removeAttribute("backbuffer");
    }

    private void setBackbuffer(HttpServletRequest request, double xmin, double ymin,
            double xmax, double ymax, String mapInstanceKey, BufferedImage backbuffer) {
        final HttpSession session = request.getSession();
        session.setAttribute("previousXMin", xmin);
        session.setAttribute("previousYMin", ymin);
        session.setAttribute("previousXMax", xmax);
        session.setAttribute("previousYMax", ymax);
        session.setAttribute("previousMapInstanceKey", mapInstanceKey);
        session.setAttribute("backbuffer", backbuffer);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(30000);
                    clearBackbuffer(session);
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        }).start();
    }

    private IParams createHeatMapParams(final IWMSParams wmsParams, final HttpServletRequest request) {
        String[] strBounds = wmsParams.BBOX().split(",");
        final double xmin = Double.parseDouble(strBounds[0]);
        final double ymin = Double.parseDouble(strBounds[1]);
        final double xmax = Double.parseDouble(strBounds[2]);
        final double ymax = Double.parseDouble(strBounds[3]);
        return new IParams() {

            @Override
            public String mapInstanceKey() {
                return wmsParams.LAYERS();
            }

            @Override
            public Double xmin() {
                return xmin;
            }

            @Override
            public Double ymin() {
                return ymin;
            }

            @Override
            public Double xmax() {
                return xmax;
            }

            @Override
            public Double ymax() {
                return ymax;
            }

            @Override
            public Integer width() {
                return wmsParams.WIDTH();
            }

            @Override
            public Integer height() {
                return wmsParams.HEIGHT();
            }

            @Override
            public Integer zoom() {
                return Integer.parseInt(request.getParameter("zoom"));
            }

            @Override
            public JSONArray filters() {
                return wmsParams.filters();
            }

        };
    }

    public static HeatMapRules createRules() {
        try {
            return new RulesLoader(GetHeatMap.class.getResource("/com/korem/heatmaps/rules.xml").getFile()).load();
        } catch (Exception e) {
            log.error(null, e);
        }
        return null;
    }
}
