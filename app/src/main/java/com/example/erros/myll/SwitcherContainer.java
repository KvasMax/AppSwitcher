package com.example.erros.myll;
import android.app.usage.UsageStats;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SwitcherContainer {

    static int maxcount;
    public ArrayList<AppInfo> switchapps;
    public SwitcherContainer(PackageManager pm, String ownpackage, int max){

        maxcount=max;
        Intent inten=new Intent(Intent.ACTION_MAIN);
        inten.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> rInfo=pm.queryIntentActivities(inten, 0);
        inten.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> list=pm.queryIntentActivities(inten, 0);
        for(int i=0; i<rInfo.size();i++){

            if(rInfo.get(i).activityInfo.packageName.contains(ownpackage)) rInfo.remove(i);
            else
            for(ResolveInfo ri: list) {
                if(rInfo.get(i).activityInfo.packageName.contains(ri.activityInfo.packageName)) {
                    rInfo.remove(i);
                    continue;
                }
            }
        }
        AppContainer.Initialise(pm, rInfo);
        //new Intent(Intent.CATEGORY_HOME), PackageManager.GET_INTENT_FILTERS
        switchapps=new ArrayList<AppInfo>();
    }
    public void update( List<UsageStats> stats){
        switchapps=new ArrayList<AppInfo>();
        sortApps(stats);
        AppInfo app;
        boolean match=false;
        for (UsageStats stat: stats){
            app=AppContainer.getApp(stat.getPackageName());
            if(!match){
                match=true;
                if(app!=null) continue;
            }
            if(app!=null)switchapps.add(app);
            else continue;
            if(switchapps.size()==maxcount) break;
        }

    }
    public void sortApps(List<UsageStats> stats){
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
