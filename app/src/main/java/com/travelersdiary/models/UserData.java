package com.travelersdiary.models;

import java.util.ArrayList;

/**
 * Created by itrifonov on 31.12.2015.
 */
public class UserData {
    private ArrayList<TodoItem> reminder;
    private ArrayList<DiaryNote> diary;
    private ArrayList<Travel> travels;
    private ArrayList<TagListItem> tags;

    public UserData() {
    }

    public UserData(ArrayList<TodoItem> reminder, ArrayList<DiaryNote> diary, ArrayList<Travel> travels, ArrayList<TagListItem> tags) {
        this.reminder = reminder;
        this.diary = diary;
        this.travels = travels;
        this.tags = tags;
    }

    public ArrayList<TodoItem> getReminder() {
        return reminder;
    }

    public void setReminder(ArrayList<TodoItem> reminder) {
        this.reminder = reminder;
    }

    public ArrayList<DiaryNote> getDiary() {
        return diary;
    }

    public void setDiary(ArrayList<DiaryNote> diary) {
        this.diary = diary;
    }

    public ArrayList<Travel> getTravels() {
        return travels;
    }

    public void setTravels(ArrayList<Travel> travels) {
        this.travels = travels;
    }

    public ArrayList<TagListItem> getTags() {
        return tags;
    }

    public void setTags(ArrayList<TagListItem> tags) {
        this.tags = tags;
    }
}
