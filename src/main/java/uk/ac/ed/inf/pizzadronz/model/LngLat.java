package uk.ac.ed.inf.pizzadronz.model;

import static uk.ac.ed.inf.pizzadronz.constants.SystemConstants.DRONE_IS_CLOSE_DISTANCE;

public class LngLat {
    private double lng;
    private double lat;
    private boolean validPosition;

    public LngLat(double lng, double lat) {
        this.lng = lng;
        this.lat = lat;

    }




    public LngLat(){

    }

    public double getLng() {
        return lng;
    }
    public double getLat() {
        return lat;
    }
    public void setLng(double lng) {
        this.lng = lng;
    }
    public void setLat(double lat) {
        this.lat = lat;
    }


    public double distanceTo(LngLat other) {
        double dLng = other.getLng() - this.lng;
        double dLat = other.getLat() - this.lat;
        return Math.sqrt(dLng * dLng + dLat * dLat);
    }

    public boolean isValidPosition() {
        return this.lng >= -180 && this.lng <= 180 && this.lat <= 90 && this.lat >= -90;
    }

    public boolean isCloseTo(LngLat point){
        return this.distanceTo(point) <= DRONE_IS_CLOSE_DISTANCE;
    }

    public boolean getValidPosition() {
        return this.validPosition;
    }
}
