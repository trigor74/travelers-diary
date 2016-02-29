package com.travelersdiary;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.bumptech.glide.Glide;

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarColor(Activity activity, int color) {
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }

    public static void clearImageCache(final Context context) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                Glide.get(context).clearDiskCache();
                return true;
            }
        };
        task.execute();
    }

    public static void tintWidget(Context context, View view, int color) {
        Drawable wrappedDrawable = DrawableCompat.wrap(view.getBackground());
        DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(context, color));
        view.setBackground(wrappedDrawable);
    }
}
