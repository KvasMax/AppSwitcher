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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.view.WindowManager;



public class SwitcherService extends Service implements ISwitcherService {


    private SharedPreferences mSettings;
    private SharedPreferences.Editor mEditor;


    AppSwitcher appSwitcher;
    WindowContainer winContainer;

    //Constantsinc
    public  static final String ACTION_FINISH ="ACTION_FINISH";

    public  static final String ACTION_CHANGE_BUTTON_POSITION = "ACTION_CHANGE_BUTTON_POSITION";
    public  static final String ACTION_ALLOW_DRAG_BUTTON = "ACTION_ALLOW_DRAG_BUTTON";
    public  static final String ACTION_BUTTON_AVOID_KEYBOARD = "ACTION_BUTTON_AVOID_KEYBOARD";
    public  static final String ACTION_CHANGE_BUTTON_THICKNESS ="ACTION_CHANGE_BUTTON_THICKNESS";
    public  static final String ACTION_CHANGE_BUTTON_LENGTH ="ACTION_CHANGE_BUTTON_LENGTH";
    public  static final String ACTION_CHANGE_BUTTON_COLOR = "ACTION_CHANGE_BUTTON_COLOR";

    public  static final String ACTION_APPS_VISIBILITY="ACTION_APPS_VISIBILITY";
    public  static final String ACTION_ALLOW_DRAG_APPS ="ACTION_ALLOW_DRAG_APPS";
    public  static final String ACTION_CHANGE_APPS_COUNT ="ACTION_CHANGE_APPS_COUNT";
    public  static final String ACTION_CHANGE_APPS_ICON_SIZE ="ACTION_CHANGE_APPS_ICON_SIZE";
    public  static final String ACTION_CHANGE_APPS_ORDER ="ACTION_CHANGE_APPS_ORDER";
    public  static final String ACTION_CHANGE_APPS_LAYOUT ="ACTION_CHANGE_APPS_LAYOUT";
    public  static final String ACTION_CHANGE_APPS_ANIM ="ACTION_CHANGE_APPS_ANIM";
    public  static final String ACTION_LAUNCH_ANIMATION_ENABLE ="ACTION_LAUNCH_ANIMATION_ENABLE";
    public  static final String ACTION_LAUNCH_VIBRATION_ENABLE ="ACTION_LAUNCH_VIBRATION_ENABLE";




    int defaultSpinnerValue = 1;
    int defaultModeValue = 0;


    public void onCreate() {
        super.onCreate();

        SwitcherApplication.serviceIsRunning.set(true);

        Notification notification = KamikadzeService.getNotification(this);
        startForeground(KamikadzeService.NOTIFICATION_ID, notification);
        if(Build.VERSION.SDK_INT < 25) {
            Intent kamikadzeIntent = new Intent(this, KamikadzeService.class);
            startService(kamikadzeIntent);
        }



        //DaggerSwitcherComponent.builder().switcherModule(new SwitcherModule(getApplication())).build().inject(this);

        mSettings = getApplication().getSharedPreferences(SwitcherApplication.APP_PREFERENCES, Context.MODE_PRIVATE);
        mEditor = mSettings.edit();
        initialise();
        update();
        mEditor.putBoolean(SwitcherApplication.APP_PREFERENCES_SERVICE_FIRST_LAUNCH, true);
        mEditor.apply();
    }

    private void initialise()
    {
        int maxCount = 5;
        maxCount = mSettings.getInt(SwitcherApplication.APP_PREFERENCES_APP_COUNT, maxCount) + 1;

        createAppSwitcher(maxCount);
        createWindowContainer(maxCount);
    }

    private void createAppSwitcher(int maxCount)
    {
        PackageManager packageManager = getApplication().getPackageManager();
        UsageStatsManager usageStatsManager = (UsageStatsManager) getApplication().getSystemService(Activity.USAGE_STATS_SERVICE);
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        boolean useAnimation = mSettings.getBoolean(SwitcherApplication.APP_PREFERENCES_APP_USE_ANIMATION, true);
        boolean useVibration = mSettings.getBoolean(SwitcherApplication.APP_PREFERENCES_APP_USE_VIBRATION, false);

        appSwitcher = new AppSwitcher(getBaseContext(), usageStatsManager, packageManager, vibrator, maxCount, useAnimation, useVibration);
    }

