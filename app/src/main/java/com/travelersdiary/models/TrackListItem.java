package com.travelersdiary.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TrackListItem {
    private String travelId;
    private Map<Long, LocationPoint> track; // Key = timestamp

    public TrackListItem() {
    }

    public TrackListItem(String travelId, Map<Long, LocationPoint> track) {
        this.travelId = travelId;
        this.track = track;
    }

    public String getTravelId() {
        return travelId;
    }

    public void setTravelId(String travelId) {
        this.travelId = travelId;
    }

    public Map<Long, LocationPoint> getTrack() {
        return track;
    }

    public void setTrack(Map<Long, LocationPoint> track) {
        this.track = track;
    }

    @JsonIgnore
    public List<LatLng> getSortedLatLngTrack() {
        if (track == null) {
            return null;
        }

        TreeMap<Long, LocationPoint> sortedTrack = new TreeMap<>(track);

        List<LatLng> list = new ArrayList<>(sortedTrack.size());

        for (Map.Entry<Long, LocationPoint> entry : sortedTrack.entrySet()) {
            list.add(entry.getValue().getLatLng());
        }

        return list;
    }
}
