package com.erros.kvasmax.switcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class StartOnBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
            if (SettingsManager.getInstance(context).isStartingOnBoot()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    StartOnBootService.enqueueWork(context, StartOnBootService.class, 1, new Intent());
                } else {
                    Intent serviceIntent = new Intent(context, SwitcherService.class);
                    serviceIntent.putExtra(SwitcherService.PARAM, 0);
                    serviceIntent.setAction(SwitcherService.ACTION_APPS_VISIBILITY);
                    context.startService(serviceIntent);
                }
            }
    }
}
