package com.erros.kvasmax.switcher;

import android.app.Application;
import android.os.StrictMode;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by minimax on 06.10.17.
 */

public class SwitcherApplication extends Application {

    public static AtomicBoolean serviceIsRunning = new AtomicBoolean(false);

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            final StrictMode.VmPolicy policy = new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build();
            StrictMode.setVmPolicy(policy);
        }
        final Thread.UncaughtExceptionHandler exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            SettingsManager.getInstance(SwitcherApplication.this).setAppWasCrashed(true);
            exceptionHandler.uncaughtException(thread, throwable);
        });
    }

}
