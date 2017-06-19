package com.travelersdiary.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.travelersdiary.models.ReminderItem;

public class AlarmSetterService extends IntentService {

    private static final String ACTION_SET_ALARM = "ACTION_SET_ALARM";
    private static final String ACTION_CANCEL_ALARM = "ACTION_CANCEL_ALARM";

    private static final String EXTRA_TITLE = "EXTRA_TITLE";
    private static final String EXTRA_TIME = "EXTRA_TIME";
    private static final String EXTRA_ITEM_KEY = "EXTRA_ITEM_KEY";

    public AlarmSetterService() {
        super("AlarmSetterService");
    }

    public static void setAlarm(Context context, ReminderItem reminderItem, String itemKey) {
        if (reminderItem.getTime() == 0 || reminderItem.getTime() < System.currentTimeMillis()) {
            return;
        }
        Intent intent = new Intent(context, AlarmSetterService.class);
        intent.setAction(ACTION_SET_ALARM);
        intent.putExtra(EXTRA_TITLE, reminderItem.getTitle());
        intent.putExtra(EXTRA_TIME, reminderItem.getTime());
        intent.putExtra(EXTRA_ITEM_KEY, itemKey);
        context.startService(intent);
    }

    public static void cancelAlarm(Context context, String itemKey) {
        Intent intent = new Intent(context, AlarmSetterService.class);
        intent.setAction(ACTION_CANCEL_ALARM);
        intent.putExtra(EXTRA_ITEM_KEY, itemKey);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SET_ALARM.equals(action)) {
                final String title = intent.getStringExtra(EXTRA_TITLE);
                final long time = intent.getLongExtra(EXTRA_TIME, System.currentTimeMillis());
                final String itemKey = intent.getStringExtra(EXTRA_ITEM_KEY);
                handleSetAlarm(time, createAlarmPendingIntent(title, time, itemKey));
            } else if (ACTION_CANCEL_ALARM.equals(action)) {
                final String itemKey = intent.getStringExtra(EXTRA_ITEM_KEY);
                handleCancelAlarm(createAlarmPendingIntent(null, 0, itemKey));
            }
        }
    }

    private PendingIntent createAlarmPendingIntent(String title, long time, String itemKey) {
        Intent intent = new Intent(this, AlarmNotificationIntentService.class);
        intent.putExtra(AlarmNotificationIntentService.KEY_TITLE, title);
        intent.putExtra(AlarmNotificationIntentService.KEY_TIME, time);
        intent.putExtra(AlarmNotificationIntentService.KEY_ITEM_KEY, itemKey);
        return PendingIntent.getService(this, itemKey.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void handleSetAlarm(long time, PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }

    private void handleCancelAlarm(PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
