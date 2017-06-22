package com.travelersdiary.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

public class OnLocationChangedReceiver extends BroadcastReceiver {
    private static final String TAG = "OnLocationChangedBR";
    public static final String ACTION_TRACKING = "ACTION_TRACKING";
    public static final String ACTION_SINGLE_LOCATION_REQUEST = "ACTION_SINGLE_LOCATION_REQUEST";
    public static final String ACTION_GEOFENCE_LOCATION_UPDATES = "ACTION_GEOFENCE_LOCATION_UPDATES";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        if (intent == null) {
            return;
        }

        String action = intent.getAction();
        Log.d(TAG, "action = " + action);

        LocationResult locationResult = LocationResult.extractResult(intent);
        if (locationResult == null) {
            Log.d(TAG, "LocationResult is null");
            return;
        }

        Location location = locationResult.getLastLocation();
        // List<Location> locations = locationResult.getLocations();
        Log.d(TAG, "location = " + location.toString());

        switch (action) {
            case ACTION_TRACKING:
                break;
            case ACTION_GEOFENCE_LOCATION_UPDATES: // do nothing
            default:
        }
    }
}
