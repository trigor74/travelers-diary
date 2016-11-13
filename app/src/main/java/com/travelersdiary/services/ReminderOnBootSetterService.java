package com.travelersdiary.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.travelersdiary.Constants;
import com.travelersdiary.Utils;
import com.travelersdiary.models.ReminderItem;

public class ReminderOnBootSetterService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setAllAlarms();
    }

    private void setAllAlarms() {
        final Context context = getApplicationContext();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

        new Firebase(Utils.getFirebaseUserReminderUrl(userUID))
                .orderByChild(Constants.FIREBASE_REMINDER_ACTIVE)
                .equalTo(true)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            ReminderItem reminderItem = child.getValue(ReminderItem.class);
                            if (!reminderItem.getType().isEmpty()
                                    && !reminderItem.isCompleted()
                                    && reminderItem.getUID() != 0) {
                                Utils.enableAlarmGeofence(context.getApplicationContext(), reminderItem, child.getKey());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }
}
