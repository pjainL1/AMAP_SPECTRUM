package com.lo.web;

import com.korem.IWMSParams;
import com.korem.invocationThrottling.AbstractMethod;
import com.korem.invocationThrottling.Throttler;
import com.korem.openlayers.IMapProvider;
import com.korem.openlayers.kms.MapProvider;
import com.korem.openlayers.kms.MapProviderFactory;
import com.korem.openlayers.parameters.IBoundsParameters;
import com.korem.openlayers.parameters.IImageParameters;
import com.korem.requestHelpers.PlainGenericServlet;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author jduchesne
 */
public class GetOpenLayers extends PlainGenericServlet<IWMSParams> {

    private static final String SES_THROTTLER = "getopenlayers_throller";
    private static final String SES_IMAGE = "getopenlayers_image";
    public static final String SESSIONATTRIBUTE_MAPPROVIDER = "SESSIONATTRIBUTE_MAPPROVIDER";

    private static IMapProvider globalMapProvider;

    @Override
    protected void execute(final HttpServletRequest request, final HttpServletResponse response, final IWMSParams params) throws Exception {
        
        final IMapProvider mapProvider = getMapProvider();
        final IImageParameters imageParams = createImageParameters(params);
        final IBoundsParameters boundsParams = createBoundsParameters(params);
        AbstractMethod method = new AbstractMethod() {
            @Override
            protected Object doInvoke() {
                try {
                    mapProvider.setBounds(boundsParams);
                    byte[] image = mapProvider.getImage(imageParams.mapInstanceKey(),boundsParams,request, imageParams.format(), imageParams.width(), imageParams.height());
                    
                    //byte[] image = getTransparentTile();
                    if (writeImage(image, response, params)) {
                        saveImage(image, request.getSession());
                    }
                    return null;
                } catch (Exception ex) {
                    return ex;
                }
            }
        };
        getThrottler(request.getSession(), imageParams.mapInstanceKey()).invoke(method);
        if (method.wasInvoked()) {
            if (method.getResult() instanceof Exception) {
                throw (Exception)method.getResult();
            }
        } else {
            writePreviousImage(response, request.getSession(), params);
        }
    }
    
    public synchronized static IMapProvider getMapProvider() throws Exception {
        if(globalMapProvider == null){
            globalMapProvider = MapProviderFactory.instance().createMapProvider();
        }
        return globalMapProvider;
    }

    private void writePreviousImage(HttpServletResponse response, HttpSession session, IWMSParams params) throws IOException {
        byte[] image = (byte[])session.getAttribute(SES_IMAGE);
        if (image != null) {
            writeImage(image, response, params);
        }
    }
    
    private byte[] getTransparentTile() {
        try {
            BufferedImage finalTile = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);

            byte[] imageInByte = null;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(finalTile, "png", baos);
            baos.flush();
            imageInByte = baos.toByteArray();
            baos.close();

            return imageInByte;
        } catch (Exception e) {
            //LOGGER.error("Error while getting Empty tile", e);
            return null;
        }
    }

    private boolean writeImage(byte[] image, HttpServletResponse response, IWMSParams params) {
        try {
            response.setContentType(params.FORMAT());
            response.getOutputStream().write(image);
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }
//    
//        private byte[] spectrumImage(){
//    
//        SpectrumRenderTile srt = new SpectrumRenderTile() ;
//        srt.createImageFromTiles();
//        try {
//            BufferedImage img = srt.getMergedTiles();
//            
//        } catch (ServletException ex) {
//            java.util.logging.Logger.getLogger(MapProvider.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        
//        return null;
//    }

    private void saveImage(byte[] image, HttpSession session) {
        session.setAttribute(SES_IMAGE, image);
    }

    private Throttler getThrottler(HttpSession session, String mapInstanceKey) {
        Throttler throttler = (Throttler)session.getAttribute(SES_THROTTLER + mapInstanceKey);
        if (throttler == null) {
            session.setAttribute(SES_THROTTLER + mapInstanceKey, throttler = new Throttler());
        }
        return throttler;
    }

    private IBoundsParameters createBoundsParameters(final IWMSParams params) {
        String[] strBounds = params.BBOX().split(",");
        final double xmin = Double.parseDouble(strBounds[0]);
        final double ymin = Double.parseDouble(strBounds[1]);
        final double xmax = Double.parseDouble(strBounds[2]);
        final double ymax = Double.parseDouble(strBounds[3]);
        return new IBoundsParameters() {

            @Override
            public double xmin() {
                return xmin;
            }

            @Override
            public double ymin() {
                return ymin;
            }

            @Override
            public double xmax() {
                return xmax;
            }

            @Override
            public double ymax() {
                return ymax;
            }

            @Override
            public String mapInstanceKey() {
                return params.LAYERS();
            }
        };
    }

    private IImageParameters createImageParameters(final IWMSParams params) {
        return new IImageParameters() {

            @Override
            public String mapInstanceKey() {
                return params.LAYERS();
            }

            @Override
            public String format() {
                return params.FORMAT();
            }

            @Override
            public Integer width() {
                return params.WIDTH();
            }

            @Override
            public Integer height() {
                return params.HEIGHT();
            }
        };
    }
}
