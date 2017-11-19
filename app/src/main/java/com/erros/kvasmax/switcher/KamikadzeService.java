package com.erros.kvasmax.switcher;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class KamikadzeService extends Service {

    public static final int NOTIFICATION_ID = 666;

    public KamikadzeService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {

        Notification notification = getNotification(this);

        startForeground(NOTIFICATION_ID, notification);
        stopForeground(true);
    }

    public static Notification getNotification(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "");
        if (Build.VERSION.SDK_INT < 24)
            builder.setPriority(Notification.PRIORITY_MIN);
        else
            builder.setPriority(NotificationManager.IMPORTANCE_NONE);
        Notification notification = builder.build();
        // Notification.Builder builder = new Notification.Builder(this)
        //         .setSmallIcon(R.drawable.bubble);
        /*  notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;*/
        return notification;
    }
}
