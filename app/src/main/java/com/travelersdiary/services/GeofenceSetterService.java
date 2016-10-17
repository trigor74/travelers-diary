package com.travelersdiary.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
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
import com.google.android.gms.location.LocationServices;
import com.travelersdiary.models.LocationPoint;

public class GeofenceSetterService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status> {

    private static final String TAG = "GeofenceSetterService";

    private static final String ACTION_SET_GEOFENCE = "ACTION_SET_GEOFENCE";
    private static final String ACTION_CANCEL_GEOFENCE = "ACTION_CANCEL_GEOFENCE";

    private static final String EXTRA_UID = "EXTRA_UID";
    private static final String EXTRA_TITLE = "EXTRA_TITLE";
    private static final String EXTRA_LOCATION_POINT = "EXTRA_LOCATION_POINT";
    private static final String EXTRA_LOCATION_TITLE = "EXTRA_LOCATION_TITLE";
    private static final String EXTRA_RADIUS = "EXTRA_RADIUS";

    private static final int DEFAULT_RADIUS = 500;

    private GoogleApiClient mGoogleApiClient;

    public GeofenceSetterService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        buildGoogleApiClient();
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

        // TODO: 18.10.16 !!! add CHECK GoogleApiConnection and add logic for not connected client
        final int uid = intent.getIntExtra(EXTRA_UID, 0);

        switch (action) {
            case ACTION_SET_GEOFENCE:
                String title = intent.getStringExtra(EXTRA_TITLE);
                String locationTitle = intent.getStringExtra(EXTRA_LOCATION_TITLE);
                LocationPoint locationPoint = (LocationPoint) intent.getSerializableExtra(GeofenceSetterService.EXTRA_LOCATION_POINT);
                int radius = intent.getIntExtra(EXTRA_RADIUS, DEFAULT_RADIUS);

                addGeofence(uid, title, locationTitle, locationPoint, radius);
                break;
            case ACTION_CANCEL_GEOFENCE:
                removeGeofence(uid);
                break;
        }

        return START_STICKY;
    }

    private PendingIntent getGeofencePendingIntent(int uid, String title, String locationTitle) {
        Intent intent = new Intent(this, NotificationIntentService.class);
        intent.putExtra(NotificationIntentService.KEY_TITLE, title);
        // TODO: 18.10.16 add extra logic for geofences to NotificationIntentService
        //intent.putExtra(NotificationIntentService.KEY_LOCATION_TITLE, locationTitle);
        return PendingIntent.getService(this, uid, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Geofence getGeofence(int uid, double latitude, double longitude, int radius) {
        return new Geofence.Builder()
                .setRequestId(Integer.toString(uid))
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setCircularRegion(latitude, longitude, (float) radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    private GeofencingRequest getGeofencingRequest(Geofence geofence) {
        return new GeofencingRequest.Builder()
                .addGeofence(geofence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
    }

    private void addGeofence(int uid, String title, String locationTitle, LocationPoint locationPoint, int radius) {
        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                getGeofencingRequest(getGeofence(uid, locationPoint.getLatitude(), locationPoint.getLongitude(), radius)),
                getGeofencePendingIntent(uid, title, locationTitle)
        ).setResultCallback(this);
    }

    private void removeGeofence(int uid) {
        LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient,
                getGeofencePendingIntent(uid, null, null)
        ).setResultCallback(this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");
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
}
