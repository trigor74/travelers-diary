package com.travelersdiary.models;

/**
 * Created by itrifonov on 30.12.2015.
 */
public class TrackPoint {
    private long time;
    private LocationPoint location;

    public TrackPoint() {
    }

    public TrackPoint(long time, LocationPoint location) {
        this.time = time;
        this.location = location;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public LocationPoint getLocation() {
        return location;
    }

    public void setLocation(LocationPoint location) {
        this.location = location;
    }
}
