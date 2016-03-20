package com.travelersdiary.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.firebase.client.Firebase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.travelersdiary.Constants;
import com.travelersdiary.Utils;
import com.travelersdiary.bus.BusProvider;
import com.travelersdiary.models.LocationPoint;

import java.util.HashMap;
import java.util.Map;

public class LocationTrackingService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "LocationTrackingService";
    public static final String ACTION_START_TRACK = "ACTION_START_TRACK";
    public static final String ACTION_STOP_TRACK = "ACTION_STOP_TRACK";
    public static final String ACTION_GET_CURRENT_LOCATION = "ACTION_GET_CURRENT_LOCATION";

    private boolean isRequestingLocationUpdates = false;
    private boolean isTrackingEnabled = false;
    private boolean isSingleRequestLocation = false;
    private String mUserUID;
    private String mTravelId;
    private Firebase mTrackRef = null;

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private long mLastUpdateTimestamp;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mUserUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

        buildGoogleApiClient();
        if (isGooglePlayServicesAvailable()) {
            mGoogleApiClient.connect();
        }

        createLocationRequest();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * travel:      users/USER_UID/tracks/TRACK_UID/travelId:TRAVEL_UID
     * trackpoints: users/USER_UID/tracks/TRACK_UID/track/[TIMESTAMP:LOCATION_POINT]
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
            return Service.START_STICKY;
        }

        String action = intent.getAction();
        if (action == null) {
            action = "";
        }

        switch (action) {
            case ACTION_GET_CURRENT_LOCATION:
                isSingleRequestLocation = true;
                if (mGoogleApiClient.isConnected()) {
                    mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    mLastUpdateTimestamp = System.currentTimeMillis();

                    if (mCurrentLocation != null) {
                        isSingleRequestLocation = false;
                        sendCurrentLocation();
                    } else {
                        if (!isRequestingLocationUpdates) {
                            startLocationUpdates();
                        }
                    }
                } else {
                    if (!mGoogleApiClient.isConnecting()) {
                        mGoogleApiClient.connect();
                    }
                }
                break;
            case ACTION_START_TRACK:
                // TODO: 18.03.2016 put travel id to SharedPreferences on change
                //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                //mTravelId = sharedPreferences.getString(Constants.KEY_TRAVEL_REF, null);
                mTravelId = "default"; // TODO: 18.03.2016 remove after testing
                isTrackingEnabled = true;

                Firebase userTracksRef = new Firebase(Utils.getFirebaseUserTracksUrl(mUserUID));
                Firebase newTrackRef = userTracksRef.push();
                Map<String, Object> map = new HashMap<>();
                map.put(Constants.FIREBASE_TRACKS_TRAVELID, mTravelId);
                newTrackRef.setValue(map);
                mTrackRef = newTrackRef.child(Constants.FIREBASE_TRACKS_TRACK);

                if (mGoogleApiClient.isConnected() && !isRequestingLocationUpdates) {
                    startLocationUpdates();
                } else {
                    if (!mGoogleApiClient.isConnecting()) {
                        mGoogleApiClient.connect();
                    }
                }
                break;
            case ACTION_STOP_TRACK:
                isTrackingEnabled = false;
                mTrackRef = null;
                if (mGoogleApiClient.isConnected() && !isSingleRequestLocation && isRequestingLocationUpdates) {
                    stopLocationUpdates();
                }
                break;
            default:
        }

        return Service.START_STICKY;
    }

    @Override
    public void onConnected(Bundle bundle) {
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        mLastUpdateTimestamp = System.currentTimeMillis();

        if (mCurrentLocation != null) {
            if (isSingleRequestLocation) {
                sendCurrentLocation();
                isSingleRequestLocation = false;
            }

            if (isTrackingEnabled) {
                saveCurrentTrackPoint();
                startLocationUpdates();
            } else {
                stopLocationUpdates();
            }
        } else {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (isTrackingEnabled || isSingleRequestLocation) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
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

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        isRequestingLocationUpdates = true;
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        isRequestingLocationUpdates = false;
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTimestamp = System.currentTimeMillis();

        if (mCurrentLocation != null) {
            if (isSingleRequestLocation) {
                sendCurrentLocation();
                isSingleRequestLocation = false;
            }

            if (isTrackingEnabled) {
                saveCurrentTrackPoint();
            } else {
                stopLocationUpdates();
            }
        }
    }

    private void sendCurrentLocation() {
        if (mCurrentLocation != null) {
            LocationPoint location = new LocationPoint(mCurrentLocation.getLatitude(),
                    mCurrentLocation.getLongitude(),
                    mCurrentLocation.getAltitude());
            BusProvider.bus().post(location);
        }
    }

    private void saveCurrentTrackPoint() {
        if (mCurrentLocation != null) {
            LocationPoint point = new LocationPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), mCurrentLocation.getAltitude());
            if (mTrackRef != null) {
                mTrackRef.child(String.valueOf(mLastUpdateTimestamp)).setValue(point);
            }
        }
    }

    @Override
    public void onDestroy() {
        if (isRequestingLocationUpdates) {
            stopLocationUpdates();
        }
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }
}
