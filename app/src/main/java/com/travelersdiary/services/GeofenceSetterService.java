package com.travelersdiary.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.travelersdiary.Constants;
import com.travelersdiary.models.LocationPoint;
import com.travelersdiary.models.ReminderItem;

import java.util.HashMap;
import java.util.Map;

public class GeofenceSetterService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status>,
        LocationListener { // for getting current position

    private static final String TAG = "GeofenceSetterService";

    private static final String ACTION_SET_GEOFENCE = "ACTION_SET_GEOFENCE";
    private static final String ACTION_CANCEL_GEOFENCE = "ACTION_CANCEL_GEOFENCE";

    private static final String EXTRA_UID = "EXTRA_UID";
    private static final String EXTRA_TITLE = "EXTRA_TITLE";
    private static final String EXTRA_LOCATION_POINT = "EXTRA_LOCATION_POINT";
    private static final String EXTRA_LOCATION_TITLE = "EXTRA_LOCATION_TITLE";
    private static final String EXTRA_RADIUS = "EXTRA_RADIUS";

    private static final int DEFAULT_RADIUS = 500;
    private static final int DEFAULT_NOTIFICATION_RESPONSIVENESS = 2000; // 2 second

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private Map<PendingIntent, GeofencingRequest> mGeofencingRequestsMap = new HashMap<>();

    public GeofenceSetterService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void setGeofence(Context context, ReminderItem reminderItem) {
        Intent intent = new Intent(context, GeofenceSetterService.class);
        intent.setAction(ACTION_SET_GEOFENCE);
        intent.putExtra(EXTRA_UID, reminderItem.getUID());
        intent.putExtra(EXTRA_TITLE, reminderItem.getTitle());
        intent.putExtra(EXTRA_LOCATION_TITLE, reminderItem.getWaypoint().getTitle());
        intent.putExtra(EXTRA_LOCATION_POINT, reminderItem.getWaypoint().getLocation());
        intent.putExtra(EXTRA_RADIUS, reminderItem.getDistance());
        context.startService(intent);
    }

    public static void cancelGeofence(Context context, ReminderItem reminderItem) {
        Intent intent = new Intent(context, GeofenceSetterService.class);
        intent.setAction(ACTION_CANCEL_GEOFENCE);
        intent.putExtra(EXTRA_UID, reminderItem.getUID());
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        buildGoogleApiClient();

        // LocationRequest for getting current position
        mLocationRequest = new LocationRequest();
        // We want a location update every 5 seconds.
        mLocationRequest.setInterval(5000);
        // We want the location to be as accurate as possible.
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (isGooglePlayServicesAvailable()) {
            mGoogleApiClient.connect();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
            return START_STICKY;
        }

        String action = intent.getAction();
        if (action == null) {
            return START_STICKY;
        }

        final int uid = intent.getIntExtra(EXTRA_UID, 0);

        switch (action) {
            case ACTION_SET_GEOFENCE:
                String title = intent.getStringExtra(EXTRA_TITLE);
                String locationTitle = intent.getStringExtra(EXTRA_LOCATION_TITLE);
                LocationPoint locationPoint = (LocationPoint) intent.getSerializableExtra(GeofenceSetterService.EXTRA_LOCATION_POINT);
                int radius = intent.getIntExtra(EXTRA_RADIUS, DEFAULT_RADIUS);
                PendingIntent pendingIntent = getGeofencePendingIntent(uid, title, locationTitle);
                GeofencingRequest geofencingRequest = getGeofencingRequest(getGeofence(uid, locationPoint.getLatitude(), locationPoint.getLongitude(), radius));
                try {
                    addGeofence(pendingIntent, geofencingRequest);
                } catch (SecurityException securityException) {
                    // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
                    showSecurityException(securityException);
                }
                break;
            case ACTION_CANCEL_GEOFENCE:
                PendingIntent pendingIntentRemove = getGeofencePendingIntent(uid, null, null);
                try {
                    removeGeofence(pendingIntentRemove);
                } catch (SecurityException securityException) {
                    // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
                    showSecurityException(securityException);
                }
                break;
        }

        return START_STICKY;
    }

    private PendingIntent getGeofencePendingIntent(int uid, String title, String locationTitle) {
        Intent intent = new Intent(this, NotificationIntentService.class);
        intent.putExtra(NotificationIntentService.KEY_UID, uid);
        intent.putExtra(NotificationIntentService.KEY_TYPE, Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_LOCATION);
        intent.putExtra(NotificationIntentService.KEY_TITLE, title);
        intent.putExtra(NotificationIntentService.KEY_LOCATION_TITLE, locationTitle);
        return PendingIntent.getService(this, uid, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Geofence getGeofence(int uid, double latitude, double longitude, int radius) {
        return new Geofence.Builder()
                .setRequestId(Integer.toString(uid))
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

    private void addGeofence(PendingIntent pendingIntent, GeofencingRequest geofencingRequest) {
        if (!mGeofencingRequestsMap.containsKey(pendingIntent)) {
            mGeofencingRequestsMap.put(pendingIntent, geofencingRequest);
            if (mGoogleApiClient.isConnected()) {
                LocationServices.GeofencingApi.addGeofences(
                        mGoogleApiClient,
                        geofencingRequest,
                        pendingIntent
                ).setResultCallback(this);
            }
        }
    }

    private void removeGeofence(PendingIntent pendingIntent) {
        if (mGeofencingRequestsMap.containsKey(pendingIntent)) {
            mGeofencingRequestsMap.remove(pendingIntent);
            if (mGoogleApiClient.isConnected()) {
                LocationServices.GeofencingApi.removeGeofences(
                        mGoogleApiClient,
                        pendingIntent
                ).setResultCallback(this);
            }
        }
        if (mGeofencingRequestsMap.isEmpty()) {
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            }
            stopSelf();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");
        try {
            // requestLocationUpdates for getting current position
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);

            for (Map.Entry<PendingIntent, GeofencingRequest> entry :
                    mGeofencingRequestsMap.entrySet()) {
                addGeofence(entry.getKey(), entry.getValue());
            }
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            showSecurityException(securityException);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason.
        Log.i(TAG, "Connection suspended");
        // onConnected() will be called again automatically when the service reconnects
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    // Runs when the result of calling addGeofences() and removeGeofences() becomes available.
    // Either method can complete successfully or with an error.
    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Log.i(TAG, "Geofences added/removed");
        } else {
            Log.e(TAG, "Error add/remove geofences: GeofenceStatusCode = " + status.getStatusCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // for getting current position
        Log.v(TAG, "Location Information\n"
                + "==========\n"
                + "Provider:\t" + location.getProvider() + "\n"
                + "Lat & Long:\t" + location.getLatitude() + ", "
                + location.getLongitude() + "\n"
                + "Altitude:\t" + location.getAltitude() + "\n"
                + "Bearing:\t" + location.getBearing() + "\n"
                + "Speed:\t\t" + location.getSpeed() + "\n"
                + "Accuracy:\t" + location.getAccuracy() + "\n");
    }

    @Override
    public void onDestroy() {
        try {
            for (Map.Entry<PendingIntent, GeofencingRequest> entry :
                    mGeofencingRequestsMap.entrySet()) {
                removeGeofence(entry.getKey());
            }
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            showSecurityException(securityException);
        }
        mGeofencingRequestsMap.clear();
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(getApplicationContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                Log.e(TAG, "Google Play Services not available");
            }
            return false;
        }
        return true;
    }

    private void showSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }
}
