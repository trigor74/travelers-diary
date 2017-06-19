package com.travelersdiary.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.travelersdiary.Constants;
import com.travelersdiary.models.LocationPoint;
import com.travelersdiary.models.ReminderItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GeofenceSetterService extends Service implements
        OnCompleteListener<Void> {

    private static final String TAG = "GeofenceSetterService";

    private static final String ACTION_SET_GEOFENCE = "ACTION_SET_GEOFENCE";
    private static final String ACTION_CANCEL_GEOFENCE = "ACTION_CANCEL_GEOFENCE";

    private static final String EXTRA_LOCATION_POINT = "EXTRA_LOCATION_POINT";
    private static final String EXTRA_RADIUS = "EXTRA_RADIUS";
    private static final String EXTRA_ITEM_KEY = "EXTRA_ITEM_KEY";

    private static final int DEFAULT_RADIUS = 500;
    private static final int DEFAULT_NOTIFICATION_RESPONSIVENESS = 2000; // 2 second

    private PendingIntent mPendingIntent = null;
    private GeofencingClient mGeofencingClient;
    private ArrayList<Geofence> mGeofenceList;

    public GeofenceSetterService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void setGeofence(Context context, ReminderItem reminderItem, String itemKey) {
        Intent intent = new Intent(context, GeofenceSetterService.class);
        intent.setAction(ACTION_SET_GEOFENCE);
        intent.putExtra(EXTRA_ITEM_KEY, itemKey);
        intent.putExtra(EXTRA_LOCATION_POINT, reminderItem.getWaypoint().getLocation());
        intent.putExtra(EXTRA_RADIUS, reminderItem.getDistance());
        context.startService(intent);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> stringHashSet = new HashSet<>();
        stringHashSet = sharedPreferences.getStringSet(Constants.KEY_GEOFENCE_SET, stringHashSet);
        stringHashSet.add(itemKey);
        sharedPreferences.edit().putStringSet(Constants.KEY_GEOFENCE_SET, stringHashSet).apply();

        intent = new Intent(context, LocationTrackingService.class);
        intent.setAction(LocationTrackingService.ACTION_START_GEOFENCE_LOCATION_UPDATES);
        context.startService(intent);
    }

    public static void cancelGeofence(Context context, String itemKey) {
        Intent intent = new Intent(context, GeofenceSetterService.class);
        intent.setAction(ACTION_CANCEL_GEOFENCE);
        intent.putExtra(EXTRA_ITEM_KEY, itemKey);
        context.startService(intent);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> stringHashSet = new HashSet<>();
        stringHashSet = sharedPreferences.getStringSet(Constants.KEY_GEOFENCE_SET, stringHashSet);
        stringHashSet.remove(itemKey);
        sharedPreferences.edit().putStringSet(Constants.KEY_GEOFENCE_SET, stringHashSet).apply();

        if (stringHashSet.isEmpty()) {
            intent = new Intent(context, LocationTrackingService.class);
            intent.setAction(LocationTrackingService.ACTION_STOP_GEOFENCE_LOCATION_UPDATES);
            context.startService(intent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGeofencingClient = LocationServices.getGeofencingClient(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            stopSelf();
            return START_NOT_STICKY;
        } else {
            String action = intent.getAction();
            switch (action) {
                case ACTION_SET_GEOFENCE:
                    LocationPoint locationPoint = (LocationPoint) intent.getSerializableExtra(GeofenceSetterService.EXTRA_LOCATION_POINT);
                    int radius = intent.getIntExtra(EXTRA_RADIUS, DEFAULT_RADIUS);
                    String itemKey = intent.getStringExtra(EXTRA_ITEM_KEY);
                    PendingIntent pendingIntent = getGeofencePendingIntent();
                    GeofencingRequest geofencingRequest = getGeofencingRequest(getGeofence(itemKey, locationPoint.getLatitude(), locationPoint.getLongitude(), radius));
                    try {
                        addGeofence(geofencingRequest, pendingIntent);
                    } catch (SecurityException securityException) {
                        // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
                        showSecurityException(securityException);
                    }
                    break;
                case ACTION_CANCEL_GEOFENCE:
                    String deleteKey = intent.getStringExtra(EXTRA_ITEM_KEY);
                    try {
                        removeGeofence(deleteKey);
                    } catch (SecurityException securityException) {
                        // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
                        showSecurityException(securityException);
                    }
                    break;
            }
        }
        return START_STICKY;
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mPendingIntent == null) {
            Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
            mPendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return mPendingIntent;
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
        if (mGeofencingClient != null) {
            mGeofencingClient.addGeofences(geofencingRequest, pendingIntent)
                    .addOnCompleteListener(this);
        }
    }

    private void removeGeofence(String itemKey) {
        if (mGeofencingClient != null) {
            mGeofencingClient.removeGeofences(new ArrayList<String>(Arrays.asList(itemKey)))
                    .addOnCompleteListener(this);
        }
    }

    private void removeGeofence(List<String> itemKeyList) {
        if (mGeofencingClient != null) {
            mGeofencingClient.removeGeofences(itemKeyList)
                    .addOnCompleteListener(this);
        }
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()) {
            Log.i(TAG, "Geofence added/removed");
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = task.getException().getMessage();
            Log.w(TAG, "Error add/remove geofence:" + errorMessage);
        }
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void showSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }
}
