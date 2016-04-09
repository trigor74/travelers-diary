package com.travelersdiary.models;

import java.io.Serializable;

public class ActiveTravel implements Serializable {
    String activeTravelKey;
    String activeTravelTitle;

    public ActiveTravel() {
    }

    public ActiveTravel(String activeTravelKey, String activeTravelTitle) {
        this.activeTravelKey = activeTravelKey;
        this.activeTravelTitle = activeTravelTitle;
    }

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
