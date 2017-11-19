package com.erros.kvasmax.switcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class StartOnBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            if (SettingsManager.getInstance(context).isStartingOnBoot()) {
                Intent serviceIntent = new Intent(context, SwitcherService.class);
                serviceIntent.putExtra(SwitcherService.PARAM, 1);
                context.startService(serviceIntent);
            }
        }
    }
}
