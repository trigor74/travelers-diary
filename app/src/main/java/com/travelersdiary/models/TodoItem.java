package com.travelersdiary.models;

import java.util.ArrayList;

/**
 * Created by itrifonov on 30.12.2015.
 */
public class TodoItem {
    private String title;
    private ArrayList<TodoTask> task;
    private boolean viewAsCheckboxes;
    private boolean completed;
    //Reminde at time
    private long time;
    private long interval;
    //Remind in a point
    private Waypoint waypoint;
    private int distance;
    private boolean repeat;

    public TodoItem() {
    }

    public TodoItem(String title, ArrayList<TodoTask> task, boolean viewAsCheckboxes, boolean completed,
                    long time, long interval, Waypoint waypoint, int distance, boolean repeat) {
        this.title = title;
        this.task = task;
        this.viewAsCheckboxes = viewAsCheckboxes;
        this.completed = completed;
        this.time = time;
        this.interval = interval;
        this.waypoint = waypoint;
        this.distance = distance;
        this.repeat = repeat;
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
