package org.timetable.schedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotifierStartBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(NotifierStartBroadcastReceiver.class.getSimpleName(), "Service Stops");
        context.startService(new Intent(context, NotificationService.class));
    }
}