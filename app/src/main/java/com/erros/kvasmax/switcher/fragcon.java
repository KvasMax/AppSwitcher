package com.erros.kvasmax.switcher;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;


public class fragcon extends Fragment implements AppResultsReceiver.Receiver {

    final static int PERMISSION_REQUEST_CODE = 102;
    final static int USAGE_ACCESS_PERMISSION_REQUEST_CODE = 101;

    private AppResultsReceiver mReceiver;
    private SharedPreferences mSettings;
    private SharedPreferences.Editor mEditor;

    private Spinner SweepDirect;
    private Spinner appOrder;
    private Spinner appLayout;
    private Spinner appAnim;
    private SwitchCompat OnOff;
    private SwitchCompat dragButton;
    private SwitchCompat dragAppPanel;
    private SwitchCompat enableAnimation;
    private boolean onoff;

    private Button chooseColor;
    private Button transparentColor;

    SeekBar widhtChange;
    SeekBar heightChange;

    SeekBar AppCount;
    SeekBar AppIconSize;

    ScrollView settingsContainer;
    int offset;

    public LinearLayout ll;

    int defaultCoor=10;

    //int touchCount = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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


       /* NativeExpressAdView adView =
                v.findViewById(R.id.adViewTop);
        adView.loadAd(new AdRequest.Builder()
                .addTestDevice("2EF0BBC16E5B4F63573C3867F95EC667")
                .build());*/

       // adView = v.findViewById(R.id.adViewBottom);
       // adView.loadAd(new AdRequest.Builder().addTestDevice("33BE2250B43518CCDA7DE426D04EE232").build());

      /*  AdView mAdView = (AdView) v.findViewById(R.id.adViewBottom);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("2EF0BBC16E5B4F63573C3867F95EC667")
                .build();
        mAdView.loadAd(adRequest);*/


        widhtChange=(SeekBar) v.findViewById(R.id.widthpanel);
        heightChange=(SeekBar) v.findViewById(R.id.heightpanel);

        AppCount=(SeekBar) v.findViewById(R.id.AppCount);
        AppIconSize=(SeekBar) v.findViewById(R.id.AppIconSize);
        ll=(LinearLayout)v.findViewById(R.id.properties);
        chooseColor = (Button) v.findViewById(R.id.butChooseColor);
        transparentColor = (Button) v.findViewById(R.id.butTransparentColor);

        WindowManager windowManager = (WindowManager)getActivity().getSystemService(getActivity().WINDOW_SERVICE);
        Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        LinearLayout top = (LinearLayout) v.findViewById(R.id.topPadding);
        LinearLayout bottom = (LinearLayout) v.findViewById(R.id.bottomPadding);
        ViewGroup.LayoutParams params =  top.getLayoutParams();
        offset = size.y / 2;
        params.height = offset;
        top.setLayoutParams(params);
        bottom.setLayoutParams(params);

        settingsContainer = (ScrollView) v.findViewById(R.id.settingsContainer);

