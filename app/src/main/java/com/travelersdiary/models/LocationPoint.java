package com.travelersdiary.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class LocationPoint implements Serializable {
    private double latitude;
    private double longitude;
    private double altitude;

    public LocationPoint() {
    }

    public LocationPoint(double latitude, double longitude, double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    @JsonIgnore
    public void setLocation(double latitude, double longitude, double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    @JsonIgnore
    public void setLocation(double latitude, double longitude) {
        this.setLocation(latitude, longitude, 0);
    }

    @JsonIgnore
    public LatLng getLatLng() {
        return new LatLng(this.latitude, this.longitude);
    }

    @JsonIgnore
    public void setLatLng(LatLng point) {
        this.setLocation(point.latitude, point.longitude, 0);
    }
}