    private void createWindowContainer(int maxCount)
    {
        WindowManager windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);

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
        buttonPosition = mSettings.getInt(SwitcherApplication.APP_PREFERENCES_BUTTON_POSITION, buttonPosition);
        buttonThickness = mSettings.getInt(SwitcherApplication.APP_PREFERENCES_BUTTON_THICKNESS, buttonThickness);
        buttonLength = mSettings.getInt(SwitcherApplication.APP_PREFERENCES_BUTTON_LENGTH, buttonLength);
        butPortraitY = mSettings.getInt(SwitcherApplication.APP_PREFERENCES_BUTTON_Y_PORTRAIT, butPortraitY);
        butPortraitX = mSettings.getInt(SwitcherApplication.APP_PREFERENCES_BUTTON_X_PORTRAIT, butPortraitX);
        butLandscapeY = mSettings.getInt(SwitcherApplication.APP_PREFERENCES_BUTTON_Y_LANDSCAPE, butLandscapeY);
        butLandscapeX = mSettings.getInt(SwitcherApplication.APP_PREFERENCES_BUTTON_X_LANDSCAPE, butLandscapeX);
        distanceY = mSettings.getInt(SwitcherApplication.APP_PREFERENCES_APP_Y_DISTANCE, distanceY);
        distanceX = mSettings.getInt(SwitcherApplication.APP_PREFERENCES_APP_X_DISTANCE, distanceX);
        iconSize = mSettings.getInt(SwitcherApplication.APP_PREFERENCES_APP_ICON_SIZE, iconSize);
        avoidKeyboard = mSettings.getBoolean(SwitcherApplication.APP_PREFERENCES_BUTTON_AVOID_KEYBOARD, false);
        appOrder = mSettings.getInt(SwitcherApplication.APP_PREFERENCES_APP_ORDER, 0) == 0;
        appLayout = mSettings.getInt(SwitcherApplication.APP_PREFERENCES_APP_LAYOUT, appLayout);
        appAnim = mSettings.getInt(SwitcherApplication.APP_PREFERENCES_APP_ANIM, appAnim);
        buttonColor = mSettings.getInt(SwitcherApplication.APP_PREFERENCES_BUTTON_COLOR, buttonColor);

