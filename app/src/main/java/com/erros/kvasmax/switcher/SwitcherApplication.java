package com.erros.kvasmax.switcher;

import android.app.Application;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by minimax on 06.10.17.
 */

public class SwitcherApplication extends Application {

    public static AtomicBoolean serviceIsRunning = new AtomicBoolean(false);

}
