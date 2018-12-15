package com.erros.kvasmax.switcher;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by erros on 23.04.16.
 */
public class AppInfo {

    private String packageName;
    private String classname;
    private String name;
    private Drawable icon;

    public AppInfo(@NonNull String packageName, @NonNull String classname, @NonNull String name) {
        this.packageName = packageName;
        this.classname = classname;
        this.name = name;
    }

    @Nullable
    public Drawable getIcon(PackageManager packageManager) {
        try {
            if (icon == null) {
                icon = packageManager.getApplicationIcon(packageName);
            }
            return icon;
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
