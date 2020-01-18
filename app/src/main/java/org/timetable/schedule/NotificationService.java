package org.timetable.schedule;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NotificationService extends Service {
    public NotificationService(Context applicationContext, String classname, String begintime) {
        super();
        Log.i("HERE", "here I am!");
        run(begintime,classname);
    }
    public NotificationService() {
    }

    private void run(String time, String details) {
        while (true){
            System.out.println("MyService still running");
            notifier(time, details);
        }
    }

    private void notifier(String begin, String classname){
        String currentTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
        SimpleDateFormat parser = new SimpleDateFormat("HH:mm:ss");
        try {
            Date start = parser.parse(begin);
            Date userTime = parser.parse(currentTime);
            assert userTime != null;
            if (userTime.equals(start)) {
                createNotification(classname);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    int notificationId = 101;
    private void createNotification(String className){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "NewPeriod")
                .setSmallIcon(R.drawable.ic_icon)
                .setContentTitle(className+" class has started.")
                .setContentText("It's time for the lessons in "+className+". Reach there before it gets too late.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("It's time for the lessons in "+className+". Reach there before it gets too late."))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
// notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, builder.build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent intent = new Intent(this, NotifierStartBroadcastReceiver.class);
        sendBroadcast(intent);
        super.onTaskRemoved(rootIntent);
    }
}
