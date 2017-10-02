package com.erros.kvasmax.switcher.di.components;

import com.erros.kvasmax.switcher.FloatingSwitcher;
import com.erros.kvasmax.switcher.SwitcherContainer;
import com.erros.kvasmax.switcher.di.modules.SwitcherModule;

import dagger.Component;

/**
 * Created by Max on 20.08.2017.
 */
@Component(modules = SwitcherModule.class)
public interface SwitcherComponent {
    SwitcherContainer getSwitcherContainer();

    void inject(FloatingSwitcher service);
}
