package com.travelersdiary.models;

import java.util.ArrayList;

/**
 * Created by itrifonov on 30.12.2015.
 */
public class DiaryNote {
    private int travelId;
    private long time;
    private String title;
    private String text;
    private LocationPoint location;
    private WeatherInfo weather;
    private ArrayList<String> photos;
    private ArrayList<String> audios;
    private ArrayList<String> videos;
    private ArrayList<String> tags;

    public DiaryNote() {
    }

    public DiaryNote(int travelId, long time, String title, String text, LocationPoint location,
                     WeatherInfo weather, ArrayList<String> photos, ArrayList<String> audios, ArrayList<String> videos,
                     ArrayList<String> tags) {
        this.travelId = travelId;
        this.time = time;
        this.title = title;
        this.text = text;
        this.location = location;
        this.weather = weather;
        this.photos = photos;
        this.audios = audios;
        this.videos = videos;
        this.tags = tags;
    }

    public int getTravelId() {
        return travelId;
    }

    public void setTravelId(int travelId) {
        this.travelId = travelId;
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

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }
}
