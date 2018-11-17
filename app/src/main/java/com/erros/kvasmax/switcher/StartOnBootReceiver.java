package com.erros.kvasmax.switcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartOnBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
            if (SettingsManager.getInstance(context).isStartingOnBoot()) {
                Intent serviceIntent = new Intent(context, SwitcherService.class);
                serviceIntent.putExtra(SwitcherService.PARAM, 1);
                context.startService(serviceIntent);
            }
    }
}
