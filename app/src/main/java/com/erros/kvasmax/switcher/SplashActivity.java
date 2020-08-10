package com.erros.kvasmax.switcher;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        final Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        if (SettingsManager.getInstance(this).wasAppCrashed()) {
            intent.setClass(this, SorryForCrashActivity.class);
        } else {
            intent.setClass(this, SettingActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
