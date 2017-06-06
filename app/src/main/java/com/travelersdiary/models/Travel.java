package com.travelersdiary.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Travel implements Serializable {
    private long creationTime; //time creation
    private long start; //time stamp
    private long stop; //time stamp
    private String title;
    private String description;
    private boolean active;
    private String picasaAlbumId;
    private String defaultCover;
    private String userCover;

    public Travel() {
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
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

    public String getPicasaAlbumId() {
        return picasaAlbumId;
    }

    public void setPicasaAlbumId(String picasaAlbumId) {
        this.picasaAlbumId = picasaAlbumId;
    }

    public String getDefaultCover() {
        return defaultCover;
    }

    public void setDefaultCover(String defaultCover) {
        this.defaultCover = defaultCover;
    }

    public String getUserCover() {
        return userCover;
    }

    public void setUserCover(String userCover) {
        this.userCover = userCover;
    }
}
