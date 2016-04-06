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

import java.util.Calendar;
import java.util.Locale;

public class ReminderService extends Service {

    final static String tag = "Reminder Service";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int id = intent.getIntExtra("hash", 0);
        String title = intent.getStringExtra("title");
        long time = intent.getLongExtra("time", System.currentTimeMillis());

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
