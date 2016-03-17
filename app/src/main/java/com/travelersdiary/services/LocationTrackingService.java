package com.travelersdiary.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.travelersdiary.Constants;
import com.travelersdiary.bus.BusProvider;
import com.travelersdiary.models.LocationPoint;

public class LocationTrackingService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LocationTrackingService";
    public static final String ACTION_START_TRACK = "ACTION_START_TRACK";
    public static final String ACTION_STOP_TRACK = "ACTION_STOP_TRACK";
    public static final String ACTION_GET_CURRENT_LOCATION = "ACTION_GET_CURRENT_LOCATION";

    private boolean isTracking = false;
    private String mUserUID;
    private String mTravelId;
    private String mTrackId;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    protected Location mCurrentLocation;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mUserUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        if (checkPlayServices()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    /**
     * tracks:    users/USER_UID/travels/TRAVEL_UID/tracks/[TRACK_UID]
     * trackpoints: users/USER_UID/tracks/[TRACK_UID]/[timestamp:[location]]
     * Extras:
     * Constants.KEY_TRAVEL_KEY - TRAVEL_UID
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand, flag: " + flags + "startId: " + startId);
        String action = intent.getAction();

        Log.i(TAG, "onStartCommand, action: " + action);
        Log.i(TAG, "onStartCommand, travel id: " + mTravelId);

        switch (action) {
            case ACTION_GET_CURRENT_LOCATION:
                // TODO: 17.03.2016 add logic for get current location to mCurrentLocation
                //test>
                // 49.415781, 32.066044
                // 48.957014,32.146055
                // 49.8327787,23.942196
                if (mCurrentLocation == null) {
                    mCurrentLocation = new Location("");
                }
                mCurrentLocation.setLatitude(49.8327787);
                mCurrentLocation.setLongitude(23.942196);
                mCurrentLocation.setAltitude(0);
                //<test

                sendCurrentLocation();
                if (!isTracking){
                    stopSelf();
                }
                break;
            case ACTION_START_TRACK:
                mTravelId = intent.getStringExtra(Constants.KEY_TRAVEL_KEY);
                isTracking = true;
                // TODO: 17.03.2016 add start tracking logic
                break;
            case ACTION_STOP_TRACK:
                // TODO: 17.03.2016 add stop tracking logic
                isTracking = false;
                stopSelf();
                break;
            default:
        }


        return Service.START_STICKY;
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private boolean checkPlayServices() {
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

    private void sendCurrentLocation() {
        if (mCurrentLocation != null) {
            LocationPoint location = new LocationPoint(mCurrentLocation.getLatitude(),
                    mCurrentLocation.getLongitude(),
                    mCurrentLocation.getAltitude());
            BusProvider.bus().post(location);
        }
    }
}
