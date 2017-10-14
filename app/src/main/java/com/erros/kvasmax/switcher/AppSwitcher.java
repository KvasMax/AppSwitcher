package com.erros.kvasmax.switcher;

import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by minimax on 14.10.17.
 */

public class AppSwitcher {

    private Context context;
    private UsageStatsManager usageStatsManager;
    private PackageManager packageManager;
    private Vibrator vibrator;
    private SwitcherContainer appContainer;

    private int period = 30; //minutes
    private int vibrationDuration = 10;
    private boolean useAnimation = true;
    private boolean useVibration = false;

    public AppSwitcher(Context context, UsageStatsManager usageStatsManager, PackageManager packageManager, Vibrator vibrator,
                       int maxCount, boolean useAnimation, boolean useVibration)
    {
        this.context = context;
        this.usageStatsManager = usageStatsManager;
        this.packageManager = packageManager;
        this.vibrator = vibrator;
        this.appContainer = new SwitcherContainer(packageManager, maxCount);
        this.useAnimation = useAnimation;
        this.useVibration = useVibration;
    }

    public void setMaxCount(int maxCount)
    {
        this.appContainer.setMaxCount(maxCount);
    }

    public void update()
    {
        List<String> recentApps = getRecentApps();
        appContainer.updateList(recentApps);
    }
    public ArrayList<Drawable> getIcons()
    {
        return appContainer.getIcons(packageManager);
    }

    private List<String> getRecentApps()
    {
        long currentTime = System.currentTimeMillis();
        int period = this.period;
        List<UsageStats> usageStats;
        List<String> recentApps;
        do {
            usageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 1000 * 60 * period, currentTime);
            sortUsageStats(usageStats);
            List<String> usedApps = new ArrayList<>();
            for(UsageStats stat: usageStats)
            {
                String packageName = stat.getPackageName();
                if(!usedApps.contains(packageName) && appContainer.contains(stat.getPackageName()))
                {
                    usedApps.add(packageName);
                }
            }

            UsageEvents usageEvents = usageStatsManager.queryEvents( currentTime - 1000 * 60 * period, currentTime);
            UsageEvents.Event event = new UsageEvents.Event();
            ArrayList<String> foregroundApps = new ArrayList<>();
            while (usageEvents.hasNextEvent())
            {
                usageEvents.getNextEvent(event);
                if(event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND && !foregroundApps.contains(event.getPackageName())) {
                    foregroundApps.add(event.getPackageName());
                }
            }

            recentApps = new ArrayList<>();
            for(String app: usedApps)
            {
                if(foregroundApps.contains(app))
                {
                    recentApps.add(app);
                }
            }

            period *= 5;
            if (period > 43200)
                break;
        }
        while(recentApps.size() <= appContainer.getMaxCount() + 1 );

        return recentApps;
    }

    public void startApplication(int position) {

        if (appContainer.getRecentAppCount() > position) {
            AppInfo appInf = appContainer.getRecentApp(position);
            try {
                Intent intent = //getPackageManager().getLaunchIntentForPackage(appInf.getPackageName());
                        new Intent(Intent.ACTION_MAIN);
                intent.setClassName(appInf.getPackageName(), appInf.getClassname());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //  in.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); // with animation or not
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                if(appContainer.currentAppIsLauncher)
                {
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                    pendingIntent.send();
                } else {
                    if(useAnimation) {
                        ActivityOptions options =
                                ActivityOptions.makeCustomAnimation(context, R.anim.fade_in, R.anim.fade_up_out);
                        //  ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.show_from_bottom,R.anim.move_away);
                        context.startActivity(intent, options.toBundle());
                    } else {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        context.startActivity(intent);
                    }
                }
                if(useVibration)
                {
                    if (Build.VERSION.SDK_INT >= 26) {
                        vibrator.vibrate(VibrationEffect.createOneShot(vibrationDuration, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(vibrationDuration);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void useAnimation(boolean value)
    {
        this.useAnimation = value;
    }
    public void useVibration(boolean value)
    {
        this.useVibration = value;
    }

    private void sortUsageStats(List<UsageStats> stats){
        Collections.sort(stats, new Comparator<UsageStats>() {
            @Override
            public int compare(UsageStats lhs, UsageStats rhs) {
                if (lhs.getLastTimeUsed() > rhs.getLastTimeUsed())
                    return -1;
                else if (lhs.getLastTimeUsed() < rhs.getLastTimeUsed()) return 1;
                else return 0;
            }
        });
    }
}
