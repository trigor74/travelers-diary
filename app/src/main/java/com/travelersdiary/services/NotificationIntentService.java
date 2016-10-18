package com.travelersdiary.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.activities.MainActivity;

import java.util.Calendar;
import java.util.Locale;

public class NotificationIntentService extends IntentService {

    public static final String KEY_UID = "KEY_UID";
    public static final String KEY_TYPE = "KEY_TYPE";
    public static final String KEY_TITLE = "KEY_TITLE";
    public static final String KEY_TIME = "KEY_TIME";
    public static final String KEY_LOCATION_TITLE = "KEY_LOCATION_TITLE";

    public NotificationIntentService() {
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int uid = intent.getIntExtra(KEY_UID, 0);
        String title = intent.getStringExtra(KEY_TITLE);
        String type = intent.getStringExtra(KEY_TYPE);
        String text = null;
        PendingIntent pendingIntent = null;

        if (Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_LOCATION.equals(type)) {
            // TODO: 18.10.16 geofence notification
            String locationTitle = intent.getStringExtra(KEY_LOCATION_TITLE);

        } else if (Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_TIME.equals(type)) {
            // TODO: 18.10.16 alarm notification
            long time = intent.getLongExtra(KEY_TIME, System.currentTimeMillis());

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(time);
            int hours = c.get(Calendar.HOUR);
            int minutes = c.get(Calendar.MINUTE);

            text = String.format(Locale.getDefault(), "Today, %02d:%02d", hours, minutes);

            //TODO: open specific reminder item
            pendingIntent = PendingIntent.getActivity(getApplicationContext(), uid,
                    new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        }
        sendNotification(uid, title, text, pendingIntent);
    }

    private void sendNotification(int uid, String title, String text, PendingIntent pendingIntent) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle(title)
                .setContentText(text)
                .setVibrate(new long[]{300, 300, 300, 300, 300})
                .setSmallIcon(R.drawable.ic_bell_white_24dp)
                .setContentIntent(pendingIntent)
                .build();

        notificationManager.notify(uid, notification);
    }
}
