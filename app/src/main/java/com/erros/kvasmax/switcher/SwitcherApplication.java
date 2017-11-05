package com.erros.kvasmax.switcher;

import android.app.Application;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by minimax on 06.10.17.
 */

public class SwitcherApplication extends Application {

    public static AtomicBoolean serviceIsRunning = new AtomicBoolean(false);

    public static final String APP_PREFERENCES = "APP_PREFERENCES";
    public static final String APP_PREFERENCES_SERVICE_FIRST_LAUNCH = "APP_PREFERENCES_SERVICE_FIRST_LAUNCH";
    public static final String APP_PREFERENCES_BUTTON_POSITION = "APP_PREFERENCES_BUTTON_POSITION";
    public static final String APP_PREFERENCES_BUTTON_X_PORTRAIT = "APP_PREFERENCES_BUTTON_X_PORTRAIT";
    public static final String APP_PREFERENCES_BUTTON_Y_PORTRAIT = "APP_PREFERENCES_BUTTON_Y_PORTRAIT";
    public static final String APP_PREFERENCES_BUTTON_X_LANDSCAPE = "APP_PREFERENCES_BUTTON_X_LANDSCAPE";
    public static final String APP_PREFERENCES_BUTTON_Y_LANDSCAPE = "APP_PREFERENCES_BUTTON_Y_LANDSCAPE";
    public static final String APP_PREFERENCES_BUTTON_THICKNESS = "APP_PREFERENCES_BUTTON_THICKNESS";
    public static final String APP_PREFERENCES_BUTTON_LENGTH = "APP_PREFERENCES_BUTTON_LENGTH";
    public static final String APP_PREFERENCES_BUTTON_COLOR = "APP_PREFERENCES_BUTTON_COLOR";
    public static final String APP_PREFERENCES_BUTTON_AVOID_KEYBOARD = "APP_PREFERENCES_BUTTON_AVOID_KEYBOARD";

    public static final String APP_PREFERENCES_APP_COUNT = "APP_PREFERENCES_APP_COUNT";
    public static final String APP_PREFERENCES_APP_ICON_SIZE = "APP_PREFERENCES_APP_ICON_SIZE";
    public static final String APP_PREFERENCES_APP_ORDER = "APP_PREFERENCES_APP_ORDER";
    public static final String APP_PREFERENCES_APP_LAYOUT = "APP_PREFERENCES_APP_LAYOUT";
    public static final String APP_PREFERENCES_APP_ANIM = "APP_PREFERENCES_APP_ANIM";
    public static final String APP_PREFERENCES_APP_X_DISTANCE = "APP_PREFERENCES_APP_X_DISTANCE";
    public static final String APP_PREFERENCES_APP_Y_DISTANCE = "APP_PREFERENCES_APP_Y_DISTANCE";
    public static final String APP_PREFERENCES_APP_USE_ANIMATION = "APP_PREFERENCES_APP_USE_ANIMATION";
    public static final String APP_PREFERENCES_APP_USE_VIBRATION = "APP_PREFERENCES_APP_USE_VIBRATION";

    public static final String APP_PREFERENCES_COMMON_START_ON_BOOT = "APP_PREFERENCES_COMMON_START_ON_BOOT";
    public static final String APP_PREFERENCES_COMMON_BLACKLIST = "APP_PREFERENCES_COMMON_BLACKLIST";

}
