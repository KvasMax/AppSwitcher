package com.example.erros.myll;

import android.graphics.drawable.Drawable;

/**
 * Created by erros on 23.04.16.
 */
public class AppInfo {

    public AppInfo(String pack, String name, Drawable img){
        packagename=pack;
        classname=name;
        icon=img;
    }
    String packagename;
    String classname;

    public Drawable getIcon() {
        return icon;
    }

    Drawable icon;
    public String getClassname() {
        return classname;
    }
    public String getPackagename() {
        return packagename;
    }




}
