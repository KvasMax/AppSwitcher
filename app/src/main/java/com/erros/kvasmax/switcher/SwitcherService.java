package com.erros.kvasmax.switcher;
/**
 * Created by erros on 25.04.16.
 */

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.view.WindowManager;

import java.util.Set;


public class SwitcherService extends Service implements ISwitcherService {

    private SettingsManager settingsManager;

    AppSwitcher appSwitcher;
    ViewManipulator viewManipulator;

    //Constants
    public static final int NOTIFICATION_ID = 666;

    public static final String ACTION_FINISH = "ACTION_FINISH";

    public static final String PARAM = "SWITCHER_PARAM";

    public static final String ACTION_CHANGE_BUTTON_POSITION = "ACTION_CHANGE_BUTTON_POSITION";
    public static final String ACTION_ALLOW_DRAG_BUTTON = "ACTION_ALLOW_DRAG_BUTTON";
    public static final String ACTION_BUTTON_AVOID_KEYBOARD = "ACTION_BUTTON_AVOID_KEYBOARD";
    public static final String ACTION_CHANGE_BUTTON_THICKNESS = "ACTION_CHANGE_BUTTON_THICKNESS";
    public static final String ACTION_CHANGE_BUTTON_LENGTH = "ACTION_CHANGE_BUTTON_LENGTH";
    public static final String ACTION_CHANGE_BUTTON_COLOR = "ACTION_CHANGE_BUTTON_COLOR";

    public static final String ACTION_APPS_VISIBILITY = "ACTION_APPS_VISIBILITY";
    public static final String ACTION_ALLOW_DRAG_APPS = "ACTION_ALLOW_DRAG_APPS";
    public static final String ACTION_APPS_DARKENING_BACKGROUND = "ACTION_APPS_DARKENING_BACKGROUND";
    public static final String ACTION_CHANGE_APPS_COUNT = "ACTION_CHANGE_APPS_COUNT";
    public static final String ACTION_CHANGE_APPS_ICON_SIZE = "ACTION_CHANGE_APPS_ICON_SIZE";
    public static final String ACTION_CHANGE_APPS_ORDER = "ACTION_CHANGE_APPS_ORDER";
    public static final String ACTION_CHANGE_APPS_LAYOUT = "ACTION_CHANGE_APPS_LAYOUT";
    public static final String ACTION_CHANGE_APPS_ANIM = "ACTION_CHANGE_APPS_ANIM";
    public static final String ACTION_LAUNCH_ANIMATION_ENABLE = "ACTION_LAUNCH_ANIMATION_ENABLE";
    public static final String ACTION_LAUNCH_VIBRATION_ENABLE = "ACTION_LAUNCH_VIBRATION_ENABLE";

    public static final String ACTION_UPDATE_BLACKLIST = "ACTION_UPDATE_BLACKLIST";

