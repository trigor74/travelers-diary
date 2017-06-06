package com.travelersdiary.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TravelersDiary implements Serializable {
    private String name;
    private String email;
    private ActiveTravel activeTravel;
    private Map<String, DiaryNote> diary;
    private Map<String, ReminderItem> reminder;
    private Map<String, Travel> travels;
    private Map<String, Map<String, TrackList>> tracks;

    public TravelersDiary() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
