package com.travelersdiary.models;

import java.io.Serializable;

public class Travel implements Serializable {
    private long start; //time stamp
    private long stop; //time stamp
    private String title;
    private String description;
    private boolean active;

    public Travel() {
    }

    public Travel(long start, long stop, String title, String description, boolean active) {
        this.start = start;
        this.stop = stop;
        this.title = title;
        this.description = description;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
