package com.travelersdiary.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderOnBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent service = new Intent(context, ReminderOnBootSetterService.class);
            context.startService(service);
        }
    }
}
