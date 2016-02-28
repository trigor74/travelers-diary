package com.travelersdiary.models;

import java.io.Serializable;

public class TrackPoint implements Serializable {
    private String travelId;
    private long time;
    private LocationPoint location;

    public TrackPoint() {
    }

    public TrackPoint(String travelId, long time, LocationPoint location) {
        this.travelId = travelId;
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

    public String getTravelId() {
        return travelId;
    }

    public void setTravelId(String travelId) {
        this.travelId = travelId;
    }
}
