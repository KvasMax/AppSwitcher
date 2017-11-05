package com.erros.kvasmax.switcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class StartOnBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences settings = context.getSharedPreferences(SwitcherApplication.APP_PREFERENCES, Context.MODE_PRIVATE);
            if(settings.contains(SwitcherApplication.APP_PREFERENCES_COMMON_START_ON_BOOT)) {
                Intent serviceIntent = new Intent(context, SwitcherService.class);
                serviceIntent.putExtra(SwitcherService.PARAM, 1);
                context.startService(serviceIntent);
            }
        }
    }
}
