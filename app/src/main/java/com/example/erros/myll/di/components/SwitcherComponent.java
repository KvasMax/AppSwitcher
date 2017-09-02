package com.example.erros.myll.di.components;

import android.app.Service;

import com.example.erros.myll.FloatingSwitcher;
import com.example.erros.myll.SwitcherContainer;
import com.example.erros.myll.di.modules.SwitcherModule;

import dagger.Component;

/**
 * Created by Max on 20.08.2017.
 */
@Component(modules = SwitcherModule.class)
public interface SwitcherComponent {
    SwitcherContainer getSwitcherContainer();

    void inject(FloatingSwitcher service);
}
