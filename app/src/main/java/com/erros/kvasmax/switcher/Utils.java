package com.erros.kvasmax.switcher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;

/**
 * Created by minimax on 11/4/17.
 */

public class Utils {

    public static AppInfo[] getApps(PackageManager packageManager) {

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> ordinaryApps = packageManager.queryIntentActivities(intent, 0);
        int ordinaryAppsCount = ordinaryApps.size();

        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> lauchers = packageManager.queryIntentActivities(intent, 0);

        for (int i = 0; i < ordinaryAppsCount; i++) {
            for (ResolveInfo ri : lauchers) {
                if (ordinaryApps.get(i).activityInfo.packageName.equals(ri.activityInfo.packageName)) {
                    ordinaryApps.remove(i);
                }
            }
        }
        AppInfo[] appList = new AppInfo[ordinaryApps.size()];
        for (int i = 0; i < ordinaryAppsCount; i++) {
            ResolveInfo resolveInfo = ordinaryApps.get(i);
            appList[i] = new AppInfo(resolveInfo.activityInfo.applicationInfo.packageName,
                    resolveInfo.activityInfo.name,
                    resolveInfo.loadLabel(packageManager).toString());
        }
        return appList;
    }
}
