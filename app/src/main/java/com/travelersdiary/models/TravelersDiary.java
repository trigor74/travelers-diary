package com.travelersdiary.models;

import java.io.Serializable;
import java.util.Map;

public class TravelersDiary implements Serializable {
    ActiveTravel activeTravel;
    Map<String, DiaryNote> diary;
    Map<String, ReminderItem> reminder;
    Map<String, Travel> travels;
    Map<String, Map<String, TrackList>> tracks;

    public TravelersDiary() {
    }

    public TravelersDiary(ActiveTravel activeTravel, Map<String, DiaryNote> diary,
                          Map<String, ReminderItem> reminder, Map<String, Travel> travels,
                          Map<String, Map<String, TrackList>> tracks) {
        this.activeTravel = activeTravel;
        this.diary = diary;
        this.reminder = reminder;
        this.travels = travels;
        this.tracks = tracks;
    }

    public ActiveTravel getActiveTravel() {
        return activeTravel;
    }

    public void setActiveTravel(ActiveTravel activeTravel) {
        this.activeTravel = activeTravel;
    }

    public Map<String, DiaryNote> getDiary() {
        return diary;
    }

    public void setDiary(Map<String, DiaryNote> diary) {
        this.diary = diary;
    }

    public Map<String, ReminderItem> getReminder() {
        return reminder;
    }

    public void setReminder(Map<String, ReminderItem> reminder) {
        this.reminder = reminder;
    }

    public Map<String, Travel> getTravels() {
        return travels;
    }

    public void setTravels(Map<String, Travel> travels) {
        this.travels = travels;
    }

    public Map<String, Map<String, TrackList>> getTracks() {
        return tracks;
    }

    public void setTracks(Map<String, Map<String, TrackList>> tracks) {
        this.tracks = tracks;
    }
}
