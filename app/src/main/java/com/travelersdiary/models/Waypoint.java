package com.travelersdiary.models;

import java.io.Serializable;

public class Waypoint implements Serializable {
    private String travelId;
    private String title;
    private LocationPoint location;

    public Waypoint() {
    }

    public Waypoint(String travelId, String title, LocationPoint location) {
        this.travelId = travelId;
        this.title = title;
        this.location = location;
    }

    public String getTravelId() {
        return travelId;
    }

    public void setTravelId(String travelId) {
        this.travelId = travelId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocationPoint getLocation() {
        return location;
    }

    public void setLocation(LocationPoint location) {
        this.location = location;
    }
}