    public static Notification getNotification(Context context) {

        final String channelId = "Main";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null
                    && notificationManager.getNotificationChannels().isEmpty()) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_LOW);
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            builder.setPriority(Notification.PRIORITY_MIN);
        else
            builder.setPriority(NotificationManager.IMPORTANCE_LOW);

        return builder.build();
    }

    private void initialise() {
        int maxCount = settingsManager.getAppCount();

        createAppSwitcher(maxCount);
        createWindowContainer(maxCount);
    }

    private void createAppSwitcher(int maxCount) {
        PackageManager packageManager = getApplication().getPackageManager();
        UsageStatsManager usageStatsManager = (UsageStatsManager) getApplication().getSystemService(Activity.USAGE_STATS_SERVICE);
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        boolean useAnimation = settingsManager.isAnimatingSwitching();
        boolean useVibration = settingsManager.isVibratingOnSwitch();
        Set<String> blacklist = settingsManager.getBlacklist();

        appSwitcher = new AppSwitcher(getApplicationContext(),
                usageStatsManager,
                packageManager,
                vibrator,
                maxCount,
                useAnimation,
                useVibration,
                blacklist);
    }

    private void createWindowContainer(int maxCount) {
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        int pointCount = getResources().getInteger(R.integer.point_count);
        int buttonPosition = settingsManager.getButtonPosition();
        int buttonThickness = settingsManager.getButtonThickness();
        int buttonLength = settingsManager.getButtonLength();
        Point coordinates = settingsManager.getButtonPortraitCoordinates();
        int butPortraitY = coordinates.y;
        int butPortraitX = coordinates.x;
        coordinates = settingsManager.getButtonLandscapeCoordinates();
        int butLandscapeY = coordinates.y;
        int butLandscapeX = coordinates.x;
        coordinates = settingsManager.getAppBarDistance();
        int distanceY = coordinates.y;
        int distanceX = coordinates.x;
        int iconSize = settingsManager.getAppIconSize();
        boolean avoidKeyboard = settingsManager.isAvoidingKeyboard();
        boolean appOrder = settingsManager.getAppOrder() == 0;
        int appLayout = settingsManager.getAppLayout();
        int appAnim = settingsManager.getAppBarAnimation();
        int buttonColor = settingsManager.getButtonColor();
        boolean useDarkeningBehind = settingsManager.shouldUseDarkeningBehind();

        if (settingsManager.containsCoordinates()) {
            viewManipulator = new ViewManipulator(this, this, windowManager, maxCount, pointCount,
                    appLayout, appOrder, buttonPosition, buttonThickness, buttonLength, butPortraitX, butPortraitY, butLandscapeX,
                    butLandscapeY, iconSize, distanceX, distanceY, appAnim, getResources().getConfiguration().orientation, buttonColor, avoidKeyboard, useDarkeningBehind);
        } else {
            viewManipulator = new ViewManipulator(this, this, windowManager, maxCount, pointCount, getResources().getConfiguration().orientation,
                    appLayout, appOrder, appAnim, buttonPosition, buttonThickness, buttonLength, buttonColor, iconSize, avoidKeyboard, useDarkeningBehind);
        }
        viewManipulator.dragFloatingButton(settingsManager.isDragableFloatingButton());
        viewManipulator.dragIconBar(settingsManager.isDragableAppBar());
    }

    public void onCreate() {
        super.onCreate();

        SwitcherApplication.serviceIsRunning.set(true);

        Notification notification = getNotification(this);
        startForeground(NOTIFICATION_ID, notification);
        if (Build.VERSION.SDK_INT < 25) {
            Intent kamikadzeIntent = new Intent(this, KamikadzeService.class);
            startService(kamikadzeIntent);
        }

        settingsManager = SettingsManager.getInstance(this);
        initialise();
    }


    private void update() {
        appSwitcher.update();
        viewManipulator.setIconViews(appSwitcher.getIcons());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        SwitcherApplication.serviceIsRunning.set(false);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void onTapIconWithIndex(int position) {
        appSwitcher.startApplication(position);
    }

    @Override
    public void updateIcons() {
        update();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        viewManipulator.rotateScreen(newConfig.orientation);
    }

    @Override
    public void saveWindowPositions() {
        Point pos = viewManipulator.getButtonPortraitPosition();
        settingsManager.saveButtonPortraitCoordinates(pos);
        pos = viewManipulator.getButtonLandscapePosition();
        settingsManager.saveButtonLandscapeCoordinates(pos);
        pos = viewManipulator.getIconBarDistance();
        settingsManager.saveAppBarDistance(pos);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null
                && intent.getAction() != null) {

            int param = intent.getIntExtra(PARAM, 0);

            if (intent.getAction().equals(ACTION_APPS_VISIBILITY)) {
                boolean activityIsVisible = param == 1;
                if (activityIsVisible) {
                    viewManipulator.forceIconBarToBeVisible();
                } else {
                    viewManipulator.allowIconBarToBeHidden();
                }
            } else if (intent.getAction().equals(ACTION_FINISH)) {
                viewManipulator.removeViews();
                stopSelf();
            } else if (intent.getAction().contains(ACTION_ALLOW_DRAG_BUTTON)) {
                viewManipulator.dragFloatingButton(param == 1);
            } else if (intent.getAction().contains(ACTION_CHANGE_BUTTON_POSITION)) {
                viewManipulator.changeButtonPosition(param);
            } else if (intent.getAction().contains(ACTION_ALLOW_DRAG_APPS)) {
                viewManipulator.dragIconBar(param == 1);
            } else if (intent.getAction().contains(ACTION_CHANGE_BUTTON_THICKNESS)) {
                viewManipulator.changeButtonThickness(param);
            } else if (intent.getAction().contains(ACTION_CHANGE_BUTTON_LENGTH)) {
                viewManipulator.changeButtonLength(param);
            } else if (intent.getAction().equals(ACTION_CHANGE_BUTTON_COLOR)) {
                viewManipulator.setButtonColor(param);
            } else if (intent.getAction().equals(ACTION_CHANGE_APPS_COUNT)) {
                appSwitcher.setMaxCount(param);
                viewManipulator.setMaxCount(param);
            } else if (intent.getAction().equals(ACTION_CHANGE_APPS_ICON_SIZE)) {
                viewManipulator.changeIconSize(param);
            } else if (intent.getAction().equals(ACTION_CHANGE_APPS_ORDER)) {
                boolean iconOrderIsDirect = param == 0;
                viewManipulator.changeIconOrder(iconOrderIsDirect);
            } else if (intent.getAction().equals(ACTION_LAUNCH_ANIMATION_ENABLE)) {
                appSwitcher.useAnimation(param == 1);
            } else if (intent.getAction().equals(ACTION_LAUNCH_VIBRATION_ENABLE)) {
                appSwitcher.useVibration(param == 1);
            } else if (intent.getAction().equals(ACTION_BUTTON_AVOID_KEYBOARD)) {
                viewManipulator.avoidKeyboard(param == 1);
            } else if (intent.getAction().equals(ACTION_CHANGE_APPS_LAYOUT)) {
                viewManipulator.changeIconBarLayout(param);
            } else if (intent.getAction().equals(ACTION_CHANGE_APPS_ANIM)) {
                viewManipulator.setAnimation(param);
            } else if (intent.getAction().equals(ACTION_UPDATE_BLACKLIST)) {
                appSwitcher.updateBlacklist(settingsManager.getBlacklist());
            } else if (intent.getAction().equals(ACTION_APPS_DARKENING_BACKGROUND)) {
                viewManipulator.useDarkeningBackground(param == 1);
            }
        }
        return START_STICKY;
    }

}
