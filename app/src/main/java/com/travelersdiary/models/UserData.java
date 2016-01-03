package com.travelersdiary.models;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by itrifonov on 31.12.2015.
 */
public class UserData {
    private ArrayList<DiaryNote> diary;
    private HashMap<String, Travel> travels; // Key = UUID.randomUUID()

    public UserData() {
    }

    public UserData(ArrayList<DiaryNote> diary,
                    HashMap<String, Travel> travels) {
        this.diary = diary;
        this.travels = travels;
    }

    public ArrayList<DiaryNote> getDiary() {
        return diary;
    }

    public void setDiary(ArrayList<DiaryNote> diary) {
        this.diary = diary;
    }

    public HashMap<String, Travel> getTravels() {
        return travels;
    }

    public void setTravels(HashMap<String, Travel> travels) {
        this.travels = travels;
    }
}
