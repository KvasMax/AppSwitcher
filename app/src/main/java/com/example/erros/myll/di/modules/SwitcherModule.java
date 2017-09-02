package com.example.erros.myll.di.modules;

import android.app.Application;
import android.content.pm.PackageManager;

import com.example.erros.myll.SwitcherContainer;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Max on 20.08.2017.
 */
@Module
public class SwitcherModule {

    private final Application app;

    public SwitcherModule(Application app)
    {
        this.app = app;
    }

    @Provides
    public SwitcherContainer provideSwitcherContainer(PackageManager pm, String packageName, Integer maxCount)
    {
        return new SwitcherContainer(pm, packageName, maxCount);
    }

    @Provides
    public PackageManager providePackageManager()
    {
      return  app.getPackageManager();
    }

    @Provides
    public String providePackageName()
    {
        return app.getPackageName();
    }

    @Provides
    public Integer provideMaxCount()
    {
        return 5;
    }
}
