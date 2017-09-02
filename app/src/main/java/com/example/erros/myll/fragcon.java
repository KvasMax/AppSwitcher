package com.example.erros.myll;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;

public class fragcon extends Fragment implements AppResultsReceiver.Receiver {

    private AppResultsReceiver mReceiver;
    private SharedPreferences mSettings;
    private SharedPreferences.Editor mEditor;

    private Spinner SweepDirect;
    private Spinner appOrder;
    private Spinner appLayout;
    private Spinner appAnim;
    private Switch OnOff;
    private boolean onoff;

    SeekBar xchange;
    SeekBar ychange;
    SeekBar widhtChange;
    SeekBar heightChange;
    SeekBar xAppSChange;
    SeekBar yAppsChange;
    SeekBar AppCount;

    LinearLayout ll;

    int defaultCoor=10;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getActivity()))
        {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getActivity().getPackageName()));
            startActivityForResult(intent, 1);
        }

        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment, container, false);
        initialise(v);
        return v;

    }
    public void initialise(View v)
    {
        mSettings = getActivity().getSharedPreferences(FloatingSwitcher.APP_PREFERENCES, Context.MODE_PRIVATE);
        mEditor= mSettings.edit();
        xchange=(SeekBar) v.findViewById( R.id.xpanel);
        ychange=(SeekBar) v.findViewById( R.id.ypanel);
        widhtChange=(SeekBar) v.findViewById(R.id.widthpanel);
        heightChange=(SeekBar) v.findViewById(R.id.heightpanel);
        xAppSChange=(SeekBar)v.findViewById(R.id.xAppPanel);
        yAppsChange=(SeekBar)v.findViewById(R.id.yAppPanel);
        AppCount=(SeekBar) v.findViewById(R.id.AppCount);
        ll=(LinearLayout)v.findViewById(R.id.properties);

        SeekBar.OnSeekBarChangeListener seeklistener =new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch(seekBar.getId()) {
                    case R.id.xpanel:
                        sendParam(FloatingSwitcher.ACTION_CHANGE_X,progress);
                        break;
                    case R.id.ypanel:
                        sendParam(FloatingSwitcher.ACTION_CHANGE_Y, progress);
                        break;
                    case R.id.widthpanel:
                        sendParam(FloatingSwitcher.ACTION_CHANGE_WiDTH, progress);
                        break;
                    case R.id.heightpanel:
                        sendParam(FloatingSwitcher.ACTION_CHANGE_HEIGHT, progress);
                        break;
                    case R.id.xAppPanel:
                        sendParam(FloatingSwitcher.ACTION_CHANGE_APPS_X, progress);
                        break;
                    case R.id.yAppPanel:
                        sendParam(FloatingSwitcher.ACTION_CHANGE_APPS_Y, progress);
                        break;
                    case    R.id.AppCount:
                        sendParam(FloatingSwitcher.ACTION_CHANGE_APP_COUNT, progress);
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
        xchange.setOnSeekBarChangeListener(seeklistener);
        ychange.setOnSeekBarChangeListener(seeklistener);
        widhtChange.setOnSeekBarChangeListener(seeklistener);
        heightChange.setOnSeekBarChangeListener(seeklistener);
        xAppSChange.setOnSeekBarChangeListener(seeklistener);
        yAppsChange.setOnSeekBarChangeListener(seeklistener);
        AppCount.setOnSeekBarChangeListener(seeklistener);

        SweepDirect=(Spinner)v.findViewById(R.id.SweepDirection);
        appOrder =(Spinner)v.findViewById(R.id.AppOrder);
        appLayout =(Spinner)v.findViewById(R.id.AppLayout);
        appAnim=(Spinner)v.findViewById(R.id.AppAnim);

        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(getContext(), R.array.sweepdirection, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SweepDirect.setAdapter(adapter);
        SweepDirect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sendParam(FloatingSwitcher.ACTION_CHANGE_SWEEPDIRECTION, position);
            }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

    }
    });

        ArrayAdapter<CharSequence> adap=ArrayAdapter.createFromResource(getContext(), R.array.apporder, android.R.layout.simple_spinner_item);
        adap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        appOrder.setAdapter(adap);
        appOrder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sendParam(FloatingSwitcher.ACTION_CHANGE_APP_ORDER, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<CharSequence> adapt=ArrayAdapter.createFromResource(getContext(), R.array.applayout, android.R.layout.simple_spinner_item);
        adapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        appLayout.setAdapter(adapt);
        appLayout.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sendParam(FloatingSwitcher.ACTION_CHANGE_APP_LAYOUT, position);
                switch (position)
                {
                    case FloatingSwitcher.VERTICAL:
                        appAnim.setAdapter(getAnimAdapter(R.array.app_ver_anim));
                        break;
                    case FloatingSwitcher.HORIZONTAL:
                        appAnim.setAdapter(getAnimAdapter(R.array.app_hor_anim));
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        appAnim.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sendParam(FloatingSwitcher.ACTION_CHANGE_APP_ANIM, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        OnOff=(Switch)v.findViewById(R.id.startService);
        OnOff.setChecked(onoff=isMyServiceRunning(FloatingSwitcher.class));
        OnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    getActivity().startService(new Intent(getActivity(), FloatingSwitcher.class));
                else
                    sendParam(FloatingSwitcher.ACTION_FINISH,666);
                onoff=isChecked;
            }
        });

    }


    @Override
    public void onResume() {
        super.onResume();
        mReceiver = new AppResultsReceiver(new Handler());
        mReceiver.setReceiver(this);
        loadSettings();
        sendParam(FloatingSwitcher.ACTION_INI, xchange.getMax());
        //((ArrayAdapter<UsageStats>)getListAdapter()).notifyDataSetChanged();

    }

    @Override
    public void onPause() {
        super.onPause();
        mReceiver.setReceiver(null);
        saveSettings();
    }


    @Override
    public void onReceiveResult(int resultCode, Bundle data) {
         if(resultCode==0);
    }

    private void loadSettings(){
        if(!mSettings.contains(FloatingSwitcher.APP_PREFERENCES_POINT_COUNT)) return;
        xchange.setProgress(mSettings.getInt(FloatingSwitcher.APP_PREFERENCES_BUTTON_X, defaultCoor));
        ychange.setProgress(mSettings.getInt(FloatingSwitcher.APP_PREFERENCES_BUTTON_Y, defaultCoor));
        widhtChange.setProgress(mSettings.getInt(FloatingSwitcher.APP_PREFERENCES_BUTTON_WIDTH, defaultCoor));
        heightChange.setProgress(mSettings.getInt(FloatingSwitcher.APP_PREFERENCES_BUTTON_HEIGHT, defaultCoor));
        xAppSChange.setProgress(mSettings.getInt(FloatingSwitcher.APP_PREFERENCES_APP_X, defaultCoor));
        yAppsChange.setProgress(mSettings.getInt(FloatingSwitcher.APP_PREFERENCES_APP_Y, defaultCoor));
        AppCount.setProgress(mSettings.getInt(FloatingSwitcher.APP_PREFERENCES_APP_COUNT, defaultCoor));
        SweepDirect.setSelection(mSettings.getInt(FloatingSwitcher.APP_PREFERENCES_SWEEP_DIRECTION, 0));
        appOrder.setSelection(mSettings.getInt(FloatingSwitcher.APP_PREFERENCES_APP_ORDER, 0));
        appLayout.setSelection(mSettings.getInt(FloatingSwitcher.APP_PREFERENCES_APP_LAYOUT, 0));
        ;
        switch (appLayout.getSelectedItemPosition()) {
            case FloatingSwitcher.VERTICAL:
                appAnim.setAdapter(getAnimAdapter(R.array.app_ver_anim));
                break;
            case FloatingSwitcher.HORIZONTAL:
                appAnim.setAdapter(getAnimAdapter(R.array.app_hor_anim));
                break;
        }
        appAnim.setSelection(mSettings.getInt(FloatingSwitcher.APP_PREFERENCES_APP_ANIM, 0));
    }
    private void saveSettings(){
        mEditor.putInt(FloatingSwitcher.APP_PREFERENCES_BUTTON_X, xchange.getProgress());
        mEditor.putInt(FloatingSwitcher.APP_PREFERENCES_BUTTON_Y, ychange.getProgress());
        mEditor.putInt(FloatingSwitcher.APP_PREFERENCES_BUTTON_WIDTH, widhtChange.getProgress());
        mEditor.putInt(FloatingSwitcher.APP_PREFERENCES_BUTTON_HEIGHT, heightChange.getProgress());
        mEditor.putInt(FloatingSwitcher.APP_PREFERENCES_POINT_COUNT, xchange.getMax());
        mEditor.putInt(FloatingSwitcher.APP_PREFERENCES_SWEEP_DIRECTION, SweepDirect.getSelectedItemPosition());
        mEditor.putInt(FloatingSwitcher.APP_PREFERENCES_APP_X, xAppSChange.getProgress());
        mEditor.putInt(FloatingSwitcher.APP_PREFERENCES_APP_Y, yAppsChange.getProgress());
        mEditor.putInt(FloatingSwitcher.APP_PREFERENCES_APP_COUNT, AppCount.getProgress());
        mEditor.putInt(FloatingSwitcher.APP_PREFERENCES_APP_ORDER, appOrder.getSelectedItemPosition());
        mEditor.putInt(FloatingSwitcher.APP_PREFERENCES_APP_LAYOUT, appLayout.getSelectedItemPosition());
        mEditor.putInt(FloatingSwitcher.APP_PREFERENCES_APP_ANIM, appAnim.getSelectedItemPosition());

        mEditor.apply();
    }
    private void sendParam(String action, int param){
        if(onoff) {
            Intent in = new Intent(getActivity(), FloatingSwitcher.class);
            in.setAction(action);
            in.putExtra(FloatingSwitcher.PARAM, param);
            // in.putExtra(FloatingSwitcher.RECEIVER, mReceiver);
            getActivity().startService(in);
        }
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    private ArrayAdapter getAnimAdapter(int resId)
    {
        ArrayAdapter<CharSequence> adapter= ArrayAdapter.createFromResource(getContext(), resId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }
}

/*
ActivityManager am=(ActivityManager)getActivity().getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> proclist =am.getRunningAppProcesses();
ActivityManager.RunningAppProcessInfo ri = getItem(pos);
                tv.setText(ri.processName);
                Package pac= Package.getPackage(ri.processName);
                String nametask=pac.getName();
                Intent in=new Intent(Intent.ACTION_MAIN);
                in.setClassName(ri.processName, nametask);
                in.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
*/
