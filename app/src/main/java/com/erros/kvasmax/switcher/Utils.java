package com.erros.kvasmax.switcher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by minimax on 11/4/17.
 */

public class Utils {

    public static List<AppInfo> getApps(PackageManager packageManager) {

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> ordinaryApps = packageManager.queryIntentActivities(intent, 0);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> lauchers = packageManager.queryIntentActivities(intent, 0);
        for(int i = 0; i < ordinaryApps.size();i++){
            for(ResolveInfo ri: lauchers) {
                if(ordinaryApps.get(i).activityInfo.packageName.contains(ri.activityInfo.packageName)) {
                    ordinaryApps.remove(i);
                    continue;
                }
            }
        }
        List<AppInfo> appList = new ArrayList<>(ordinaryApps.size());
        for(ResolveInfo ri: ordinaryApps){
            appList.add(new AppInfo(ri.activityInfo.applicationInfo.packageName, ri.activityInfo.name, ri.loadLabel(packageManager).toString()));
        }
        return appList;
    }
}
