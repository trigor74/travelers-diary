package com.travelersdiary.models;

import java.io.Serializable;
import java.util.Map;

public class TravelersDiary implements Serializable {
    ActiveTravel activeTravel;
    Map<String, DiaryNote> diary;
    Map<String, ReminderItem> reminder;
    Map<String, Travel> travels;
    Map<String, Map<String, TrackList>> tracks;
}
