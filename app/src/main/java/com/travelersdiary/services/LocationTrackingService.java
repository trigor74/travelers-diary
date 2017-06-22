package com.travelersdiary.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;

import com.firebase.client.Firebase;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.bus.BusProvider;
import com.travelersdiary.models.LocationPoint;
import com.travelersdiary.screens.main.MainActivity;

public class LocationTrackingService extends Service {

    public class CheckTrackingEvent {
        public boolean isTrackingEnabled;

        public CheckTrackingEvent(boolean isTrackingEnabled) {
            this.isTrackingEnabled = isTrackingEnabled;
        }
    }

    private static final String TAG = "LocationTrackingService";
    public static final String ACTION_START_TRACK = "ACTION_START_TRACK";
    public static final String ACTION_STOP_TRACK = "ACTION_STOP_TRACK";
    public static final String ACTION_PAUSE_RESUME_TRACK = "ACTION_PAUSE_RESUME_TRACK";
    public static final String ACTION_CHECK_TRACKING = "ACTION_CHECK_TRACKING";
    public static final String EXTRA_FROM_NOTIFICATION = "EXTRA_FROM_NOTIFICATION";
    private static final int ONGOING_NOTIFICATION_ID = 1002;

    private boolean isRequestingLocationUpdates = false;
    private boolean isTrackingEnabled = false;
    private boolean isPause = false;
    private String mUserUID;
    private String mTravelId;
    private Firebase mTrackRef = null;

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 5;
    public static final long SMALLEST_DISPLACEMENT_IN_METERS = 15;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
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

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onLocationChanged(locationResult.getLastLocation());
            }
        };
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
        if (intent != null && intent.getAction() != null) {

            String action = intent.getAction();

            switch (action) {
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

                    startForeground(ONGOING_NOTIFICATION_ID, buildForegroundNotification());

                    startLocationUpdates();
                    break;
                case ACTION_STOP_TRACK:
                    isTrackingEnabled = false;
                    if (intent.getBooleanExtra(EXTRA_FROM_NOTIFICATION, false)) {
                        BusProvider.bus().post(new CheckTrackingEvent(isTrackingEnabled));
                    }
                    mTrackRef = null;

                    stopForeground(true);

                    stopLocationUpdates();
                    stopSelf();
                    break;
                case ACTION_PAUSE_RESUME_TRACK:
                    isPause = !isPause;
                    if (isPause) {
                        stopLocationUpdates();
                    } else {
                        startLocationUpdates();
                    }
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(ONGOING_NOTIFICATION_ID, buildForegroundNotification());
                    break;
                case ACTION_CHECK_TRACKING:
                    BusProvider.bus().post(new CheckTrackingEvent(isTrackingEnabled));
                    break;
                default:
            }
        }
        return Service.START_STICKY;
    }

    protected void startLocationUpdates() {
        if (mFusedLocationProviderClient != null
                && !isRequestingLocationUpdates) {
            mFusedLocationProviderClient
                    .requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            isRequestingLocationUpdates = true;
        }
    }

    protected void stopLocationUpdates() {
        if (mFusedLocationProviderClient != null
                && isRequestingLocationUpdates) {
            mFusedLocationProviderClient
                    .removeLocationUpdates(mLocationCallback);
            isRequestingLocationUpdates = false;
        }
    }

    private void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTimestamp = System.currentTimeMillis();
        saveCurrentTrackPoint();
    }

    private void saveCurrentTrackPoint() {
        if (!isPause && isTrackingEnabled && mCurrentLocation != null) {
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

        Intent stopIntent = new Intent(this, LocationTrackingService.class);
        stopIntent.setAction(ACTION_STOP_TRACK);
        stopIntent.putExtra(EXTRA_FROM_NOTIFICATION, true);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this, LocationTrackingService.class);
        pauseIntent.setAction(ACTION_PAUSE_RESUME_TRACK);
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher);
        String text = isTrackingEnabled ? "Location tracking" : "Location not tracking";
        if (isPause) {
            text = "Location tracking is paused";
        }
        return new NotificationCompat.Builder(this)
                .setContentTitle("Traveler\'s Diary")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_pin_white_24dp)
                .setLargeIcon(icon)
                .setTicker("Start location tracking")
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_MAX)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle("Traveler\'s Diary")
                        .bigText(text))
                .addAction(R.drawable.ic_action_stop_black_24dp, "Stop", stopPendingIntent)
                .addAction(
                        (isPause ? R.drawable.ic_action_play_black_24dp :
                                R.drawable.ic_action_pause_black_24dp),
                        (isPause ? "Resume" : "Pause"),
                        pausePendingIntent)
                .build();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        if (isRequestingLocationUpdates) {
            stopLocationUpdates();
        }
        super.onDestroy();
    }
}
