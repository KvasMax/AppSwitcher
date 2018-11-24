package com.erros.kvasmax.switcher;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class KamikadzeService extends Service {


    public KamikadzeService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        Notification notification = SwitcherService.getNotification(this);

        startForeground(SwitcherService.NOTIFICATION_ID, notification);
        stopForeground(true);
    }

}