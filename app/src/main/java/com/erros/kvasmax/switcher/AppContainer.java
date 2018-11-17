package com.erros.kvasmax.switcher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

public class AppContainer {

    public boolean currentAppIsLauncher;
    private int maxCount;
    private String launcherPackage;

    private AppInfo[] appList;
    private ArrayList<AppInfo> recentApps = new ArrayList<>();

    public AppContainer(PackageManager packageManager, int maxCount) {

        this.maxCount = maxCount;
        appList = Utils.getApps(packageManager);

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        launcherPackage = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
    }

    private AppInfo getApp(String packageName) {
        for (AppInfo appInfo : appList) {
            if (appInfo.getPackageName().equals(packageName))
                return appInfo;
        }
        return null;
    }

    public void setMaxCount(int value) {
        this.maxCount = value;
    }

    public int getMaxCount() {
        return this.maxCount;
    }

    public int getRecentAppCount() {
        return this.recentApps.size();
    }

    public AppInfo getRecentApp(int position) {
        return this.recentApps.get(position);
    }

    public Drawable[] getIcons(PackageManager pm) {
        Drawable[] icons = new Drawable[recentApps.size()];
        int recentAppsCount = recentApps.size();
        for (int index = 0; index < recentAppsCount; index++) {
            icons[index] = recentApps.get(index).getIcon(pm);

        }
        return icons;
    }

    public boolean contains(String app) {
        return getApp(app) != null || app.equals(launcherPackage);
    }

    public void updateList(List<String> recentApps) {
        this.recentApps.clear();
        AppInfo appInfo;
        boolean match = false;
        for (String appName : recentApps) {
            appInfo = getApp(appName);
            if (!match) {
                match = true;
                currentAppIsLauncher = currentAppIsLauncher(appName);
                continue;
            }
            if (appInfo != null && !this.recentApps.contains(appInfo)) {
                this.recentApps.add(appInfo);
            }
            if (this.recentApps.size() == maxCount)
                break;
        }

    }

    private boolean currentAppIsLauncher(String packageName) {
        return packageName.equals(launcherPackage);
    }
}
