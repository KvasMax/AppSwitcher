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
            StrictMode.VmPolicy policy = new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build();
            StrictMode.setVmPolicy(policy);
        }
    }

}
