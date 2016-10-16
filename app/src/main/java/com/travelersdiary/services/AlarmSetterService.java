package com.travelersdiary.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;

import com.travelersdiary.Constants;
import com.travelersdiary.models.ReminderItem;

public class AlarmSetterService extends IntentService {

    private static final String ACTION_SET_ALARM = "ACTION_SET_ALARM";
    private static final String ACTION_CANCEL_ALARM_GEOFENCE = "ACTION_CANCEL_ALARM_GEOFENCE";
    private static final String ACTION_SET_GEOFENCE = "ACTION_SET_GEOFENCE";

    private static final String EXTRA_UID = "EXTRA_UID";
    private static final String EXTRA_TITLE = "EXTRA_TITLE";
    private static final String EXTRA_TIME = "EXTRA_TIME";

    public AlarmSetterService() {
        super("AlarmSetterService");
    }

    public static void setAlarm(Context context, ReminderItem reminderItem) {
        Intent intent = new Intent(context, AlarmSetterService.class);
        switch (reminderItem.getType()) {
            case Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_TIME:
                intent.setAction(ACTION_SET_ALARM);
                intent.putExtra(EXTRA_UID, reminderItem.getUID());
                intent.putExtra(EXTRA_TITLE, reminderItem.getTitle());
                intent.putExtra(EXTRA_TIME, reminderItem.getTime());
                break;
            case Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_LOCATION:
                intent.setAction(ACTION_SET_GEOFENCE);
                // TODO: 16.10.16 set geofence
                break;
        }
        context.startService(intent);
    }

    public static void cancelAlarmGeofence(Context context, ReminderItem reminderItem) {
        Intent intent = new Intent(context, AlarmSetterService.class);
        intent.setAction(ACTION_CANCEL_ALARM_GEOFENCE);
        intent.putExtra(EXTRA_UID, reminderItem.getUID());
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SET_ALARM.equals(action)) {
                final int id = intent.getIntExtra(EXTRA_UID, 0);
                final String title = intent.getStringExtra(EXTRA_TITLE);
                final long time = intent.getLongExtra(EXTRA_TIME, System.currentTimeMillis());
                handleSetAlarm(time, createTimePendingIntent(id, title, time));
            } else if (ACTION_CANCEL_ALARM_GEOFENCE.equals(action)) {
                final int id = intent.getIntExtra(EXTRA_UID, 0);
                handleCancelAlarmGeofence(createTimePendingIntent(id, "", 0));
            }
        }
    }

    private PendingIntent createTimePendingIntent(int uid, String title, long time) {
        Intent intent = new Intent(this, NotificationIntentService.class);
        intent.putExtra(NotificationIntentService.KEY_UID, uid);
        intent.putExtra(NotificationIntentService.KEY_TITLE, title);
        intent.putExtra(NotificationIntentService.KEY_TIME, time);
        PendingIntent pendingIntent = PendingIntent.getService(this, uid, intent, 0);
        return pendingIntent;
    }

    private void handleSetAlarm(long time, PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }

    private void handleCancelAlarmGeofence(PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        // TODO: 16.10.16 handle geofences
    }
}
