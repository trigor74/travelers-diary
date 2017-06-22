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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
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

// TODO: 22.06.17 change to IntentService
public class GeofenceSetterService extends Service implements
        OnCompleteListener<Void> {

    private static final String TAG = "GeofenceSetterService";

    private static final String ACTION_SET_GEOFENCE = "ACTION_SET_GEOFENCE";
    private static final String ACTION_CANCEL_GEOFENCE = "ACTION_CANCEL_GEOFENCE";
    public static final String ACTION_START_LOCATION_UPDATES = "ACTION_START_LOCATION_UPDATES";
    public static final String ACTION_STOP_LOCATION_UPDATES = "ACTION_STOP_LOCATION_UPDATES";

    private static final String EXTRA_LOCATION_POINT = "EXTRA_LOCATION_POINT";
    private static final String EXTRA_RADIUS = "EXTRA_RADIUS";
    private static final String EXTRA_ITEM_KEY = "EXTRA_ITEM_KEY";

    private static final int DEFAULT_RADIUS = 500;
    private static final int DEFAULT_NOTIFICATION_RESPONSIVENESS = 2000; // 2 second

    private GeofencingClient mGeofencingClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private ArrayList<Geofence> mGeofenceList;

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 5;
    public static final long SMALLEST_DISPLACEMENT_IN_METERS = 15;

    private int mStartServiceCount = 0;

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

        intent = new Intent(context, GeofenceSetterService.class);
        intent.setAction(GeofenceSetterService.ACTION_START_LOCATION_UPDATES);
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
            intent = new Intent(context, GeofenceSetterService.class);
            intent.setAction(GeofenceSetterService.ACTION_STOP_LOCATION_UPDATES);
            context.startService(intent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGeofencingClient = LocationServices.getGeofencingClient(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void stopTheService() {
        mStartServiceCount = mStartServiceCount - 1;
        if (mStartServiceCount < 1) {
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mStartServiceCount = mStartServiceCount + 1;
        if (intent == null || intent.getAction() == null) {
            stopTheService();
        } else {
            String action = intent.getAction();
            switch (action) {
                case ACTION_SET_GEOFENCE:
                    LocationPoint locationPoint = (LocationPoint) intent.getSerializableExtra(GeofenceSetterService.EXTRA_LOCATION_POINT);
                    int radius = intent.getIntExtra(EXTRA_RADIUS, DEFAULT_RADIUS);
                    String itemKey = intent.getStringExtra(EXTRA_ITEM_KEY);
                    PendingIntent pendingIntent = getGeofenceTransitionsPendingIntent();
                    GeofencingRequest geofencingRequest = getGeofencingRequest(getGeofence(itemKey, locationPoint.getLatitude(), locationPoint.getLongitude(), radius));
                    try {
                        addGeofence(geofencingRequest, pendingIntent);
                    } catch (SecurityException securityException) {
                        // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
                        showSecurityException(securityException);
                        stopTheService();
                    }
                    break;
                case ACTION_CANCEL_GEOFENCE:
                    String deleteKey = intent.getStringExtra(EXTRA_ITEM_KEY);
                    try {
                        removeGeofence(deleteKey);
                    } catch (SecurityException securityException) {
                        // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
                        showSecurityException(securityException);
                        stopTheService();
                    }
                    break;
                case ACTION_START_LOCATION_UPDATES:
                    startLocationUpdates();
                    break;
                case ACTION_STOP_LOCATION_UPDATES:
                    stopLocationUpdates();
                    break;
                default:
                    stopTheService();
            }
        }
        return START_STICKY;
    }

    private PendingIntent getGeofenceTransitionsPendingIntent() {
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getLocationUpdatePendingIntent() {
        Intent intent = new Intent(getApplicationContext(), OnLocationChangedReceiver.class);
        intent.setAction(OnLocationChangedReceiver.ACTION_GEOFENCE_LOCATION_UPDATES);
        return PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
        Log.d(TAG, "Geofence adding");
        if (mGeofencingClient != null) {
            mGeofencingClient.addGeofences(geofencingRequest, pendingIntent)
                    .addOnCompleteListener(this);
        }
    }

    private void removeGeofence(String itemKey) {
        Log.d(TAG, "Geofence removing");
        if (mGeofencingClient != null) {
            mGeofencingClient.removeGeofences(Arrays.asList(itemKey))
                    .addOnCompleteListener(this);
        }
    }

    private void removeGeofence(List<String> itemKeyList) {
        Log.d(TAG, "Geofence removing");
        if (mGeofencingClient != null) {
            mGeofencingClient.removeGeofences(itemKeyList)
                    .addOnCompleteListener(this);
        }
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()) {
            Log.d(TAG, "Successful");
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = task.getException().getMessage();
            Log.d(TAG, "Error:" + errorMessage);
        }
        stopTheService();
    }

    private void startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates");
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT_IN_METERS);
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (mFusedLocationProviderClient != null) {
            mFusedLocationProviderClient
                    .requestLocationUpdates(locationRequest, getLocationUpdatePendingIntent())
                    .addOnCompleteListener(this);
        }

        stopTheService();
    }

    private void stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates");
        if (mFusedLocationProviderClient != null) {
            mFusedLocationProviderClient
                    .removeLocationUpdates(getLocationUpdatePendingIntent())
                    .addOnCompleteListener(this);
        }

        stopTheService();
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
