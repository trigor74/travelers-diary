package com.travelersdiary.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.bus.BusProvider;
import com.travelersdiary.models.LocationPoint;
import com.travelersdiary.screens.main.MainActivity;

public class LocationTrackingService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public class CheckTrackingEvent {
        public boolean isTrackingEnabled;

        public CheckTrackingEvent(boolean isTrackingEnabled) {
            this.isTrackingEnabled = isTrackingEnabled;
        }
    }

    private static final String TAG = "LocationTrackingService";
    public static final String ACTION_START_TRACK = "ACTION_START_TRACK";
    public static final String ACTION_STOP_TRACK = "ACTION_STOP_TRACK";
    public static final String ACTION_GET_CURRENT_LOCATION = "ACTION_GET_CURRENT_LOCATION";
    public static final String ACTION_CHECK_TRACKING = "ACTION_CHECK_TRACKING";
    public static final String ACTION_START_GEOFENCE_LOCATION_UPDATES = "ACTION_START_GEOFENCE_LOCATION_UPDATES";
    public static final String ACTION_STOP_GEOFENCE_LOCATION_UPDATES = "ACTION_STOP_GEOFENCE_LOCATION_UPDATES";
    private static final int ONGOING_NOTIFICATION_ID = 1002;

    private boolean isRequestingLocationUpdates = false;
    private boolean isTrackingEnabled = false;
    private boolean isSingleRequestLocation = false;
    private boolean isGeofenceTrackingEnabled = false;
    private boolean isForeground = false;
    private String mUserUID;
    private String mTravelId;
    private Firebase mTrackRef = null;

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 5;
    public static final long SMALLEST_DISPLACEMENT_IN_METERS = 15;

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
//        if (isGooglePlayServicesAvailable()) {
//            mGoogleApiClient.connect();
//        }

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
        mLocationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT_IN_METERS);
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * track: users/USER_UID/tracks/TRAVEL_UID/TRACK_UID/track/[TIMESTAMP:LOCATION_POINT]
     * track/[TIMESTAMP:LOCATION_POINT] - TrackList class
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            stopSelf();
            return Service.START_STICKY;
        }

        String action = intent.getAction();

        switch (action) {
            case ACTION_GET_CURRENT_LOCATION:
            case ACTION_START_TRACK:
            case ACTION_START_GEOFENCE_LOCATION_UPDATES:
                if (isGooglePlayServicesAvailable() && !mGoogleApiClient.isConnecting()) {
                    mGoogleApiClient.connect();
                }
                break;
            default:
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
                        startLocationUpdates();
                    }
                }
                break;
            case ACTION_START_TRACK:
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                mTravelId = sharedPreferences.getString(Constants.KEY_ACTIVE_TRAVEL_KEY, null);
                if (mTravelId == null) {
                    mTravelId = Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY;
                }
                isTrackingEnabled = true;

                Firebase userTracksRef = new Firebase(Utils.getFirebaseUserTracksUrl(mUserUID));
                Firebase newTrackRef = userTracksRef.child(mTravelId).push();
                mTrackRef = newTrackRef.child(Constants.FIREBASE_TRACKS_TRACK);

                isForeground = true;
                startForeground(ONGOING_NOTIFICATION_ID, buildForegroundNotification());

                startLocationUpdates();
                break;
            case ACTION_STOP_TRACK:
                isTrackingEnabled = false;
                mTrackRef = null;

                if (isForeground && !isGeofenceTrackingEnabled) {
                    isForeground = false;
                    stopForeground(true);
                } else {
                    // start again for update notification
                    startForeground(ONGOING_NOTIFICATION_ID, buildForegroundNotification());
                }

                stopLocationUpdates();

                break;
            case ACTION_CHECK_TRACKING:
                BusProvider.bus().post(new CheckTrackingEvent(isTrackingEnabled));
                break;
            case ACTION_START_GEOFENCE_LOCATION_UPDATES:
                if (!isGeofenceTrackingEnabled) {
                    isGeofenceTrackingEnabled = true;

                    isForeground = true;
                    startForeground(ONGOING_NOTIFICATION_ID, buildForegroundNotification());

                    startLocationUpdates();
                }
                break;
            case ACTION_STOP_GEOFENCE_LOCATION_UPDATES:
                isGeofenceTrackingEnabled = false;

                if (isForeground && !isTrackingEnabled) {
                    isForeground = false;
                    stopForeground(true);
                } else {
                    // start again for update notification
                    startForeground(ONGOING_NOTIFICATION_ID, buildForegroundNotification());
                }

                stopLocationUpdates();
                break;
            default:
                stopSelf();
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
            }
        }

        if (isSingleRequestLocation || isTrackingEnabled || isGeofenceTrackingEnabled) {
            startLocationUpdates();
        } else {
            stopLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (isTrackingEnabled || isSingleRequestLocation || isGeofenceTrackingEnabled) {
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
        if (mGoogleApiClient.isConnected() && !isRequestingLocationUpdates) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            isRequestingLocationUpdates = true;
        }
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()
                && !(isSingleRequestLocation || isTrackingEnabled || isGeofenceTrackingEnabled)
                && isRequestingLocationUpdates) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            isRequestingLocationUpdates = false;
        }
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
            }
        }

        if (!isSingleRequestLocation || !isTrackingEnabled || !isGeofenceTrackingEnabled) {
            stopLocationUpdates();
        }
    }

    private void sendCurrentLocation() {
        if (mCurrentLocation != null) {
            LocationPoint location = new LocationPoint(mCurrentLocation.getLatitude(),
                    mCurrentLocation.getLongitude(),
                    mCurrentLocation.getAltitude());
            BusProvider.bus().post(location);
        }
        stopLocationUpdates();
    }

    private void saveCurrentTrackPoint() {
        if (mCurrentLocation != null) {
            LocationPoint point = new LocationPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), mCurrentLocation.getAltitude());
            if (mTrackRef != null) {
                mTrackRef.child(String.valueOf(mLastUpdateTimestamp)).setValue(point);
            }
        }
    }

    private Notification buildForegroundNotification() {
        // TODO: 19.06.17 move strings to res/values
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher);
        String text = isTrackingEnabled ? "Save track" : "";
        text = text + (isGeofenceTrackingEnabled ? (isTrackingEnabled ? ", t" : "T") + "rack geofence" : "");
        return new Notification.Builder(this)
                .setContentTitle("Traveler\'s Diary")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_pin_white_24dp)
                .setLargeIcon(icon)
                .setTicker("Start location requests")
                .setContentIntent(pendingIntent)
                .build();
    }

    @Override
    public void onDestroy() {
        if (isForeground) {
            stopForeground(true);
        }
        if (isRequestingLocationUpdates) {
            stopLocationUpdates();
        }
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }
}
