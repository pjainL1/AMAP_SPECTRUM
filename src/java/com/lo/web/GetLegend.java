package com.lo.web;

import com.korem.requestHelpers.RequestParser;
import com.korem.requestHelpers.Servlet;
import com.lo.config.Confs;
import com.lo.util.WSClientLone;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This Servlet send a Legend as a gif image.
 * @author Charles St-Hilaire for Korem inc.
 */
@WebServlet(name="getLegend", urlPatterns={"/getLegend.safe"})
public class GetLegend extends Servlet{
    public interface Params {
        String mapInstanceKey();
        String layerID();
    }
    /**
     * This method is responsible to call LoneThematic WS and then return
     * the legend as a GIF (image/gif) image of appropriate font attribute.
     * The attribute in used are define under kmsLengend.* properties in
     * config.properties file.
     * 
     * @param request an HttpServletRequest
     * @param response an HttpServletResponse
     * @throws Exception if something wrong occurs
     */
    @Override
    protected void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Params params = RequestParser.parse(request, GetLegend.Params.class);
        byte[] gif = WSClientLone.getLoneThematicService().getLegend(params.mapInstanceKey(),
                                                                     params.layerID(),
                                                                     Confs.STATIC_CONFIG.kmsLegendFontName(),
                                                                     Confs.STATIC_CONFIG.kmsLegendFontStyle(),
                                                                     Confs.STATIC_CONFIG.kmsLegendFontSize());
        response.setContentType("image/gif");
        response.setContentLength(gif.length);
        response.getOutputStream().write(gif);
        response.flushBuffer();
    }
}