        SeekBar.OnSeekBarChangeListener seeklistener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch(seekBar.getId()) {
                    case R.id.widthpanel:
                        sendParam(FloatingSwitcher.ACTION_CHANGE_BUTTON_WiDTH, progress);
                        break;
                    case R.id.heightpanel:
                        sendParam(FloatingSwitcher.ACTION_CHANGE_BUTTON_HEIGHT, progress);
                        break;
                    case R.id.AppCount:
                        sendParam(FloatingSwitcher.ACTION_CHANGE_APPS_COUNT, progress);
                        break;
                    case R.id.AppIconSize:
                        sendParam(FloatingSwitcher.ACTION_CHANGE_APPS_ICON_SIZE, progress);
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
        widhtChange.setOnSeekBarChangeListener(seeklistener);
        heightChange.setOnSeekBarChangeListener(seeklistener);
        AppCount.setOnSeekBarChangeListener(seeklistener);
        AppIconSize.setOnSeekBarChangeListener(seeklistener);

        dragButton = (SwitchCompat)v.findViewById(R.id.dragFloatindButton);
        dragButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                sendParam(FloatingSwitcher.ACTION_ALLOW_DRAG_BUTTON, isChecked ? 1 : 0);
            }
        });

        dragAppPanel = (SwitchCompat)v.findViewById(R.id.dragAppPanel);
        dragAppPanel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                sendParam(FloatingSwitcher.ACTION_ALLOW_DRAG_APPS, isChecked ? 1 : 0);
            }
        });

        enableAnimation = (SwitchCompat)v.findViewById(R.id.enableAnimation);
        enableAnimation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                sendParam(FloatingSwitcher.ACTION_LAUNCH_ANIMATION_ENABLE, isChecked ? 1 : 0);
            }
        });

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
                sendParam(FloatingSwitcher.ACTION_CHANGE_BUTTON_SWEEPDIRECTION, position);
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
                sendParam(FloatingSwitcher.ACTION_CHANGE_APPS_ORDER, position);
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
                sendParam(FloatingSwitcher.ACTION_CHANGE_APPS_LAYOUT, position);
                switch (position)
                {
                    case FloatingWindowContainer.VERTICAL:
                        appAnim.setAdapter(getAnimAdapter(R.array.app_ver_anim));
                        break;
                    case FloatingWindowContainer.HORIZONTAL:
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
                sendParam(FloatingSwitcher.ACTION_CHANGE_APPS_ANIM, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        OnOff = (SwitchCompat)v.findViewById(R.id.startService);
        OnOff.setChecked(onoff=isMyServiceRunning(FloatingSwitcher.class));
        OnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked)
                    if(checkPermissions()) {
                        boolean areNotificationsEnabled = false;
                        if(Build.VERSION.SDK_INT > 23) {
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
                            areNotificationsEnabled = notificationManager.areNotificationsEnabled();
                        }
                            saveSettings();
                            getActivity().startService(new Intent(getActivity().getBaseContext(), FloatingSwitcher.class));
                         /*   if(!isFirstLaunch())
                            {
                                ((MainActivity)getActivity()).showAd();
                            }*/
                        if(areNotificationsEnabled) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle(R.string.title_notification);
                            builder.setMessage(R.string.message_hide_notifications);
                            builder.setCancelable(true);
                            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            builder.setPositiveButton(R.string.button_hide, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    /*Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(uri);
                                    startActivity(intent);*/
                                    Intent intent = new Intent();
                                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");

                                    //for Android 5-7
                                    intent.putExtra("app_package", getActivity().getPackageName());
                                    intent.putExtra("app_uid", getActivity().getApplicationInfo().uid);

                                    // for Android O
                                    intent.putExtra("android.provider.extra.APP_PACKAGE", getActivity().getPackageName());

                                    startActivity(intent);
                                }
                            });

                            builder.create().show();
                        }
                    } else {
                        isChecked = false;
                        buttonView.setChecked(isChecked);
                    }
                else {
                    saveSettings();
                    sendParam(FloatingSwitcher.ACTION_FINISH, 666);
                   // ((MainActivity)getActivity()).showAd();
                }
                onoff = isChecked;
            }
        });

        chooseColor.setOnClickListener(new View.OnClickListener() {
           
            @Override
            public void onClick(View view) {
                ColorPickerDialogBuilder
                        .with(getActivity())
                        .setTitle(getResources().getString(R.string.choose_color))
                        //.initialColor(mSettings.getInt(FloatingSwitcher.APP_PREFERENCES_BUTTON_COLOR, Color.TRANSPARENT))
                        .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                        .density(12)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {
                                //toast("onColorSelected: 0x" + Integer.toHexString(selectedColor));
                                sendParam(FloatingSwitcher.ACTION_CHANGE_BUTTON_COLOR, selectedColor);
                            }
                        })
                        .setPositiveButton(getResources().getString(R.string.button_ok), new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                sendParam(FloatingSwitcher.ACTION_CHANGE_BUTTON_COLOR, selectedColor);
                                mEditor.putInt(FloatingSwitcher.APP_PREFERENCES_BUTTON_COLOR, selectedColor);
                                mEditor.apply();
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .build()
                        .show();
            }
        });
        transparentColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendParam(FloatingSwitcher.ACTION_CHANGE_BUTTON_COLOR, Color.TRANSPARENT);
                mEditor.putInt(FloatingSwitcher.APP_PREFERENCES_BUTTON_COLOR, Color.TRANSPARENT);
                mEditor.apply();
            }
        });

    }


    @Override
    public void onResume() {
        super.onResume();
        mReceiver = new AppResultsReceiver(new Handler());
        mReceiver.setReceiver(this);
        settingsContainer.post(new Runnable() {
            @Override
            public void run() {
                settingsContainer.scrollTo(0, offset);
            }
        });
        loadSettings();
        if(isMyServiceRunning(FloatingSwitcher.class)) {
            //sendParam(FloatingSwitcher.ACTION_INI, xchange.getMax());
            sendParam(FloatingSwitcher.ACTION_APPS_VISIBILITY, 0);
        }
        //((ArrayAdapter<UsageStats>)getListAdapter()).notifyDataSetChanged();

    }

    @Override
    public void onPause() {
        super.onPause();
        mReceiver.setReceiver(null);
        dragButton.setChecked(false);
        dragAppPanel.setChecked(false);
        saveSettings();
        if(isMyServiceRunning(FloatingSwitcher.class)) {
            sendParam(FloatingSwitcher.ACTION_APPS_VISIBILITY, 1);
        }
    }


    @Override
    public void onReceiveResult(int resultCode, Bundle data) {
         if(resultCode==0);
    }

    private boolean isFirstLaunch()
    {
        return mSettings.contains(FloatingSwitcher.APP_PREFERENCES_POINT_COUNT);
    }

    private void loadSettings(){
        if(!isFirstLaunch()) {
            ((MainActivity)getActivity()).showTip();
            return;
        }
        widhtChange.setProgress(mSettings.getInt(FloatingSwitcher.APP_PREFERENCES_BUTTON_WIDTH, defaultCoor));
        heightChange.setProgress(mSettings.getInt(FloatingSwitcher.APP_PREFERENCES_BUTTON_HEIGHT, defaultCoor));
        AppCount.setProgress(mSettings.getInt(FloatingSwitcher.APP_PREFERENCES_APP_COUNT, defaultCoor));
        AppIconSize.setProgress(mSettings.getInt(FloatingSwitcher.APP_PREFERENCES_APP_ICON_SIZE, defaultCoor));
        SweepDirect.setSelection(mSettings.getInt(FloatingSwitcher.APP_PREFERENCES_SWEEP_DIRECTION, 0));
        appOrder.setSelection(mSettings.getInt(FloatingSwitcher.APP_PREFERENCES_APP_ORDER, 0));
        appLayout.setSelection(mSettings.getInt(FloatingSwitcher.APP_PREFERENCES_APP_LAYOUT, 0));

        switch (appLayout.getSelectedItemPosition()) {
            case FloatingWindowContainer.VERTICAL:
                appAnim.setAdapter(getAnimAdapter(R.array.app_ver_anim));
                break;
            case FloatingWindowContainer.HORIZONTAL:
                appAnim.setAdapter(getAnimAdapter(R.array.app_hor_anim));
                break;
        }
        appAnim.setSelection(mSettings.getInt(FloatingSwitcher.APP_PREFERENCES_APP_ANIM, 0));
        enableAnimation.setChecked(mSettings.getBoolean(FloatingSwitcher.APP_PREFERENCES_APP_USE_ANIMATION, true));
        dragButton.setChecked(mSettings.getBoolean(FloatingSwitcher.ACTION_ALLOW_DRAG_BUTTON, false));
        dragAppPanel.setChecked(mSettings.getBoolean(FloatingSwitcher.ACTION_ALLOW_DRAG_APPS, false));
    }
    private void saveSettings(){
        mEditor.putInt(FloatingSwitcher.APP_PREFERENCES_POINT_COUNT, widhtChange.getMax());
        mEditor.putInt(FloatingSwitcher.APP_PREFERENCES_BUTTON_WIDTH, widhtChange.getProgress());
        mEditor.putInt(FloatingSwitcher.APP_PREFERENCES_BUTTON_HEIGHT, heightChange.getProgress());
        mEditor.putInt(FloatingSwitcher.APP_PREFERENCES_SWEEP_DIRECTION, SweepDirect.getSelectedItemPosition());
        mEditor.putInt(FloatingSwitcher.APP_PREFERENCES_APP_COUNT, AppCount.getProgress());
        mEditor.putInt(FloatingSwitcher.APP_PREFERENCES_APP_ICON_SIZE, AppIconSize.getProgress());
        mEditor.putInt(FloatingSwitcher.APP_PREFERENCES_APP_ORDER, appOrder.getSelectedItemPosition());
        mEditor.putInt(FloatingSwitcher.APP_PREFERENCES_APP_LAYOUT, appLayout.getSelectedItemPosition());
        mEditor.putInt(FloatingSwitcher.APP_PREFERENCES_APP_ANIM, appAnim.getSelectedItemPosition());
        mEditor.putBoolean(FloatingSwitcher.APP_PREFERENCES_APP_USE_ANIMATION, enableAnimation.isChecked());
        mEditor.putBoolean(FloatingSwitcher.ACTION_ALLOW_DRAG_BUTTON, dragButton.isChecked());
        mEditor.putBoolean(FloatingSwitcher.ACTION_ALLOW_DRAG_APPS, dragAppPanel.isChecked());

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
    private boolean permissionIsGranted(String permission)
    {
        if(permission.equals(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        {
            boolean granted = false;
            AppOpsManager appOps = (AppOpsManager) getActivity()
                    .getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), getActivity().getPackageName());

            if (mode == AppOpsManager.MODE_DEFAULT) {
                granted = (getActivity().checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
            } else {
                granted = (mode == AppOpsManager.MODE_ALLOWED);
            }
            return granted;
        }
        return ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_GRANTED
                || getActivity().checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }
    private void sendPermissionRequest(String permission, boolean newMethod, int requestCode) {

        if (newMethod){
            requestPermissions(new String[]{permission}, requestCode);
        } else {
            Intent intent = new Intent(permission); // Settings.ACTION_USAGE_ACCESS_SETTINGS
            startActivityForResult(intent, requestCode);
        }

    }
    private boolean checkPermissions(/*Runnable func*/) {
/*

        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));

        if(permissionIsGranted(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
        {
            func.run();
        } else {

        }*/
        Log.e("check", "check");
        boolean granted = true;
        if (!permissionIsGranted(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        {
            requestPermission(Settings.ACTION_USAGE_ACCESS_SETTINGS, false, getActivity().getResources().getString(R.string.usage_access_permission), USAGE_ACCESS_PERMISSION_REQUEST_CODE);
            granted = false;
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getActivity()))
        {
            requestPermission(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, false, getActivity().getResources().getString(R.string.overlay_permission), PERMISSION_REQUEST_CODE);
            granted = false;
        }
        return granted;

    }

    private void requestPermission(String permission, boolean newMethod, String message, int requestCode)
    {
        if(permissionIsGranted(permission))
            return;

        if(newMethod) {
            if (shouldShowRequestPermissionRationale(permission))
            {
                //final String message = "Usage access permission is needed to get a list of running apps";
                showPermissionDialog(permission, newMethod, message, requestCode);

            } else {

                sendPermissionRequest(permission, newMethod, requestCode);
            }
        } else {
            showPermissionDialog(permission, newMethod, message, requestCode);
                // = "Usage access permission is needed to get a list of running apps";
              /*  Snackbar.make(ll, message, Snackbar.LENGTH_LONG)
                        .setAction(getResources().getString(R.string.grant), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                sendPermissionRequest(permission, newMethod, requestCode);
                            }
                        })
                        .show();*/
            }


    }

    private void showPermissionDialog(final String permission, final boolean newMethod, String message, final int requestCode)
    {
        Log.e("permission", permission);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.grant, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sendPermissionRequest(permission, newMethod, requestCode);
            }
        });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
/*        if(requestCode == USAGE_ACCESS_PERMISSION_REQUEST_CODE)
        {
            if(!permissionIsGranted(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            {
                checkPermissions();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getActivity()))
                {
                  // Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getActivity().getPackageName()));
                 //   startActivityForResult(intent, PERMISSION_REQUEST_CODE);

                    checkPermissions();
                }
            }
        }*/
        Log.e("onActivityResult", "onActivityResult");
        checkPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    
    private int getBackgroundColor(View v)
    {
        int color = Color.TRANSPARENT;
        Drawable background = v.getBackground();
        if (background instanceof ColorDrawable)
            color = ((ColorDrawable) background).getColor();
        return color;
    }

    /*private void showOnConditionAd()
    {
        touchCount++;
        if(touchCount % 10 == 0) {
            ((MainActivity) getActivity()).showAd();
        }
    }*/
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
