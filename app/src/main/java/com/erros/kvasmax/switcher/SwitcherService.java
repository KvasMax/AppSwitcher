package com.erros.kvasmax.switcher;
/**
 * Created by erros on 25.04.16.
 */

import android.app.Activity;
import android.app.Notification;
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
import android.support.v4.content.ContextCompat;
import android.view.WindowManager;

import java.util.LinkedHashSet;
import java.util.Set;


public class SwitcherService extends Service implements ISwitcherService {


    private SettingsManager settingsManager;

    AppSwitcher appSwitcher;
    WindowContainer winContainer;

    //Constantsinc
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
    public static final String ACTION_CHANGE_APPS_COUNT = "ACTION_CHANGE_APPS_COUNT";
    public static final String ACTION_CHANGE_APPS_ICON_SIZE = "ACTION_CHANGE_APPS_ICON_SIZE";
    public static final String ACTION_CHANGE_APPS_ORDER = "ACTION_CHANGE_APPS_ORDER";
    public static final String ACTION_CHANGE_APPS_LAYOUT = "ACTION_CHANGE_APPS_LAYOUT";
    public static final String ACTION_CHANGE_APPS_ANIM = "ACTION_CHANGE_APPS_ANIM";
    public static final String ACTION_LAUNCH_ANIMATION_ENABLE = "ACTION_LAUNCH_ANIMATION_ENABLE";
    public static final String ACTION_LAUNCH_VIBRATION_ENABLE = "ACTION_LAUNCH_VIBRATION_ENABLE";

    public static final String ACTION_UPDATE_BLACKLIST = "ACTION_UPDATE_BLACKLIST";

    int defaultSpinnerValue = 1;
    int defaultModeValue = 0;

    public void onCreate() {
        super.onCreate();

        SwitcherApplication.serviceIsRunning.set(true);

        Notification notification = KamikadzeService.getNotification(this);
        startForeground(KamikadzeService.NOTIFICATION_ID, notification);
        if (Build.VERSION.SDK_INT < 25) {
            Intent kamikadzeIntent = new Intent(this, KamikadzeService.class);
            startService(kamikadzeIntent);
        }


        //DaggerSwitcherComponent.builder().switcherModule(new SwitcherModule(getApplication())).build().inject(this);

        settingsManager = SettingsManager.getInstance(this);
        settingsManager.serviceWasLaunched();
        initialise();
        update();
    }

    private void initialise() {
        int maxCount = 5;
        maxCount = settingsManager.getAppCount() + 1;

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

        appSwitcher = new AppSwitcher(getBaseContext(), usageStatsManager, packageManager, vibrator, maxCount, useAnimation, useVibration, blacklist);
    }

