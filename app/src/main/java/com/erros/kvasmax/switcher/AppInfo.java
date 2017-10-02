package com.erros.kvasmax.switcher;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

/**
 * Created by erros on 23.04.16.
 */
public class AppInfo {

    public AppInfo(String pack, String name/*, Drawable img*/){
        packageName =pack;
        classname=name;
    //    icon=img;
    }
    String packageName;
    String classname;

    public Drawable getIcon(PackageManager pm) {
        try {
            return pm.getApplicationIcon(packageName);
        } catch (Exception ex)
        {
            //TODO:
            return null;
        }

    }

   // Drawable icon;
    public String getClassname() {
        return classname;
    }
    public String getPackageName() {
        return packageName;
    }




}
