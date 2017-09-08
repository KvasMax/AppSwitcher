package com.example.erros.myll;

/**
 * Created by erros on 25.04.16.
 */
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class FloatingSwitcher extends Service implements ISwitcherService {

    private SharedPreferences mSettings;
    UsageStatsManager mUsageStatsManager;
    ActivityManager am;

    @Inject
    SwitcherContainer appSwither;


    FloatingWindowContainer winContainer;

    //Constantsinc
    public  static final String ACTION_FINISH ="finish yourself, dickhead";

    public  static final String ACTION_CHANGE_BUTTON_X ="change x, fucking asshole";
    public  static final String ACTION_CHANGE_BUTTON_Y ="change y, fucking asshole";
    public  static final String ACTION_CHANGE_BUTTON_WiDTH ="change width, fucking asshole";
    public  static final String ACTION_CHANGE_BUTTON_HEIGHT ="change height, fucking asshole";
    public  static final String ACTION_CHANGE_BUTTON_SWEEPDIRECTION="sweep sweep";

    public  static final String ACTION_APPS_VISIBILITY="ACTION_APPS_VISIBILITY";
    public  static final String ACTION_CHANGE_APPS_X="appps xx xx";
    public  static final String ACTION_CHANGE_APPS_Y="apppsyyy y y yyy";
    public  static final String ACTION_CHANGE_APPS_COUNT ="apppsyyy y y yyy count";
    public  static final String ACTION_CHANGE_APPS_ICON_SIZE ="icon screenSize";
    public  static final String ACTION_CHANGE_APPS_ORDER ="ACTION_CHANGE_APPS_ORDER";
    public  static final String ACTION_CHANGE_APPS_LAYOUT ="ACTION_CHANGE_APPS_LAYOUT";
    public  static final String ACTION_CHANGE_APPS_ANIM ="ACTION_CHANGE_APPS_ANIM";

    public  static final String PARAM="this is my new property, fucking switcher";
    public  static final String RECEIVER = "paramparampam";

    public static final String APP_PREFERENCES="switchersettings";
    public static final String APP_PREFERENCES_POINT_COUNT="switchersettings";
    public static final String APP_PREFERENCES_BUTTON_X="buttonX";
    public static final String APP_PREFERENCES_BUTTON_Y="buttonY";
    public static final String APP_PREFERENCES_BUTTON_WIDTH="buttonWidth";
    public static final String APP_PREFERENCES_BUTTON_HEIGHT="buttonHeight";
    public static final String APP_PREFERENCES_SWEEP_DIRECTION="sweepppdirectionsweep";

    public static final String APP_PREFERENCES_APP_COUNT = "APP_PREFERENCES_APP_COUNT";
    public static final String APP_PREFERENCES_APP_ICON_SIZE = "APP_PREFERENCES_APP_ICON_SIZE";
    public static final String APP_PREFERENCES_APP_ORDER = "APP_PREFERENCES_APP_ORDER";
    public static final String APP_PREFERENCES_APP_LAYOUT = "APP_PREFERENCES_APP_LAYOUT";
    public static final String APP_PREFERENCES_APP_ANIM = "APP_PREFERENCES_APP_ANIM";
    public static final String APP_PREFERENCES_APP_X = "APP_PREFERENCES_APP_X";
    public static final String APP_PREFERENCES_APP_Y = "APP_PREFERENCES_APP_Y";

    int defaultSpinnerValue = 1;
    int defaultModeValue = 0;

    public void onCreate() {
        super.onCreate();

        mUsageStatsManager = (UsageStatsManager) getApplication().getSystemService(Activity.USAGE_STATS_SERVICE);
        am = (ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);

        //DaggerSwitcherComponent.builder().switcherModule(new SwitcherModule(getApplication())).build().inject(this);

        mSettings = getApplication().getSharedPreferences(FloatingSwitcher.APP_PREFERENCES, Context.MODE_PRIVATE);
        initialise();
        update();
    }

    private void initialise()
    {
        int maxCount = 5;
        maxCount = mSettings.getInt(APP_PREFERENCES_APP_COUNT, maxCount) + 1;
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

        int pointCount = 20;
        int butWidth = 10;
        int butHeight = 10;
        int butY = 10;
        int butX = 10;
        int appY = 0;
        int appX = 0;
        int sweepDirection = 0;
        int iconSize = 0;
        int applayout = 0;
        int appAnim = 0;
        boolean appOrder = true;
        if(!mSettings.contains(FloatingSwitcher.APP_PREFERENCES_POINT_COUNT))
            return;
        pointCount = mSettings.getInt(APP_PREFERENCES_POINT_COUNT, pointCount);
        butWidth = mSettings.getInt(APP_PREFERENCES_BUTTON_WIDTH, butWidth);
        butHeight = mSettings.getInt(APP_PREFERENCES_BUTTON_HEIGHT, butHeight);
        butY = mSettings.getInt(APP_PREFERENCES_BUTTON_Y, butY);
        butX = mSettings.getInt(APP_PREFERENCES_BUTTON_X, butX);
        appY = mSettings.getInt(APP_PREFERENCES_APP_Y, appY);
        appX = mSettings.getInt(APP_PREFERENCES_APP_X, appX);
        sweepDirection = mSettings.getInt(APP_PREFERENCES_SWEEP_DIRECTION, sweepDirection);

        iconSize = mSettings.getInt(APP_PREFERENCES_APP_ICON_SIZE, iconSize);
        appOrder = mSettings.getInt(APP_PREFERENCES_APP_ORDER, 0) == 0;
        applayout = mSettings.getInt(APP_PREFERENCES_APP_LAYOUT, applayout);
        appAnim = mSettings.getInt(APP_PREFERENCES_APP_ANIM, appAnim);


        winContainer = new FloatingWindowContainer(this, this, windowManager, maxCount, pointCount,
                applayout, appOrder, butWidth, butHeight, butX, butY, sweepDirection, iconSize, appX, appY, appAnim);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null)
            winContainer.hideIconPanel();
        if(intent != null && intent.getAction() != null)
        {
            if (intent.getAction().equals(ACTION_FINISH)) {
                winContainer.removeViews();
                stopSelf();
            }
            else if (intent.getAction().equals(ACTION_CHANGE_BUTTON_X))
            {
                winContainer.changeButtonX(intent.getIntExtra(PARAM, defaultSpinnerValue));
            }
            else if (intent.getAction().equals(ACTION_CHANGE_BUTTON_Y))
            {
                 winContainer.changeButtonY(intent.getIntExtra(PARAM, defaultSpinnerValue));
            }
            else if( intent.getAction().contains(ACTION_CHANGE_BUTTON_WiDTH))
            {
                winContainer.changeButonWidth(intent.getIntExtra(PARAM, defaultSpinnerValue));
            }else if( intent.getAction().contains(ACTION_CHANGE_BUTTON_HEIGHT))
            {
                winContainer.changeButonHeight(intent.getIntExtra(PARAM, defaultSpinnerValue));
            }
            else if(intent.getAction().equals(ACTION_CHANGE_BUTTON_SWEEPDIRECTION))
            {
                winContainer.setSweepDirection(intent.getIntExtra(PARAM, defaultModeValue));
            }
            else if(intent.getAction().equals(ACTION_CHANGE_APPS_X))
            {
                winContainer.ChangeIconPanelX(intent.getIntExtra(PARAM, defaultSpinnerValue));
            }
            else if(intent.getAction().equals(ACTION_CHANGE_APPS_Y))
            {
                winContainer.ChangeIconPanelY(intent.getIntExtra(PARAM, defaultSpinnerValue));
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
                    winContainer.showIconPanel();
                }
                else {
                   winContainer.hideIconPanel();
                }

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
    private void update(){
        long currentTime = System.currentTimeMillis();
        // get usage stats for the last 10 seconds
        int minutes = 5;
        List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 1000 * 60 * minutes, currentTime);
        int period = minutes;
        while(stats.size() < appSwither.getMaxCount()){
            period *= minutes;
            stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 1000 * 60 * period, currentTime);
            if(period > 43200)
                break;
        }
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
            for(AppInfo app : appSwither.switchapps)
            {
                icons.add(app.getIcon());
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
                Intent in = //getPackageManager().getLaunchIntentForPackage(appInf.getPackagename());
                new Intent(Intent.ACTION_MAIN);
                                in.setClassName(appInf.getPackagename(), appInf.getClassname());
                                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                              //  in.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); // with animation or not
                                in.addCategory(Intent.CATEGORY_LAUNCHER);
                Log.e("TEST", appSwither.currentAppIsLauncher + "");
                if(appSwither.currentAppIsLauncher)
                {
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, in, 0);
                    pendingIntent.send();
                } else {
                    ActivityOptions options =
                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.show_from_bottom,R.anim.move_away);
                    startActivity(in, options.toBundle());
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
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while(true) {
                        sleep(500);
                        int butY = 10;
                        int butX = 10;
                        int appY = 0;
                        int appX = 0;
                        butY = mSettings.getInt(APP_PREFERENCES_BUTTON_Y, butY);
                        butX = mSettings.getInt(APP_PREFERENCES_BUTTON_X, butX);
                        appY = mSettings.getInt(APP_PREFERENCES_APP_Y, appY);
                        appX = mSettings.getInt(APP_PREFERENCES_APP_X, appX);
                        winContainer.changeButtonX(butX);
                        winContainer.changeButtonY(butY);
                        winContainer.ChangeIconPanelX(appX);
                        winContainer.ChangeIconPanelY(appY);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();

    }

}
