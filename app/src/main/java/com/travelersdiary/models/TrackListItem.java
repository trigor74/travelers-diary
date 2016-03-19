package com.travelersdiary.models;

import java.util.Map;

public class TrackListItem {
    private String travelId;
    private Map<Long, LocationPoint> track; // Key = timestamp
}
