package com.travelersdiary.models;

import java.util.ArrayList;

/**
 * Created by itrifonov on 31.12.2015.
 */
public class Travel {
    private long start; //time stamp
    private long stop; //time stamp
    private String title;
    private String description;
    private ArrayList<TodoItem> reminder;
    private ArrayList<Waypoint> waypoints;
    private ArrayList<TrackPoint> track;
    private boolean active;

    public Travel() {
    }

    public Travel(long start, long stop, String title, String description,
                  ArrayList<TodoItem> reminder, ArrayList<Waypoint> waypoints,
                  ArrayList<TrackPoint> track, boolean active) {
        this.start = start;
        this.stop = stop;
        this.title = title;
        this.description = description;
        this.reminder = reminder;
        this.waypoints = waypoints;
        this.track = track;
        this.active = active;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getStop() {
        return stop;
    }

    public void setStop(long stop) {
        this.stop = stop;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<TodoItem> getReminder() {
        return reminder;
    }

    public void setReminder(ArrayList<TodoItem> reminder) {
        this.reminder = reminder;
    }

    public ArrayList<Waypoint> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(ArrayList<Waypoint> waypoints) {
        this.waypoints = waypoints;
    }

    public ArrayList<TrackPoint> getTrack() {
        return track;
    }

    public void setTrack(ArrayList<TrackPoint> track) {
        this.track = track;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
