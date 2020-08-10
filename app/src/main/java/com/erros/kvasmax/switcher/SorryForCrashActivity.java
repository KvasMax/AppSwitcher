package com.erros.kvasmax.switcher;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class SorryForCrashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sorry_for_crash);
        TextView proceedTextView = findViewById(R.id.proceedTextView);

        proceedTextView.setOnClickListener(__ -> {
            SettingsManager.getInstance(this).setAppWasCrashed(false);
            finish();
            startActivity(new Intent(getApplicationContext(), SettingActivity.class));
        });
    }
}
