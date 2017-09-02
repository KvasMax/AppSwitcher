package com.example.erros.myll;

import android.app.Notification;
import android.app.usage.UsageStats;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.example.erros.myll.AppInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by erros on 23.04.16.
 */
public class AppContainer {

    public static ArrayList<AppInfo> applist = new ArrayList<>();

    public static void Initialise(PackageManager pm, List<ResolveInfo> rInfo){
        for(ResolveInfo ri: rInfo){
            applist.add(new AppInfo(ri.activityInfo.applicationInfo.packageName, ri.activityInfo.name, ri.loadIcon(pm)));
        }
    }

    public static Drawable getIcon(int pos){
        return   applist.get(pos).getIcon();
    }

    public static AppInfo getApp(String packagename){
        for(AppInfo ri: applist){
            if(ri.getPackagename().contains(packagename)) return ri;
        }
        return null;
    }




}