        if(mSettings.contains(SwitcherApplication.APP_PREFERENCES_BUTTON_X_PORTRAIT)) {
            winContainer = new WindowContainer(this, this, windowManager, maxCount, pointCount,
                    appLayout, appOrder, buttonPosition, buttonThickness, buttonLength, butPortraitX, butPortraitY, butLandscapeX,
                    butLandscapeY, iconSize, distanceX, distanceY, appAnim, getResources().getConfiguration().orientation, buttonColor, avoidKeyboard);
        } else {
            winContainer = new WindowContainer(this, this, windowManager, maxCount, pointCount, getResources().getConfiguration().orientation,
                    appLayout, appOrder, buttonPosition, buttonThickness, buttonLength, buttonColor, iconSize, avoidKeyboard);
        }
        winContainer.dragFloatingButton(mSettings.getBoolean(SwitcherService.ACTION_ALLOW_DRAG_BUTTON, false));
        winContainer.dragIconBar(mSettings.getBoolean(SwitcherService.ACTION_ALLOW_DRAG_APPS, false));
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null)
            winContainer.hideIconBar();
        if(intent != null && intent.getAction() != null)
        {
            if (intent.getAction().equals(ACTION_FINISH)) {
                winContainer.removeViews();
                stopSelf();
            }
            else  if( intent.getAction().contains(ACTION_ALLOW_DRAG_BUTTON))
            {
                winContainer.dragFloatingButton(intent.getIntExtra(SwitcherApplication.PARAM, defaultModeValue) == 1);
            }
            else  if( intent.getAction().contains(ACTION_CHANGE_BUTTON_POSITION))
            {
                winContainer.changeButtonPosition(intent.getIntExtra(SwitcherApplication.PARAM, defaultModeValue));
            }
            else  if( intent.getAction().contains(ACTION_ALLOW_DRAG_APPS))
            {
                winContainer.dragIconBar(intent.getIntExtra(SwitcherApplication.PARAM, defaultModeValue) == 1);
            }
            else  if( intent.getAction().contains(ACTION_CHANGE_BUTTON_THICKNESS))
            {
                winContainer.changeButtonThickness(intent.getIntExtra(SwitcherApplication.PARAM, defaultSpinnerValue));
            }
            else if( intent.getAction().contains(ACTION_CHANGE_BUTTON_LENGTH))
            {
                winContainer.changeButtonLength(intent.getIntExtra(SwitcherApplication.PARAM, defaultSpinnerValue));
            }
            else if(intent.getAction().equals(ACTION_CHANGE_BUTTON_COLOR))
            {
                winContainer.setButtonColor(intent.getIntExtra(SwitcherApplication.PARAM, defaultModeValue));
            }
            else if(intent.getAction().equals(ACTION_CHANGE_APPS_COUNT))
            {
                int maxCount = intent.getIntExtra(SwitcherApplication.PARAM, defaultModeValue) + 1;
                appSwitcher.setMaxCount(maxCount);
                winContainer.setMaxCount(maxCount);
            }
            else if(intent.getAction().equals(ACTION_CHANGE_APPS_ICON_SIZE))
            {
                int iconSize = intent.getIntExtra(SwitcherApplication.PARAM, defaultSpinnerValue);
                winContainer.changeIconSize(iconSize);
            }
            else if(intent.getAction().equals(ACTION_CHANGE_APPS_ORDER))
            {
                boolean appOrder = intent.getIntExtra(SwitcherApplication.PARAM, 0) == 0;
                winContainer.changeIconOrder(appOrder);
            }
            else if(intent.getAction().equals(ACTION_APPS_VISIBILITY))
            {
                boolean activityIsVisible = intent.getIntExtra(SwitcherApplication.PARAM, 0) == 0;
                if(activityIsVisible) {
                    winContainer.showIconBar();
                }
                else {
                   winContainer.hideIconBar();
                }
            }
            else if(intent.getAction().equals(ACTION_LAUNCH_ANIMATION_ENABLE))
            {
                appSwitcher.useAnimation(intent.getIntExtra(SwitcherApplication.PARAM, 1) == 1);
            }
            else if(intent.getAction().equals(ACTION_LAUNCH_VIBRATION_ENABLE))
            {
                appSwitcher.useVibration(intent.getIntExtra(SwitcherApplication.PARAM, 1) == 1);
            }
            else if(intent.getAction().equals(ACTION_BUTTON_AVOID_KEYBOARD))
            {
                winContainer.avoidKeyboard(intent.getIntExtra(SwitcherApplication.PARAM, 1) == 1);
            }
            else if(intent.getAction().equals(ACTION_CHANGE_APPS_LAYOUT))
            {
               int applayout = intent.getIntExtra(SwitcherApplication.PARAM, 0);
                winContainer.changeIconBarLayout(applayout);

            }
            else if(intent.getAction().equals(ACTION_CHANGE_APPS_ANIM))
            {
                int animation = intent.getIntExtra(SwitcherApplication.PARAM, 0);
                winContainer.setAnim( animation);
            }


        }
        return START_STICKY;
    }


    private void update(){
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
    public void saveWindowPositions()
    {
        Point pos = winContainer.getButtonPortraitPosition();
        mEditor.putInt(SwitcherApplication.APP_PREFERENCES_BUTTON_X_PORTRAIT, pos.x);
        mEditor.putInt(SwitcherApplication.APP_PREFERENCES_BUTTON_Y_PORTRAIT, pos.y);
        pos = winContainer.getButtonLandscapePosition();
        mEditor.putInt(SwitcherApplication.APP_PREFERENCES_BUTTON_X_LANDSCAPE, pos.x);
        mEditor.putInt(SwitcherApplication.APP_PREFERENCES_BUTTON_Y_LANDSCAPE, pos.y);
        pos = winContainer.getIconBarDistance();
        mEditor.putInt(SwitcherApplication.APP_PREFERENCES_APP_X_DISTANCE, pos.x);
        mEditor.putInt(SwitcherApplication.APP_PREFERENCES_APP_Y_DISTANCE, pos.y);
        mEditor.apply();
    }

}
