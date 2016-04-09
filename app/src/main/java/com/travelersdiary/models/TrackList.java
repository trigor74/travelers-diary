package com.travelersdiary.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TrackList implements Serializable {
    private Map<Long, LocationPoint> track; // Key = timestamp

    public TrackList() {
    }

    public TrackList(Map<Long, LocationPoint> track) {
        this.track = track;
    }

    public Map<Long, LocationPoint> getTrack() {
        return track;
    }

    public void setTrack(Map<Long, LocationPoint> track) {
        this.track = track;
    }

    @JsonIgnore
    public List<LatLng> getSortedLatLngTrackList() {
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
