package com.erros.kvasmax.switcher;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by erros on 23.04.16.
 */
public class AppContainer {

    public static ArrayList<AppInfo> applist = new ArrayList<>();

    public static void Initialise(PackageManager pm, List<ResolveInfo> rInfo){
        for(ResolveInfo ri: rInfo){
            applist.add(new AppInfo(ri.activityInfo.applicationInfo.packageName, ri.activityInfo.name/*, ri.loadIcon(pm)*/));
        }
    }

    public static Drawable getIcon(PackageManager pm, int pos){
        return   applist.get(pos).getIcon(pm);
    }

    public static AppInfo getApp(String packagename){
        for(AppInfo ri: applist){
            if(ri.getPackageName().contains(packagename)) return ri;
        }
        return null;
    }




}
