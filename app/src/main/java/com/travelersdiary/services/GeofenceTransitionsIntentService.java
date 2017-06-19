package com.travelersdiary.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.models.ReminderItem;
import com.travelersdiary.screens.main.MainActivity;
import com.travelersdiary.screens.reminder.ReminderItemActivity;

import java.util.List;
import java.util.Locale;


public class GeofenceTransitionsIntentService extends IntentService {
    private static final String TAG = "GeofenceTransitionsIS";
    private static final String NOTIFICATION_GROUP = "GEOFENCE_TRANSITIONS";

    public GeofenceTransitionsIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.d(TAG, "GeofencingEvent ERROR_CODE:" + Integer.toString(geofencingEvent.getErrorCode()));
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

        List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
        for (Geofence geofence : triggeringGeofences) {
            String reminderItemKey = geofence.getRequestId();

            new Firebase(Utils.getFirebaseUserReminderUrl(userUID))
                    .child(reminderItemKey)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ReminderItem item = dataSnapshot.getValue(ReminderItem.class);
                            String itemKey = dataSnapshot.getKey();
                            int uid = itemKey.hashCode();
                            String title = item.getTitle();
                            String locationTitle = item.getWaypoint().getTitle();
                            String text = String.format(Locale.getDefault(), "Enter geofence, %s", locationTitle);

                            // open Main Activity with Reminder list
                            Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                            mainIntent.putExtra(MainActivity.KEY_FRAGMENT, MainActivity.REMINDER_LIST_FRAGMENT_TAG);
                            //
                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                            stackBuilder.addParentStack(MainActivity.class);
                            stackBuilder.addNextIntent(mainIntent);
                            // open reminder item activity with itemKey
                            Intent reminderItemIntent = new Intent(getApplicationContext(), ReminderItemActivity.class);
                            reminderItemIntent.putExtra(Constants.KEY_REMINDER_ITEM_KEY, itemKey);
                            stackBuilder.addNextIntent(reminderItemIntent);

                            PendingIntent pendingIntent = stackBuilder.getPendingIntent(uid, PendingIntent.FLAG_UPDATE_CURRENT);

                            // TODO: 19.06.17 group notifications
                            // TODO: 19.06.17 Add Big View styles and actions
                            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                            NotificationCompat.Builder notificationBuilder =
                                    (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                                            .setContentTitle(title)
                                            .setContentText(text)
                                            .setVibrate(new long[]{300, 300, 300, 300, 300})
                                            .setContentIntent(pendingIntent)
                                            .setAutoCancel(true)
                                            .setSmallIcon(R.drawable.ic_location_notify_white)
                                            .setGroup(NOTIFICATION_GROUP)
                                            .setGroupSummary(true);
                            if (Build.VERSION.SDK_INT >= 21) {
                                notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
                            }

                            notificationManager.notify(uid, notificationBuilder.build());
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
        }
    }
}
