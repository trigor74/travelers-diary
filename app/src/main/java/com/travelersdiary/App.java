package com.travelersdiary;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.onegravity.rteditor.fonts.FontManager;
import com.travelersdiary.models.ReminderItem;
import com.travelersdiary.models.Travel;

import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

public class App extends android.app.Application {
    private Query mActiveTravelQuery = null;
    private Query mReminderQuery = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        Firebase.setAndroidContext(this);
        // enable disk persistence
        Firebase.getDefaultConfig().setPersistenceEnabled(true);

        //pre-load fonts for rtEditor
        FontManager.preLoadFonts(this);

        setListeners();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public void setListeners() {
        String userUID = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext())
                .getString(Constants.KEY_USER_UID, null);

        if (userUID == null || userUID.isEmpty()) {
            return;
        }

        if (mActiveTravelQuery == null) {
            mActiveTravelQuery = new Firebase(Utils.getFirebaseUserActiveTravelUrl(userUID));
            mActiveTravelQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map != null) {
                        // CHANGE ACTIVE TRAVEL HERE!!!
                        // switch active travel logic:
                        // 1. disable notifications for active travel 's reminder items
                        // 2. set "active" to false for active travel 's reminder items except for "default"
                        // 3. set "active" to false for active travel
                        // ***
                        // 4. set "active" to true for new travel
                        //    add start time if absent
                        //    clear stop time (set to -1)
                        // 5. set "active" to true for new travel 's reminder items
                        // 6. enable notifications for new travel 's reminder items
                        // 7. put new active travel's key and title to SharedPreferences

                        String newActiveTravelKey = (String) map.get(Constants.FIREBASE_ACTIVE_TRAVEL_KEY);
                        String newActiveTravelTitle = (String) map.get(Constants.FIREBASE_ACTIVE_TRAVEL_TITLE);

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        String userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

                        String oldActiveTravelKey = sharedPreferences.getString(Constants.KEY_ACTIVE_TRAVEL_KEY, null);

                        if (oldActiveTravelKey != null && !oldActiveTravelKey.equals(newActiveTravelKey)) {
                            // 1.
                            // present in reminder data change listener (App.class)

                            // 2.
                            if (!Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY.equals(oldActiveTravelKey)) {
                                Query query = new Firebase(Utils.getFirebaseUserReminderUrl(userUID))
                                        .orderByChild(Constants.FIREBASE_REMINDER_TRAVELID)
                                        .equalTo(oldActiveTravelKey);
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Map<String, Object> map = new HashMap<String, Object>();
                                        map.put(Constants.FIREBASE_REMINDER_ACTIVE, false);
                                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                                            child.getRef().updateChildren(map);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(FirebaseError firebaseError) {
                                    }
                                });
                            }

                            // 3.
                            Map<String, Object> activeTravelMap = new HashMap<String, Object>();
                            activeTravelMap.put(Constants.FIREBASE_TRAVEL_ACTIVE, false);
                            new Firebase(Utils.getFirebaseUserTravelsUrl(userUID))
                                    .child(oldActiveTravelKey)
                                    .updateChildren(activeTravelMap);
                        }

                        if (oldActiveTravelKey == null || !oldActiveTravelKey.equals(newActiveTravelKey)) {
                            // 4.
                            Map<String, Object> newActiveTravelMap = new HashMap<String, Object>();
                            newActiveTravelMap.put(Constants.FIREBASE_TRAVEL_ACTIVE, true);
                            newActiveTravelMap.put(Constants.FIREBASE_TRAVEL_STOP_TIME, -1);
                            new Firebase(Utils.getFirebaseUserTravelsUrl(userUID))
                                    .child(newActiveTravelKey)
                                    .updateChildren(newActiveTravelMap);
                            //set start time if absent
                            if (!Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY.equals(newActiveTravelKey)) {
                                new Firebase(Utils.getFirebaseUserTravelsUrl(userUID))
                                        .child(newActiveTravelKey)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Travel travel = dataSnapshot.getValue(Travel.class);
                                                if (travel.getStart() < 0) {
                                                    Map<String, Object> map = new HashMap<String, Object>();
                                                    map.put(Constants.FIREBASE_TRAVEL_START_TIME, System.currentTimeMillis());
                                                    dataSnapshot.getRef().updateChildren(map);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(FirebaseError firebaseError) {

                                            }
                                        });
                            }

                            // 5.
                            Query query = new Firebase(Utils.getFirebaseUserReminderUrl(userUID))
                                    .orderByChild(Constants.FIREBASE_REMINDER_TRAVELID)
                                    .equalTo(newActiveTravelKey);
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Map<String, Object> map = new HashMap<String, Object>();
                                    map.put(Constants.FIREBASE_REMINDER_ACTIVE, true);
                                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                                        child.getRef().updateChildren(map);
                                    }
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {
                                }
                            });

                            // 6.
                            // present in reminder data change listener (App.class)

                        }

                        // 7.
                        sharedPreferences.edit()
                                .putString(Constants.KEY_ACTIVE_TRAVEL_KEY, newActiveTravelKey)
                                .apply();
                        sharedPreferences.edit()
                                .putString(Constants.KEY_ACTIVE_TRAVEL_TITLE, newActiveTravelTitle)
                                .apply();
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }

        if (mReminderQuery == null) {
            // reminder data change listener
            mReminderQuery = new Firebase(Utils.getFirebaseUserReminderUrl(userUID));
            mReminderQuery.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    ReminderItem reminderItem = dataSnapshot.getValue(ReminderItem.class);
                    Utils.disableAlarmGeofence(getApplicationContext(), dataSnapshot.getKey());
                    if (reminderItem != null && reminderItem.getType() != null
                            && !reminderItem.getType().isEmpty()
                            && reminderItem.isActive()
                            && !reminderItem.isCompleted()) {
                        Utils.enableAlarmGeofence(getApplicationContext(), reminderItem, dataSnapshot.getKey());
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    ReminderItem reminderItem = dataSnapshot.getValue(ReminderItem.class);
                    Utils.disableAlarmGeofence(getApplicationContext(), dataSnapshot.getKey());
                    if (reminderItem != null && reminderItem.getType() != null
                            && !reminderItem.getType().isEmpty()
                            && reminderItem.isActive()
                            && !reminderItem.isCompleted()) {
                        Utils.enableAlarmGeofence(getApplicationContext(), reminderItem, dataSnapshot.getKey());
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Utils.disableAlarmGeofence(getApplicationContext(), dataSnapshot.getKey());
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }

}
