package com.erros.kvasmax.switcher;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

/**
 * Created by erros on 23.04.16.
 */
public class AppInfo {

    private String packageName;
    private String classname;
    private String name;

    public AppInfo(String packageName, String classname, String name) {
        this.packageName = packageName;
        this.classname = classname;
        this.name = name;
    }

    public Drawable getIcon(PackageManager pm) {
        try {
            return pm.getApplicationIcon(packageName);
        } catch (Exception ex) {
            return null;
        }

    }
    public String getClassname() {
        return classname;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getName() {
        return name;
    }
}
