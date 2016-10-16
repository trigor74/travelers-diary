package com.travelersdiary.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.travelersdiary.R;
import com.travelersdiary.activities.MainActivity;

import java.util.Calendar;
import java.util.Locale;

public class NotificationIntentService extends IntentService {

    public static final String KEY_UID = "KEY_UID";
    public static final String KEY_TITLE = "KEY_TITLE";
    public static final String KEY_TIME = "KEY_TIME";

    public NotificationIntentService() {
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int id = intent.getIntExtra(KEY_UID, 0);
        String title = intent.getStringExtra(KEY_TITLE);
        long time = intent.getLongExtra(KEY_TIME, System.currentTimeMillis());

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        int hours = c.get(Calendar.HOUR);
        int minutes = c.get(Calendar.MINUTE);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle(title)
                .setContentText(String.format(Locale.getDefault(), "Today, %02d:%02d", hours, minutes))
                .setVibrate(new long[]{300, 300, 300, 300, 300})
                .setSmallIcon(R.drawable.ic_bell_white_24dp)
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0,
                        new Intent(getApplicationContext(), MainActivity.class), 0)) //TODO: open specific reminder item
                .build();

        notificationManager.notify(id, notification);
    }
}
