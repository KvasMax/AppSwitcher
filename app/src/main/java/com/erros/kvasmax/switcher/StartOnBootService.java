package com.erros.kvasmax.switcher;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.ContextCompat;


public class StartOnBootService extends JobIntentService {

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Intent serviceIntent = new Intent(this, SwitcherService.class);
        serviceIntent.putExtra(SwitcherService.PARAM, 0);
        serviceIntent.setAction(SwitcherService.ACTION_APPS_VISIBILITY);
        ContextCompat.startForegroundService(this, serviceIntent);
    }
}
