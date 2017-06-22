package com.travelersdiary;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.firebase.client.annotations.NotNull;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.travelersdiary.models.LocationPoint;
import com.travelersdiary.models.ReminderItem;
import com.travelersdiary.services.GeofenceTransitionsIntentService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GeofenceSetter implements
        OnCompleteListener<Void> {

    private static final String TAG = "GeofenceSetter";

    private static final int DEFAULT_RADIUS = 500;
    private static final int DEFAULT_NOTIFICATION_RESPONSIVENESS = 2000; // 2 second

    private static final int TASK_NONE = 0;
    private static final int TASK_ADD = 1;
    private static final int TASK_REMOVE = 2;
    private int mTask = TASK_NONE;

    private Context mContext;
    private String mItemKey;
    private GeofencingClient mGeofencingClient;
    private ArrayList<Geofence> mGeofenceList;

    public GeofenceSetter(@NonNull @NotNull Activity activity) {
        mGeofencingClient = LocationServices.getGeofencingClient(activity);
        mContext = activity;
        mTask = TASK_NONE;
    }

    public GeofenceSetter(@NonNull @NotNull Context context) {
        mGeofencingClient = LocationServices.getGeofencingClient(context);
        mContext = context;
        mTask = TASK_NONE;
    }

    public void setGeofence(ReminderItem reminderItem, String itemKey) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        Set<String> stringHashSet = new HashSet<>();
        stringHashSet = sharedPreferences.getStringSet(Constants.KEY_GEOFENCE_SET, stringHashSet);

        if (stringHashSet.contains(itemKey)) {
            return;
        }
        mTask = TASK_ADD;
        mItemKey = itemKey;
        LocationPoint locationPoint = reminderItem.getWaypoint().getLocation();
        int radius = reminderItem.getDistance();

        PendingIntent pendingIntent = getGeofenceTransitionsPendingIntent();
        GeofencingRequest geofencingRequest = getGeofencingRequest(getGeofence(itemKey, locationPoint.getLatitude(), locationPoint.getLongitude(), radius));
        try {
            addGeofence(geofencingRequest, pendingIntent);
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            showSecurityException(securityException);
        }

//        stringHashSet.add(itemKey);
//        sharedPreferences.edit().putStringSet(Constants.KEY_GEOFENCE_SET, stringHashSet).apply();
    }

    public void cancelGeofence(String itemKey) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        Set<String> stringHashSet = new HashSet<>();
        stringHashSet = sharedPreferences.getStringSet(Constants.KEY_GEOFENCE_SET, stringHashSet);

        if (!stringHashSet.contains(itemKey)) {
            return;
        }

        mTask = TASK_REMOVE;
        mItemKey = itemKey;

        try {
            removeGeofence(itemKey);
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            showSecurityException(securityException);
        }

//        stringHashSet.remove(itemKey);
//        sharedPreferences.edit().putStringSet(Constants.KEY_GEOFENCE_SET, stringHashSet).apply();
    }

    private PendingIntent getGeofenceTransitionsPendingIntent() {
        Intent intent = new Intent(mContext, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Geofence getGeofence(String itemKey, double latitude, double longitude, int radius) {
        return new Geofence.Builder()
                .setRequestId(itemKey)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setCircularRegion(latitude, longitude, (float) radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setNotificationResponsiveness(DEFAULT_NOTIFICATION_RESPONSIVENESS)
                .build();
    }

    private GeofencingRequest getGeofencingRequest(Geofence geofence) {
        return new GeofencingRequest.Builder()
                .addGeofence(geofence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
    }

    private GeofencingRequest getGeofencingRequest(List<Geofence> geofencesList) {
        return new GeofencingRequest.Builder()
                .addGeofences(geofencesList)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
    }


    private void addGeofence(GeofencingRequest geofencingRequest, PendingIntent pendingIntent) {
        Log.d(TAG, "Adding key = " + mItemKey);
        if (mGeofencingClient != null) {
            mGeofencingClient.addGeofences(geofencingRequest, pendingIntent)
                    .addOnCompleteListener(this);
        }
    }

    private void removeGeofence(String itemKey) {
        Log.d(TAG, "Removing key = " + mItemKey);
        if (mGeofencingClient != null) {
            mGeofencingClient.removeGeofences(Arrays.asList(itemKey))
                    .addOnCompleteListener(this);
        }
    }

    private void removeGeofence(List<String> itemKeyList) {
        Log.d(TAG, "Removing key = " + mItemKey);
        if (mGeofencingClient != null) {
            mGeofencingClient.removeGeofences(itemKeyList)
                    .addOnCompleteListener(this);
        }
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            Set<String> stringHashSet = new HashSet<>();
            stringHashSet = sharedPreferences.getStringSet(Constants.KEY_GEOFENCE_SET, stringHashSet);

            switch (mTask) {
                case TASK_ADD:
                    stringHashSet.add(mItemKey);
                    sharedPreferences.edit().putStringSet(Constants.KEY_GEOFENCE_SET, stringHashSet).apply();
                    Log.d(TAG, "Successful added geofence, key = " + mItemKey);
                    break;
                case TASK_REMOVE:
                    stringHashSet.remove(mItemKey);
                    sharedPreferences.edit().putStringSet(Constants.KEY_GEOFENCE_SET, stringHashSet).apply();
                    Log.d(TAG, "Successful removed geofence, key = " + mItemKey);
                    break;
                default:
            }
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = task.getException().getMessage();
            Log.d(TAG, "Error " + (mTask == TASK_ADD ? "adding" : "removing") + ", key = " + mItemKey + ", " + errorMessage);
        }
    }

    private void showSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }
}
