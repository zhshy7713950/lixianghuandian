package wongxd.navi;


/**
 * Created by wongxd on 2018/11/15.
 * https://github.com/wongxd
 * wxd1@live.com
 */
public class LatLng {

    private double longitude;//经度

    private double latitude;//维度


    public LatLng() {

    }


    public LatLng(double latitude, double longitude) {

        this.longitude = longitude;

        this.latitude = latitude;

    }


    public double getLongitude() {

        return longitude;

    }


    public void setLongitude(double longitude) {

        this.longitude = longitude;

    }


    public double getLatitude() {

        return latitude;

    }


    public void setLatitude(double latitude) {

        this.latitude = latitude;

    }


    @Override

    public String toString() {

        return "LngLat{" +

                "longitude=" + longitude +

                ", lantitude=" + latitude +

                '}';

    }

}
