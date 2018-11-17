package com.erros.kvasmax.switcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.support.v4.content.ContextCompat;

import java.util.Set;

/**
 * Created by minimax on 11/19/17.
 */

public class SettingsManager {

    private static volatile SettingsManager instance;

    public static SettingsManager getInstance(Context context) {
        SettingsManager localInstance = instance;
        if (localInstance == null) {
            synchronized (SettingsManager.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new SettingsManager(context.getApplicationContext());
                }
            }
        }
        return localInstance;
    }

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    private int defaultColor;

    private SettingsManager(Context context) {
        settings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        editor = settings.edit();
        defaultColor = ContextCompat.getColor(context, R.color.defaultColor);
    }

    public boolean isFirstAppLaunch() {
        return !settings.contains(APP_PREFERENCES_APP_COUNT);
    }

    public boolean isFirstServiceLaunch() {
        return !settings.contains(APP_PREFERENCES_APP_X_DISTANCE);
    }

    public Set<String> getBlacklist() {
        return settings.getStringSet(APP_PREFERENCES_COMMON_BLACKLIST, null);
    }

    public void saveBlacklist(Set<String> blacklist) {
        editor.putStringSet(APP_PREFERENCES_COMMON_BLACKLIST, blacklist);
        editor.apply();
    }

    public int getButtonColor() {
        return settings.getInt(APP_PREFERENCES_BUTTON_COLOR, defaultColor);
    }

    public int getButtonDefaultColor() {
        return defaultColor;
    }

    public void saveButtonColor(int color) {
        editor.putInt(APP_PREFERENCES_BUTTON_COLOR, color);
        editor.apply();
    }

    public int getButtonThickness() {
        return settings.getInt(APP_PREFERENCES_BUTTON_THICKNESS, 40);
    }

    public void saveButtonThickness(int value) {
        editor.putInt(APP_PREFERENCES_BUTTON_THICKNESS, value);
        editor.apply();
    }

    public int getButtonLength() {
        return settings.getInt(APP_PREFERENCES_BUTTON_LENGTH, 80);
    }

    public void saveButtonLength(int value) {
        editor.putInt(APP_PREFERENCES_BUTTON_LENGTH, value);
        editor.apply();
    }

    public int getButtonPosition() {
        return settings.getInt(APP_PREFERENCES_BUTTON_POSITION, 1);
    }

    public void saveButtonPosition(int value) {
        editor.putInt(APP_PREFERENCES_BUTTON_POSITION, value);
        editor.apply();
    }

    public int getAppCount() {
        return settings.getInt(APP_PREFERENCES_APP_COUNT, 4);
    }

    public void saveAppCount(int value) {
        editor.putInt(APP_PREFERENCES_APP_COUNT, value);
        editor.apply();
    }

    public int getAppIconSize() {
        return settings.getInt(APP_PREFERENCES_APP_ICON_SIZE, 35);
    }

    public void saveAppIconSize(int value) {
        editor.putInt(APP_PREFERENCES_APP_ICON_SIZE, value);
        editor.apply();
    }

    public int getAppOrder() {
        return settings.getInt(APP_PREFERENCES_APP_ORDER, 1);
    }

    public void saveAppOrder(int value) {
        editor.putInt(APP_PREFERENCES_APP_ORDER, value);
        editor.apply();
    }

    public int getAppLayout() {
        return settings.getInt(APP_PREFERENCES_APP_LAYOUT, 0);
    }

    public void saveAppLayout(int value) {
        editor.putInt(APP_PREFERENCES_APP_LAYOUT, value);
        editor.apply();
    }

    public int getAppBarAnimation() {
        return settings.getInt(APP_PREFERENCES_APP_ANIM, 1);
    }

    public void saveAppBarAnimation(int value) {
        editor.putInt(APP_PREFERENCES_APP_ANIM, value);
        editor.apply();
    }

    public boolean isAnimatingSwitching() {
        return settings.getBoolean(APP_PREFERENCES_APP_USE_ANIMATION, true);
    }

    public void saveAnimatingSwitching(boolean value) {
        editor.putBoolean(APP_PREFERENCES_APP_USE_ANIMATION, value);
        editor.apply();
    }

    public boolean isVibratingOnSwitch() {
        return settings.getBoolean(APP_PREFERENCES_APP_USE_VIBRATION, false);
    }

    public void saveVibratingOnSwitch(boolean value) {
        editor.putBoolean(APP_PREFERENCES_APP_USE_VIBRATION, value);
        editor.apply();
    }

    public boolean isAvoidingKeyboard() {
        return settings.getBoolean(APP_PREFERENCES_BUTTON_AVOID_KEYBOARD, false);
    }

    public void saveAvoidingKeyboard(boolean value) {
        editor.putBoolean(APP_PREFERENCES_BUTTON_AVOID_KEYBOARD, value);
        editor.apply();
    }

    public boolean isStartingOnBoot() {
        return settings.contains(APP_PREFERENCES_COMMON_START_ON_BOOT);
    }

    public void saveStartingOnBoot(boolean value) {
        if(value) {
            editor.putBoolean(APP_PREFERENCES_COMMON_START_ON_BOOT, value);
        } else {
            editor.remove(APP_PREFERENCES_COMMON_START_ON_BOOT);
        }
        editor.apply();
    }

    public boolean containsCoordinates() {
        return settings.contains(APP_PREFERENCES_BUTTON_X_PORTRAIT);
    }

    public boolean isDragableFloatingButton() {
        return settings.getBoolean(APP_PREFERENCES_BUTTON_DRAG, false);
    }

    public void saveDragableFloatingButton(boolean value) {
        editor.putBoolean(APP_PREFERENCES_BUTTON_DRAG, value);
        editor.apply();
    }

    public boolean isDragableAppBar() {
        return settings.getBoolean(APP_PREFERENCES_APP_DRAG, false);
    }

    public void saveDragableAppBar(boolean value) {
        editor.putBoolean(APP_PREFERENCES_APP_DRAG, value);
        editor.apply();
    }

    public Point getButtonPortraitCoordinates() {
        Point point = new Point();
        point.x = settings.getInt(APP_PREFERENCES_BUTTON_X_PORTRAIT, 1);
        point.y = settings.getInt(APP_PREFERENCES_BUTTON_Y_PORTRAIT, 1);
        return point;
    }

    public void saveButtonPortraitCoordinates(Point point) {
        editor.putInt(APP_PREFERENCES_BUTTON_X_PORTRAIT, point.x);
        editor.putInt(APP_PREFERENCES_BUTTON_Y_PORTRAIT, point.y);
        editor.apply();
    }

    public Point getButtonLandscapeCoordinates() {
        Point point = new Point();
        point.x = settings.getInt(APP_PREFERENCES_BUTTON_X_LANDSCAPE, 1);
        point.y = settings.getInt(APP_PREFERENCES_BUTTON_Y_LANDSCAPE, 1);
        return point;
    }

    public void saveButtonLandscapeCoordinates(Point point) {
        editor.putInt(APP_PREFERENCES_BUTTON_X_LANDSCAPE, point.x);
        editor.putInt(APP_PREFERENCES_BUTTON_Y_LANDSCAPE, point.y);
        editor.apply();
    }

    public Point getAppBarDistance() {
        Point point = new Point();
        point.x = settings.getInt(APP_PREFERENCES_APP_X_DISTANCE, 1);
        point.y = settings.getInt(APP_PREFERENCES_APP_Y_DISTANCE, 1);
        return point;
    }

    public void saveAppBarDistance(Point point) {
        editor.putInt(APP_PREFERENCES_APP_X_DISTANCE, point.x);
        editor.putInt(APP_PREFERENCES_APP_Y_DISTANCE, point.y);
        editor.apply();
    }

    private final String APP_PREFERENCES = "APP_PREFERENCES";

    private final String APP_PREFERENCES_BUTTON_DRAG = "APP_PREFERENCES_BUTTON_DRAG";
    private final String APP_PREFERENCES_BUTTON_POSITION = "APP_PREFERENCES_BUTTON_POSITION";
    private final String APP_PREFERENCES_BUTTON_X_PORTRAIT = "APP_PREFERENCES_BUTTON_X_PORTRAIT";
    private final String APP_PREFERENCES_BUTTON_Y_PORTRAIT = "APP_PREFERENCES_BUTTON_Y_PORTRAIT";
    private final String APP_PREFERENCES_BUTTON_X_LANDSCAPE = "APP_PREFERENCES_BUTTON_X_LANDSCAPE";
    private final String APP_PREFERENCES_BUTTON_Y_LANDSCAPE = "APP_PREFERENCES_BUTTON_Y_LANDSCAPE";
    private final String APP_PREFERENCES_BUTTON_THICKNESS = "APP_PREFERENCES_BUTTON_THICKNESS";
    private final String APP_PREFERENCES_BUTTON_LENGTH = "APP_PREFERENCES_BUTTON_LENGTH";
    private final String APP_PREFERENCES_BUTTON_COLOR = "APP_PREFERENCES_BUTTON_COLOR";
    private final String APP_PREFERENCES_BUTTON_AVOID_KEYBOARD = "APP_PREFERENCES_BUTTON_AVOID_KEYBOARD";

    private final String APP_PREFERENCES_APP_COUNT = "APP_PREFERENCES_APP_COUNT";
    private final String APP_PREFERENCES_APP_DRAG = "APP_PREFERENCES_APP_DRAG";
    private final String APP_PREFERENCES_APP_ICON_SIZE = "APP_PREFERENCES_APP_ICON_SIZE";
    private final String APP_PREFERENCES_APP_ORDER = "APP_PREFERENCES_APP_ORDER";
    private final String APP_PREFERENCES_APP_LAYOUT = "APP_PREFERENCES_APP_LAYOUT";
    private final String APP_PREFERENCES_APP_ANIM = "APP_PREFERENCES_APP_ANIM";
    private final String APP_PREFERENCES_APP_X_DISTANCE = "APP_PREFERENCES_APP_X_DISTANCE";
    private final String APP_PREFERENCES_APP_Y_DISTANCE = "APP_PREFERENCES_APP_Y_DISTANCE";
    private final String APP_PREFERENCES_APP_USE_ANIMATION = "APP_PREFERENCES_APP_USE_ANIMATION";
    private final String APP_PREFERENCES_APP_USE_VIBRATION = "APP_PREFERENCES_APP_USE_VIBRATION";

    private final String APP_PREFERENCES_COMMON_START_ON_BOOT = "APP_PREFERENCES_COMMON_START_ON_BOOT";
    private final String APP_PREFERENCES_COMMON_BLACKLIST = "APP_PREFERENCES_COMMON_BLACKLIST";
}
