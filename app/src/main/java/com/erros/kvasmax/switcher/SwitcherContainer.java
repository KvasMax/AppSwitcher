package com.erros.kvasmax.switcher;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

public class SwitcherContainer {

    public boolean currentAppIsLauncher;
    private int maxCount;
    private String launcherPackage;

    private ArrayList<AppInfo> recentApps;

    public SwitcherContainer(PackageManager packageManager, int maxCount){

        this.maxCount = maxCount;
        Intent inten = new Intent(Intent.ACTION_MAIN);
        inten.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> rInfo = packageManager.queryIntentActivities(inten, 0);
        inten.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> list = packageManager.queryIntentActivities(inten, 0);
        for(int i = 0; i < rInfo.size();i++){

            for(ResolveInfo ri: list) {
                if(rInfo.get(i).activityInfo.packageName.contains(ri.activityInfo.packageName)) {
                    rInfo.remove(i);
                    continue;
                }
            }
        }
        AppContainer.Initialise(packageManager, rInfo);
        //new Intent(Intent.CATEGORY_HOME), PackageManager.GET_INTENT_FILTERS
        recentApps = new ArrayList<>();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        launcherPackage = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
    }

    public void setMaxCount( int value )
    {
        this.maxCount = value;
    }
    public int getMaxCount( )
    {
        return this.maxCount;
    }
    public int getRecentAppCount( )
    {
        return this.recentApps.size();
    }
    public AppInfo getRecentApp(int position)
    {
        return this.recentApps.get(position);
    }

    public ArrayList<Drawable> getIcons(PackageManager pm)
    {
        ArrayList<Drawable> icons = new ArrayList<>();
        for(AppInfo app : this.recentApps)
        {
            icons.add(app.getIcon(pm));
        }
        return icons;
    }

    public boolean contains(String app)
    {
        return AppContainer.getApp(app) != null || app.equals(launcherPackage);
    }

    public void updateList(List<String> recentApps){
        this.recentApps = new ArrayList<>();
        AppInfo appInfo;
        boolean match = false;
        for (String appName: recentApps){
            appInfo = AppContainer.getApp(appName);
            if(!match){
                match = true;
                currentAppIsLauncher = currentAppIsLauncher(appName);
                continue;
            }
            if(appInfo != null && !this.recentApps.contains(appInfo)) {
                this.recentApps.add(appInfo);
            }
            if(this.recentApps.size() == maxCount)
                break;
        }

    }

    private boolean currentAppIsLauncher(String packageName)
    {
        return  packageName.equals(launcherPackage);
    }
}
