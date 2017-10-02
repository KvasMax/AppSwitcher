package com.erros.kvasmax.switcher;
/**
 * Created by erros on 25.04.16.
 */
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class FloatingSwitcher extends Service implements ISwitcherService {

    private SharedPreferences mSettings;
    private SharedPreferences.Editor mEditor;
    UsageStatsManager mUsageStatsManager;
    ActivityManager am;

    @Inject
    SwitcherContainer appSwither;


    FloatingWindowContainer winContainer;

    //Constantsinc
    public  static final String ACTION_FINISH ="ACTION_FINISH";

    public  static final String ACTION_ALLOW_DRAG_BUTTON ="ACTION_ALLOW_DRAG_BUTTON";
    public  static final String ACTION_CHANGE_BUTTON_WiDTH ="ACTION_CHANGE_BUTTON_WiDTH";
    public  static final String ACTION_CHANGE_BUTTON_HEIGHT ="ACTION_CHANGE_BUTTON_HEIGHT";
    public  static final String ACTION_CHANGE_BUTTON_SWEEPDIRECTION="ACTION_CHANGE_BUTTON_SWEEPDIRECTION";
    public  static final String ACTION_CHANGE_BUTTON_COLOR = "ACTION_CHANGE_BUTTON_COLOR";

    public  static final String ACTION_APPS_VISIBILITY="ACTION_APPS_VISIBILITY";
    public  static final String ACTION_ALLOW_DRAG_APPS ="ACTION_ALLOW_DRAG_APPS";
    public  static final String ACTION_CHANGE_APPS_COUNT ="ACTION_CHANGE_APPS_COUNT";
    public  static final String ACTION_CHANGE_APPS_ICON_SIZE ="ACTION_CHANGE_APPS_ICON_SIZE";
    public  static final String ACTION_CHANGE_APPS_ORDER ="ACTION_CHANGE_APPS_ORDER";
    public  static final String ACTION_CHANGE_APPS_LAYOUT ="ACTION_CHANGE_APPS_LAYOUT";
    public  static final String ACTION_CHANGE_APPS_ANIM ="ACTION_CHANGE_APPS_ANIM";
    public  static final String ACTION_LAUNCH_ANIMATION_ENABLE ="ACTION_LAUNCH_ANIMATION_ENABLE";

    public  static final String PARAM="SWITCHER_PARAM";
    public  static final String RECEIVER = "paramparampam";

    public static final String APP_PREFERENCES="APP_PREFERENCES";
    public static final String APP_PREFERENCES_POINT_COUNT="APP_PREFERENCES_POINT_COUNT";
    public static final String APP_PREFERENCES_BUTTON_X_PORTRAIT ="APP_PREFERENCES_BUTTON_X_PORTRAIT";
    public static final String APP_PREFERENCES_BUTTON_Y_PORTRAIT ="APP_PREFERENCES_BUTTON_Y_PORTRAIT";
    public static final String APP_PREFERENCES_BUTTON_X_LANDSCAPE ="APP_PREFERENCES_BUTTON_X_LANDSCAPE";
    public static final String APP_PREFERENCES_BUTTON_Y_LANDSCAPE ="APP_PREFERENCES_BUTTON_Y_LANDSCAPE";
    public static final String APP_PREFERENCES_BUTTON_WIDTH="APP_PREFERENCES_BUTTON_WIDTH";
    public static final String APP_PREFERENCES_BUTTON_HEIGHT="APP_PREFERENCES_BUTTON_HEIGHT";
    public static final String APP_PREFERENCES_SWEEP_DIRECTION="APP_PREFERENCES_SWEEP_DIRECTION";
    public static final String APP_PREFERENCES_BUTTON_COLOR="APP_PREFERENCES_BUTTON_COLOR";

    public static final String APP_PREFERENCES_APP_COUNT = "APP_PREFERENCES_APP_COUNT";
    public static final String APP_PREFERENCES_APP_ICON_SIZE = "APP_PREFERENCES_APP_ICON_SIZE";
    public static final String APP_PREFERENCES_APP_ORDER = "APP_PREFERENCES_APP_ORDER";
    public static final String APP_PREFERENCES_APP_LAYOUT = "APP_PREFERENCES_APP_LAYOUT";
    public static final String APP_PREFERENCES_APP_ANIM = "APP_PREFERENCES_APP_ANIM";
    public static final String APP_PREFERENCES_APP_X_DISTANCE = "APP_PREFERENCES_APP_X_DISTANCE";
    public static final String APP_PREFERENCES_APP_Y_DISTANCE = "APP_PREFERENCES_APP_Y_DISTANCE";
    public static final String APP_PREFERENCES_APP_X_LANDSCAPE = "APP_PREFERENCES_APP_X_LANDSCAPE";
    public static final String APP_PREFERENCES_APP_Y_LANDSCAPE = "APP_PREFERENCES_APP_Y_LANDSCAPE";
    public static final String APP_PREFERENCES_APP_USE_ANIMATION = "APP_PREFERENCES_APP_USE_ANIMATION";



    int defaultSpinnerValue = 1;
    int defaultModeValue = 0;
    boolean useAnimation = true;

    public void onCreate() {
        super.onCreate();


        Notification notification = KamikadzeService.getNotification(this);
        startForeground(KamikadzeService.NOTIFICATION_ID, notification);
        if(Build.VERSION.SDK_INT < 25) {
            Intent kamikadzeIntent = new Intent(this, KamikadzeService.class);
            startService(kamikadzeIntent);
        }

        mUsageStatsManager = (UsageStatsManager) getApplication().getSystemService(Activity.USAGE_STATS_SERVICE);
        am = (ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);

        //DaggerSwitcherComponent.builder().switcherModule(new SwitcherModule(getApplication())).build().inject(this);

        mSettings = getApplication().getSharedPreferences(FloatingSwitcher.APP_PREFERENCES, Context.MODE_PRIVATE);
        mEditor = mSettings.edit();
        initialise();
        update();
    }

    private void initialise()
    {
        int maxCount = 5;
        maxCount = mSettings.getInt(APP_PREFERENCES_APP_COUNT, maxCount) + 1;
        useAnimation = mSettings.getBoolean(APP_PREFERENCES_APP_USE_ANIMATION, useAnimation);
        createAppSwitcher(maxCount);
        createWindowContainer(maxCount);
    }

    private void createAppSwitcher(int maxCount)
    {
        PackageManager pm = getApplication().getPackageManager(); 
        appSwither = new SwitcherContainer(pm,getPackageName(), maxCount);
    }

    private void createWindowContainer(int maxCount)
    {
        WindowManager windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);

        int pointCount = 100;
        int butWidth = 200;
        int butHeight = 200;
        int butPortraitY = 0;
        int butPortraitX = 200;
        int butLandscapeY = 10;
        int butLandscapeX = 10;
        int distanceY = 0;
        int distanceX = -200;
        int sweepDirection = 0;
        int iconSize = 0;
        int applayout = 0;
        int appAnim = 0;
        int buttonColor = Color.RED;
        boolean appOrder = true;
        if(!mSettings.contains(FloatingSwitcher.APP_PREFERENCES_POINT_COUNT))
            return;
        pointCount = mSettings.getInt(APP_PREFERENCES_POINT_COUNT, pointCount);
        butWidth = mSettings.getInt(APP_PREFERENCES_BUTTON_WIDTH, butWidth);
        butHeight = mSettings.getInt(APP_PREFERENCES_BUTTON_HEIGHT, butHeight);
        butPortraitY = mSettings.getInt(APP_PREFERENCES_BUTTON_Y_PORTRAIT, butPortraitY);
        butPortraitX = mSettings.getInt(APP_PREFERENCES_BUTTON_X_PORTRAIT, butPortraitX);
        butLandscapeY = mSettings.getInt(APP_PREFERENCES_BUTTON_Y_LANDSCAPE, butLandscapeY);
        butLandscapeX = mSettings.getInt(APP_PREFERENCES_BUTTON_X_LANDSCAPE, butLandscapeX);
        distanceY = mSettings.getInt(APP_PREFERENCES_APP_Y_DISTANCE, distanceY);
        distanceX = mSettings.getInt(APP_PREFERENCES_APP_X_DISTANCE, distanceX);
        sweepDirection = mSettings.getInt(APP_PREFERENCES_SWEEP_DIRECTION, sweepDirection);

        iconSize = mSettings.getInt(APP_PREFERENCES_APP_ICON_SIZE, iconSize);
        appOrder = mSettings.getInt(APP_PREFERENCES_APP_ORDER, 0) == 0;
        applayout = mSettings.getInt(APP_PREFERENCES_APP_LAYOUT, applayout);
        appAnim = mSettings.getInt(APP_PREFERENCES_APP_ANIM, appAnim);
        buttonColor = mSettings.getInt(APP_PREFERENCES_BUTTON_COLOR, buttonColor);

        winContainer = new FloatingWindowContainer(this, this, windowManager, maxCount, pointCount,
                applayout, appOrder, butWidth, butHeight, butPortraitX, butPortraitY, butLandscapeX,
                butLandscapeY, sweepDirection, iconSize, distanceX, distanceY, appAnim, getResources().getConfiguration().orientation, buttonColor);

        winContainer.dragFloatingButton(mSettings.getBoolean(FloatingSwitcher.ACTION_ALLOW_DRAG_BUTTON, false));
        winContainer.dragIconBar(mSettings.getBoolean(FloatingSwitcher.ACTION_ALLOW_DRAG_APPS, false));
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
                winContainer.dragFloatingButton(intent.getIntExtra(PARAM, defaultModeValue) == 1);
            }
            else  if( intent.getAction().contains(ACTION_ALLOW_DRAG_APPS))
            {
                winContainer.dragIconBar(intent.getIntExtra(PARAM, defaultModeValue) == 1);
            }
            else  if( intent.getAction().contains(ACTION_CHANGE_BUTTON_WiDTH))
            {
                winContainer.changeButtonWidth(intent.getIntExtra(PARAM, defaultSpinnerValue));
            }else if( intent.getAction().contains(ACTION_CHANGE_BUTTON_HEIGHT))
            {
                winContainer.changeButtonHeight(intent.getIntExtra(PARAM, defaultSpinnerValue));
            }
            else if(intent.getAction().equals(ACTION_CHANGE_BUTTON_SWEEPDIRECTION))
            {
                winContainer.setSweepDirection(intent.getIntExtra(PARAM, defaultModeValue));
            }
            else if(intent.getAction().equals(ACTION_CHANGE_BUTTON_COLOR))
            {
                winContainer.setButtonColor(intent.getIntExtra(PARAM, defaultModeValue));
            }
            else if(intent.getAction().equals(ACTION_CHANGE_APPS_COUNT))
            {
                int maxCount = intent.getIntExtra(PARAM, defaultModeValue) + 1;
                appSwither.setMaxCount(maxCount);
                winContainer.setMaxCount(maxCount);
            }
            else if(intent.getAction().equals(ACTION_CHANGE_APPS_ICON_SIZE))
            {
                int iconSize = intent.getIntExtra(PARAM, defaultSpinnerValue);
                winContainer.changeIconSize(iconSize);
            }
            else if(intent.getAction().equals(ACTION_CHANGE_APPS_ORDER))
            {
                boolean appOrder = intent.getIntExtra(PARAM, 0) == 0;
                winContainer.changeIconOrder(appOrder);
            }
            else if(intent.getAction().equals(ACTION_APPS_VISIBILITY))
            {
                boolean activityIsVisible = intent.getIntExtra(PARAM, 0) == 0;
                if(activityIsVisible) {
                    winContainer.showIconBar();
                }
                else {
                   winContainer.hideIconBar();
                }

            }
            else if(intent.getAction().equals(ACTION_LAUNCH_ANIMATION_ENABLE))
            {
                useAnimation = intent.getIntExtra(PARAM, 1) == 1;
            }
            else if(intent.getAction().equals(ACTION_CHANGE_APPS_LAYOUT))
            {
               int applayout = intent.getIntExtra(PARAM, 0);
                winContainer.changeIconLayout(applayout);

            }
            else if(intent.getAction().equals(ACTION_CHANGE_APPS_ANIM))
            {
                int animation = intent.getIntExtra(PARAM, 0);
                winContainer.setAnim( animation);
            }


        }
        return START_STICKY;
    }
    private List<UsageStats> getRecentApps(int minutes)
    {
        long currentTime = System.currentTimeMillis();
        int period = minutes;
        List<UsageStats> stats;
        List<UsageStats> newStats;
        do {
            stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 1000 * 60 * period, currentTime);
            newStats = new ArrayList<>();
            UsageEvents usageEvents = mUsageStatsManager.queryEvents( currentTime - 1000 * 60 * period, currentTime);
            UsageEvents.Event event = new UsageEvents.Event();
            ArrayList<String> apps = new ArrayList<>();
            while (usageEvents.hasNextEvent())
            {
                usageEvents.getNextEvent(event);
                if(event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND && !apps.contains(event.getPackageName())) {
                    apps.add(event.getPackageName());
                }
            }
            for(UsageStats stat: stats)
            {
                if(apps.contains(stat.getPackageName()) && appSwither.contains(stat.getPackageName()))
                {
                    Log.e("PACKAGE", stat.getPackageName());
                    newStats.add(stat);
                }
            }
            Log.e("period", period + "");
            period *= minutes;
            if (period > 43200)
                break;
        }
        while(newStats.size() <= appSwither.getMaxCount() + 1 );
        Log.e("SIZE", newStats.size() + "");
        return newStats;
    }



    private void update(){

        // get usage stats for the last 10 seconds
        int minutes = 5;
        List<UsageStats> stats = getRecentApps(minutes);


        /*List<UsageStats> usStats = new ArrayList<>();
        for(UsageStats newStat : stats)
        {
            boolean found = false;
            for(UsageStats stat : usStats)
            {
                if(stat.getPackageName().equals(newStat.getPackageName()))
                    found = true;
            }
            if(!found)
                usStats.add(newStat);
        }*/
        appSwither.update(stats);
        int appCount = appSwither.switchapps.size();
        if(appCount != 0) {

            ArrayList<Drawable> icons = new ArrayList<>();
            PackageManager pm = getPackageManager();
            for(AppInfo app : appSwither.switchapps)
            {
                icons.add(app.getIcon(pm));
            }
            winContainer.setIconViews(icons);
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.lackOfApps, Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void startApplication(int position) {

        if (appSwither.switchapps.size() > position) {
            AppInfo appInf = appSwither.switchapps.get(position);
            try {
                Intent intent = //getPackageManager().getLaunchIntentForPackage(appInf.getPackageName());
                new Intent(Intent.ACTION_MAIN);
                                intent.setClassName(appInf.getPackageName(), appInf.getClassname());
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                              //  in.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); // with animation or not
                                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                if(appSwither.currentAppIsLauncher)
                {
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                    pendingIntent.send();
                } else {
                    Log.e("ANIM", useAnimation + "");
                    if(useAnimation) {
                        ActivityOptions options =
                                ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.fade_in, R.anim.fade_up_out);
                        //  ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.show_from_bottom,R.anim.move_away);
                        startActivity(intent, options.toBundle());
                    } else {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void updateAppList() {
        update();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }

        //winContainer.removeViews();
        //createWindowContainer(appSwither.getMaxCount());
        winContainer.updateScreenSize();
        final int orientation = newConfig.orientation;
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(500);
                     winContainer.rotateScreen(orientation);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();


    }
    public void saveWindowPositions()
    {
        Point pos = winContainer.getButtonPortraitPosition();
        mEditor.putInt(APP_PREFERENCES_BUTTON_X_PORTRAIT, pos.x);
        mEditor.putInt(APP_PREFERENCES_BUTTON_Y_PORTRAIT, pos.y);
        pos = winContainer.getButtonLandscapePosition();
        mEditor.putInt(APP_PREFERENCES_BUTTON_X_LANDSCAPE, pos.x);
        mEditor.putInt(APP_PREFERENCES_BUTTON_Y_LANDSCAPE, pos.y);
        pos = winContainer.getIconBarDistance();
        mEditor.putInt(APP_PREFERENCES_APP_X_DISTANCE, pos.x);
        mEditor.putInt(APP_PREFERENCES_APP_Y_DISTANCE, pos.y);
        mEditor.apply();
    }

}
