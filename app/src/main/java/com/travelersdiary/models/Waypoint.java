package com.travelersdiary.models;

/**
 * Created by itrifonov on 30.12.2015.
 */
public class Waypoint {
    private String title;
    private LocationPoint location;
    private String id;

    public Waypoint() {
    }

    public Waypoint(String title, LocationPoint location) {
        this.title = title;
        this.location = location;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
