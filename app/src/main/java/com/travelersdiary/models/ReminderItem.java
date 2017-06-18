package com.travelersdiary.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReminderItem implements Serializable {
    private int UID; // unique identifier of item for notifications and pendingIntent
    private String travelId;
    private String travelTitle;
    private String title;
    private ArrayList<TodoTask> task;
    private boolean viewAsCheckboxes;
    private boolean completed;
    private boolean active;
    private String type; // "time" - remind at time, "locatiton" - remind at location, null or empty - don't remind
    //Remind at time
    private long time;
    private long interval;
    //Remind in a point
    private Waypoint waypoint;
    private int distance;
    private boolean repeat;

    public ReminderItem() {
    }

    public int getUID() {
        return UID;
    }

    public void setUID(int UID) {
        this.UID = UID;
    }

    public String getTravelId() {
        return travelId;
    }

    public void setTravelId(String travelId) {
        this.travelId = travelId;
    }

    public String getTravelTitle() {
        return travelTitle;
    }

    public void setTravelTitle(String travelTitle) {
        this.travelTitle = travelTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<TodoTask> getTask() {
        return task;
    }

    public void setTask(ArrayList<TodoTask> task) {
        this.task = task;
    }

    public boolean isViewAsCheckboxes() {
        return viewAsCheckboxes;
    }

    public void setViewAsCheckboxes(boolean viewAsCheckboxes) {
        this.viewAsCheckboxes = viewAsCheckboxes;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public Waypoint getWaypoint() {
        return waypoint;
    }

    public void setWaypoint(Waypoint waypoint) {
        this.waypoint = waypoint;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }
}
