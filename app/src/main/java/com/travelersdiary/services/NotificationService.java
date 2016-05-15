package com.travelersdiary.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.travelersdiary.R;
import com.travelersdiary.activities.MainActivity;
import com.travelersdiary.fragments.ReminderItemFragment;

import java.util.Calendar;
import java.util.Locale;

public class NotificationService extends Service {
    // TODO: 15.05.16 change to IntentService

    final static String tag = "Notification Service";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO: new extras not showing in notification when item saved
        int id = intent.getIntExtra(ReminderItemFragment.KEY_UID, 0);
        String title = intent.getStringExtra(ReminderItemFragment.KEY_TITLE);
        long time = intent.getLongExtra(ReminderItemFragment.KEY_TIME, System.currentTimeMillis());

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

        stopSelf();
        return START_NOT_STICKY;
    }

}
