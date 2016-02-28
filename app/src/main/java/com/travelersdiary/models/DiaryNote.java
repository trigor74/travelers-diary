package com.travelersdiary.models;

import java.io.Serializable;
import java.util.ArrayList;

public class DiaryNote implements Serializable {
    private String travelId;
    private String travelTitle;
    private long time;
    private String title;
    private String text;
    private LocationPoint location;
    private WeatherInfo weather;
    private ArrayList<String> photos;
    private ArrayList<String> audios;
    private ArrayList<String> videos;

    public DiaryNote() {
    }

    public DiaryNote(String travelId, long time, String title, String text, LocationPoint location,
                     WeatherInfo weather, ArrayList<String> photos, ArrayList<String> audios,
                     ArrayList<String> videos) {
        this.travelId = travelId;
        this.time = time;
        this.title = title;
        this.text = text;
        this.location = location;
        this.weather = weather;
        this.photos = photos;
        this.audios = audios;
        this.videos = videos;
    }

    public String getTravelId() {
        return travelId;
    }

    public void setTravelId(String travelId) {
        this.travelId = travelId;
    }

    public String getTravelTitle() {
        return travelTitle;
    }

    public void setTravelTitle(String travelTitle) {
        this.travelTitle = travelTitle;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocationPoint getLocation() {
        return location;
    }

    public void setLocation(LocationPoint location) {
        this.location = location;
    }

    public WeatherInfo getWeather() {
        return weather;
    }

    public void setWeather(WeatherInfo weather) {
        this.weather = weather;
    }

    public ArrayList<String> getPhotos() {
        return photos;
    }

    public void setPhotos(ArrayList<String> photos) {
        this.photos = photos;
    }

    public ArrayList<String> getAudios() {
        return audios;
    }

    public void setAudios(ArrayList<String> audios) {
        this.audios = audios;
    }

    public ArrayList<String> getVideos() {
        return videos;
    }

    public void setVideos(ArrayList<String> videos) {
        this.videos = videos;
    }
}
