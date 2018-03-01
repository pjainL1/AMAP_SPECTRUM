package com.korem.heatmaps.google;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jduchesne
 */
public class GmapsUtils {

    private static int ZOOM = 20;
    public static int GREATCIRCLE = 40075016;
    public static double GREATCIRCLE_D = 40075016D;
    private static long[] CBK = {128L, 256L, 512L, 1024L, 2048L, 4096L, 8192L, 16384L, 32768L, 65536L, 131072L, 262144L, 524288L, 1048576L, 2097152L, 4194304L, 8388608L, 16777216L, 33554432L, 67108864L, 134217728L, 268435456L, 536870912L, 1073741824L, 2147483648L, 4294967296L, 8589934592L, 17179869184L, 34359738368L, 68719476736L, 137438953472L};
    private static double[] CEK = {0.7111111111111111, 1.4222222222222223, 2.8444444444444446, 5.688888888888889, 11.377777777777778, 22.755555555555556, 45.51111111111111, 91.02222222222223, 182.04444444444445, 364.0888888888889, 728.1777777777778, 1456.3555555555556, 2912.711111111111, 5825.422222222222, 11650.844444444445, 23301.68888888889, 46603.37777777778, 93206.75555555556, 186413.51111111112, 372827.02222222224, 745654.0444444445, 1491308.088888889, 2982616.177777778, 5965232.355555556, 11930464.711111112, 23860929.422222223, 47721858.844444446, 95443717.68888889, 190887435.37777779, 381774870.75555557, 763549741.5111111};
    private static double[] CFK = {40.74366543152521, 81.48733086305042, 162.97466172610083, 325.94932345220167, 651.8986469044033, 1303.7972938088067, 2607.5945876176133, 5215.189175235227, 10430.378350470453, 20860.756700940907, 41721.51340188181, 83443.02680376363, 166886.05360752725, 333772.1072150545, 667544.214430109, 1335088.428860218, 2670176.857720436, 5340353.715440872, 10680707.430881744, 21361414.86176349, 42722829.72352698, 85445659.44705395, 170891318.8941079, 341782637.7882158, 683565275.5764316, 1367130551.1528633, 2734261102.3057265, 5468522204.611453, 10937044409.222906, 21874088818.445812, 43748177636.891624};


    static double getMeterPerPixel() {
        double meterPerPixel;
        double fTiles = Math.pow(2, ZOOM);
        double nbPx = fTiles * 256;
        meterPerPixel = GREATCIRCLE / (nbPx);
        return meterPerPixel;
    }

    static PointXY fromLatLngToPixel(double lng, double lat, int zoom) {
        long cbk = CBK[zoom];
        int x = (int) (Math.round(cbk + (lng * CEK[zoom])));

        double foo = Math.sin(lat * Math.PI / 180);
        if (foo < -0.9999) {
            foo = -0.9999;
        } else if (foo > 0.9999) {
            foo = 0.9999;
        }

        int y = (int) (Math.round(cbk + (0.5 * Math.log((1 + foo) / (1 - foo)) * (-CFK[zoom]))));

        return new PointXY(x, y);
    }

   public static int[] fromLngLatToPixel(double lng, double lat, int zoom, int ax, int ay) {
        long cbk = CBK[zoom];
        int x = (int) (Math.round(cbk + (lng * CEK[zoom])));

        double foo = Math.sin(lat * Math.PI / 180);
        if (foo < -0.9999) {
            foo = -0.9999;
        } else if (foo > 0.9999) {
            foo = 0.9999;
        }

        int y = (int) (Math.round(cbk + (0.5 * Math.log((1 + foo) / (1 - foo)) * (-CFK[zoom]))));

        return new int[] { x -ax, y -ay };
    }

    static PointXY fromLatLngToPixel(PointXY latLng, int zoom, int ax, int ay) {
        double lat = latLng.getY();
        double lng = latLng.getX();
        long cbk = CBK[zoom];
        int x = (int) (Math.round(cbk + (lng * CEK[zoom])));

        double foo = Math.sin(lat * Math.PI / 180);
        if (foo < -0.9999) {
            foo = -0.9999;
        } else if (foo > 0.9999) {
            foo = 0.9999;
        }

        int y = (int) (Math.round(cbk + (0.5 * Math.log((1 + foo) / (1 - foo)) * (-CFK[zoom]))));

        return new PointXY(x-ax, y-ay);
    }
    // not precise

    static PointXY fromPixelToLatLng(PointXY point, int zoom) {
        //"""Given three intsL, return a 2-tuple of floats.
        //Note that the pixel coordinates are tied to the entire mapL, not to the map
        //section currently in view.
        double x = point.getX();
        double y = point.getY();

        double foo = CBK[zoom];
        double lng = (x - foo) / CEK[zoom];
        double bar = (y - foo) / -CFK[zoom];
        double blam = 2 * Math.atan(Math.exp(bar)) - Math.PI / 2;
        double lat = blam / (Math.PI / 180);

        return new PointXY(lng, lat);
    }

    static List<PointXY> decodeToPixel(String encoded, int zoom) {

        int len = encoded.length();
        int index = 0;
        List<PointXY> array = new ArrayList<PointXY>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {

                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = 0;
            if ((result & 1) != 0) {
                dlat = ~(result >> 1);
            } else {
                dlat = (result >> 1);
            }
            //dlat = ((result & 1)>0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = 0;

            if ((result & 1) != 0) {
                dlng = ~(result >> 1);
            } else {
                dlng = (result >> 1);
            }
             //dlng = ((result & 1)>0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            array.add(fromLatLngToPixel(lng * 1e-5, lat * 1e-5, zoom));
        }
        return array;
    }

    static List<PointXY> decodeLine(String encoded) {
        int len = encoded.length();
        int index = 0;
        List<PointXY> array = new ArrayList();
        int lat = 0;
        int lng = 0;

        while (index < len) {

            int b;
            int shift = 0;
            int result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = 0;
            if ((result & 1) != 0) {
                dlat = ~(result >> 1);
            } else {
                dlat = (result >> 1);
            }
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = 0;

            if ((result & 1) != 0) {
                dlng = ~(result >> 1);
            } else {
                dlng = (result >> 1);
            }
            lng += dlng;

            array.add(new PointXY(lng * 1e-5, lat * 1e-5));
        }

        return array;
    }


    static class PointXY {

        double x;
        double y;

        public PointXY(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }
    }

}
