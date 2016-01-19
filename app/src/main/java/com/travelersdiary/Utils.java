package com.travelersdiary;

/**
 * Helper class with methods
 */

public class Utils {
    public static String getFirebaseUserUrl (String userUID){
        return Constants.FIREBASE_URL + "/" + Constants.FIREBASE_USERS + "/" + userUID;
    }

    public static String getFirebaseUserTravelsUrl (String userUID){
        return Constants.FIREBASE_URL + "/" + Constants.FIREBASE_USERS + "/" + userUID + "/" + Constants.FIREBASE_TRAVELS;
    }

    public static String getFirebaseUserDiaryUrl (String userUID){
        return Constants.FIREBASE_URL + "/" + Constants.FIREBASE_USERS + "/" + userUID + "/" + Constants.FIREBASE_DIARY;
    }

    public static String getFirebaseUserTracksUrl (String userUID){
        return Constants.FIREBASE_URL + "/" + Constants.FIREBASE_USERS + "/" + userUID + "/" + Constants.FIREBASE_TRACKS;
    }

    public static String getFirebaseUserReminderUrl (String userUID){
        return Constants.FIREBASE_URL + "/" + Constants.FIREBASE_USERS + "/" + userUID + "/" + Constants.FIREBASE_REMINDER;
    }

    public static String getFirebaseUserWaypointsUrl (String userUID){
        return Constants.FIREBASE_URL + "/" + Constants.FIREBASE_USERS + "/" + userUID + "/" + Constants.FIREBASE_WAYPOINTS;
    }
}
