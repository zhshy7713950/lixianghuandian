package wongxd.navi;

public class Converter {


    private final static double a = 6378245.0;

    private final static double pi = 3.14159265358979324;

    private final static double ee = 0.00669342162296594323;

    //GPS使用的是WGS-84
    //高德地图的是GCJ-02

    public static LatLng gps2gaode(double latitude, double longitude) {
        return toGCJ02Point(latitude, longitude);
    }

    // WGS-84 to GCJ-02

    private static LatLng toGCJ02Point(double latitude, double longitude) {

        LatLng dev = calDev(latitude, longitude);

        double retLat = latitude + dev.getLatitude();

        double retLon = longitude + dev.getLongitude();

        return new LatLng(retLat, retLon);

    }

    public static LatLng gaode2gps(double latitude, double longitude) {
        return toWGS84Point(latitude, longitude);
    }

    // GCJ-02 to WGS-84

    private static LatLng toWGS84Point(double latitude, double longitude) {

        LatLng dev = calDev(latitude, longitude);

        double retLat = latitude - dev.getLatitude();

        double retLon = longitude - dev.getLongitude();

        dev = calDev(retLat, retLon);

        retLat = latitude - dev.getLatitude();

        retLon = longitude - dev.getLongitude();

        return new LatLng(retLat, retLon);

    }


    private static LatLng calDev(double wgLat, double wgLon) {

        if (isOutOfChina(wgLat, wgLon)) {

            return new LatLng(0, 0);

        }

        double dLat = calLat(wgLon - 105.0, wgLat - 35.0);

        double dLon = calLon(wgLon - 105.0, wgLat - 35.0);

        double radLat = wgLat / 180.0 * pi;

        double magic = Math.sin(radLat);

        magic = 1 - ee * magic * magic;

        double sqrtMagic = Math.sqrt(magic);

        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);

        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);

        return new LatLng(dLat, dLon);

    }


    private static boolean isOutOfChina(double lat, double lon) {

        if (lon < 72.004 || lon > 137.8347)

            return true;

        if (lat < 0.8293 || lat > 55.8271)

            return true;

        return false;

    }


    private static double calLat(double x, double y) {

        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2

                * Math.sqrt(Math.abs(x));

        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;

        ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;

        ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;

        return ret;

    }


    private static double calLon(double x, double y) {

        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));

        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;

        ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;

        ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0;

        return ret;

    }


}
