package com.travelersdiary.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;

import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.screens.main.MainActivity;
import com.travelersdiary.screens.reminder.ReminderItemActivity;

import java.util.Calendar;
import java.util.Locale;

public class AlarmNotificationIntentService extends IntentService {
    private static final String TAG = "AlarmNotificationIS";
    private static final String NOTIFICATION_GROUP = "ALARM_NOTIFICATIONSS";

    public static final String KEY_TITLE = "KEY_TITLE";
    public static final String KEY_TIME = "KEY_TIME";
    public static final String KEY_ITEM_KEY = "KEY_ITEM_KEY";

    public AlarmNotificationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String title = intent.getStringExtra(KEY_TITLE);
        String itemKey = intent.getStringExtra(KEY_ITEM_KEY);
        int uid = itemKey.hashCode();
        String text = null;

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
        // TODO: 13.11.16 Add Big View styles and actions
        // TODO: 19.06.17 group notifications

        long time = intent.getLongExtra(KEY_TIME, System.currentTimeMillis());
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        int hours = c.get(Calendar.HOUR);
        int minutes = c.get(Calendar.MINUTE);
        text = String.format(Locale.getDefault(), "Today, %02d:%02d", hours, minutes);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle(title)
                .setContentText(text)
                .setVibrate(new long[]{300, 300, 300, 300, 300})
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_bell_white_24dp)
                .setGroup(NOTIFICATION_GROUP)
                .setGroupSummary(true);
        if (Build.VERSION.SDK_INT >= 21) {
            notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        notificationManager.notify(uid, notificationBuilder.build());
    }
}
