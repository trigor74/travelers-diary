package com.travelersdiary.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ActiveTravel implements Serializable {
    private String activeTravelKey;
    private String activeTravelTitle;

    public String getActiveTravelKey() {
        return activeTravelKey;
    }

    public void setActiveTravelKey(String activeTravelKey) {
        this.activeTravelKey = activeTravelKey;
    }

    public String getActiveTravelTitle() {
        return activeTravelTitle;
    }

    public void setActiveTravelTitle(String activeTravelTitle) {
        this.activeTravelTitle = activeTravelTitle;
    }
}