    private void createWindowContainer(int maxCount) {
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        int pointCount = 100;
        int buttonThickness = 10;
        int buttonLength = 10;
        int butPortraitY = 0;
        int butPortraitX = 200;
        int butLandscapeY = 10;
        int butLandscapeX = 10;
        int distanceY = 0;
        int distanceX = -200;
        int buttonPosition = 1;
        int iconSize = 0;
        int appLayout = 0;
        int appAnim = 0;
        int buttonColor = ContextCompat.getColor(this, R.color.defaultColor);
        boolean appOrder = true;
        boolean avoidKeyboard = true;

        pointCount = getResources().getInteger(R.integer.point_count);
        buttonPosition = settingsManager.getButtonPosition();
        buttonThickness = settingsManager.getButttonThickness();
        buttonLength = settingsManager.getButtonLength();
        Point coordinates = settingsManager.getButtonPortraitCoordinates();
        butPortraitY = coordinates.y;
        butPortraitX = coordinates.x;
        coordinates = settingsManager.getButtonLandscapeCoordinates();
        butLandscapeY = coordinates.y;
        butLandscapeX = coordinates.x;
        coordinates = settingsManager.getAppBarDistance();
        distanceY = coordinates.y;
        distanceX = coordinates.x;
        iconSize = settingsManager.getAppIconSize();
        avoidKeyboard = settingsManager.isAvoidingKeyboard();
        appOrder = settingsManager.getAppOrder() == 0;
        appLayout = settingsManager.getAppLayout();
        appAnim = settingsManager.getAppBarAnimation();
        buttonColor = settingsManager.getButtonColor();

        if (settingsManager.containsCoordinates()) {
            winContainer = new WindowContainer(this, this, windowManager, maxCount, pointCount,
                    appLayout, appOrder, buttonPosition, buttonThickness, buttonLength, butPortraitX, butPortraitY, butLandscapeX,
                    butLandscapeY, iconSize, distanceX, distanceY, appAnim, getResources().getConfiguration().orientation, buttonColor, avoidKeyboard);
        } else {
            winContainer = new WindowContainer(this, this, windowManager, maxCount, pointCount, getResources().getConfiguration().orientation,
                    appLayout, appOrder, appAnim, buttonPosition, buttonThickness, buttonLength, buttonColor, iconSize, avoidKeyboard);
        }
        winContainer.dragFloatingButton(settingsManager.isDragableFloatingButton());
        winContainer.dragIconBar(settingsManager.isDragableAppBar());
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            winContainer.hideIconBar();
        } else {

            int param = intent.getIntExtra(PARAM, 0);
            if (intent.getAction() == null || intent.getAction().equals(ACTION_APPS_VISIBILITY)) {
                boolean activityIsVisible = param == 0;
                if (activityIsVisible) {
                    winContainer.showIconBar();
                } else {
                    winContainer.hideIconBar();
                }
            } else if (intent.getAction().equals(ACTION_FINISH)) {
                winContainer.removeViews();
                stopSelf();
            } else if (intent.getAction().contains(ACTION_ALLOW_DRAG_BUTTON)) {
                winContainer.dragFloatingButton(param == 1);
            } else if (intent.getAction().contains(ACTION_CHANGE_BUTTON_POSITION)) {
                winContainer.changeButtonPosition(param);
            } else if (intent.getAction().contains(ACTION_ALLOW_DRAG_APPS)) {
                winContainer.dragIconBar(param == 1);
            } else if (intent.getAction().contains(ACTION_CHANGE_BUTTON_THICKNESS)) {
                winContainer.changeButtonThickness(param);
            } else if (intent.getAction().contains(ACTION_CHANGE_BUTTON_LENGTH)) {
                winContainer.changeButtonLength(param);
            } else if (intent.getAction().equals(ACTION_CHANGE_BUTTON_COLOR)) {
                winContainer.setButtonColor(param);
            } else if (intent.getAction().equals(ACTION_CHANGE_APPS_COUNT)) {
                int maxCount = param + 1;
                appSwitcher.setMaxCount(maxCount);
                winContainer.setMaxCount(maxCount);
            } else if (intent.getAction().equals(ACTION_CHANGE_APPS_ICON_SIZE)) {
                winContainer.changeIconSize(param);
            } else if (intent.getAction().equals(ACTION_CHANGE_APPS_ORDER)) {
                boolean appOrder = param == 0;
                winContainer.changeIconOrder(appOrder);
            } else if (intent.getAction().equals(ACTION_LAUNCH_ANIMATION_ENABLE)) {
                appSwitcher.useAnimation(param == 1);
            } else if (intent.getAction().equals(ACTION_LAUNCH_VIBRATION_ENABLE)) {
                appSwitcher.useVibration(param == 1);
            } else if (intent.getAction().equals(ACTION_BUTTON_AVOID_KEYBOARD)) {
                winContainer.avoidKeyboard(param == 1);
            } else if (intent.getAction().equals(ACTION_CHANGE_APPS_LAYOUT)) {
                winContainer.changeIconBarLayout(param);
            } else if (intent.getAction().equals(ACTION_CHANGE_APPS_ANIM)) {
                winContainer.setAnimation(param);
            } else if (intent.getAction().equals(ACTION_UPDATE_BLACKLIST)) {
                appSwitcher.updateBlacklist(settingsManager.getBlacklist());
            }
        }
        return START_STICKY;
    }


    private void update() {
        appSwitcher.update();
        winContainer.setIconViews(appSwitcher.getIcons());
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
    public void startApplication(int position) {
        appSwitcher.startApplication(position);
    }

    @Override
    public void updateAppList() {
        update();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        winContainer.rotateScreen(newConfig.orientation);
    }

    public void saveWindowPositions() {
        Point pos = winContainer.getButtonPortraitPosition();
        settingsManager.saveButtonPortraitCoordinates(pos);
        pos = winContainer.getButtonLandscapePosition();
        settingsManager.saveButtonLandscapeCoordinates(pos);
        pos = winContainer.getIconBarDistance();
        settingsManager.saveAppBarDistance(pos);
    }

}
