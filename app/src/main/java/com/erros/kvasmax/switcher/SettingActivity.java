package com.erros.kvasmax.switcher;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingActivity extends AppCompatActivity implements AppResultsReceiver.Receiver {

    final static int PERMISSION_REQUEST_CODE = 102;
    final static int USAGE_ACCESS_PERMISSION_REQUEST_CODE = 101;
    final static String VERSION_CODE = "VERSION_CODE";

    private AppResultsReceiver mReceiver;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    private Spinner buttonPosition;
    private Spinner appOrder;
    private Spinner appLayout;
    private Spinner appAnim;
    private SwitchCompat serviceLauncher;
    private SwitchCompat dragButton;
    private SwitchCompat dragAppPanel;
    private SwitchCompat enableAnimation;
    private SwitchCompat enableVibration;
    private SwitchCompat avoidKeyboard;
    private SwitchCompat startOnBoot;

    private Button chooseColor;
    private Button transparentColor;

    private SeekBar thicknessChange;
    private SeekBar lengthChange;

    private SeekBar appCount;
    private SeekBar appIconSize;

    private ScrollView settingsScrollView;

    private TextView blacklistButton;
    int offset;

    private LinearLayout settingContainer;

    private Dialog blacklistDialog;
    private ListView blacklistView;

    private AlertDialog colorPickerDialog;

    int defaultCoor = 10;

    Set<String> blacklist;
    List<AppInfo> appList;
    BaseAdapter adapterBlacklist;

    // private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initialise();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.switchForLaunch);
        item.setActionView(serviceLauncher);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tip_how_to_use:
                showTip();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showTip() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.how_to_use);
        builder.setMessage(R.string.how_to_use_explanation);
        builder.setCancelable(false);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
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
    protected void onPause() {
        super.onPause();
        //  showAd();

        mReceiver.setReceiver(null);
        dragButton.setChecked(false);
        dragAppPanel.setChecked(false);
        saveSettings();

        sendParamWithCheck(SwitcherService.ACTION_APPS_VISIBILITY, 1);
    }


    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mReceiver = new AppResultsReceiver(new Handler());
        mReceiver.setReceiver(this);
       /* settingsScrollView.post(new Runnable() {
            @Override
            public void run() {
                settingsScrollView.scrollTo(0, offset);
            }
        });*/
        if (!blacklistDialog.isShowing() && colorPickerDialog == null)
            sendParamWithCheck(SwitcherService.ACTION_APPS_VISIBILITY, 0);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle data) {
        if (resultCode == 0) ;
    }

    public void initialise() {
        settings = getSharedPreferences(SwitcherApplication.APP_PREFERENCES, Context.MODE_PRIVATE);
        editor = settings.edit();

       /* if(settings.contains(VERSION_CODE))
        {
            int version = settings.getInt(VERSION_CODE, BuildConfig.VERSION_CODE - 1);
            if(version != BuildConfig.VERSION_CODE)
            {
                editor.clear();
                editor.apply();
            }
        } else {
            editor.putInt(VERSION_CODE, BuildConfig.VERSION_CODE);
            editor.apply();
        }*/


        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        LinearLayout top = findViewById(R.id.topPadding);
        LinearLayout bottom = findViewById(R.id.bottomPadding);
        ViewGroup.LayoutParams params = bottom.getLayoutParams();
        offset = (int) (size.y * 0.7);
        params.height = offset;
        //top.setLayoutParams(params);
        bottom.setLayoutParams(params);

        findViews();

        buttonPosition.setAdapter(getAnimAdapter(R.array.button_positions));
        appOrder.setAdapter(getAnimAdapter(R.array.apporder));
        appLayout.setAdapter(getAnimAdapter(R.array.applayout));
        loadSettings();
        setListeners();

        if (isFirstServiceLaunch()) {
            serviceLauncher.setChecked(true);
        } else {
            checkPermissions();
        }
        if (isFirstAppLaunch()) {
            showTip();
        }

    }

    private void findViews() {
        settingsScrollView = findViewById(R.id.settingsContainer);
        thicknessChange = findViewById(R.id.buttonThickness);
        lengthChange = findViewById(R.id.buttonLength);
        appCount = findViewById(R.id.AppCount);
        appIconSize = findViewById(R.id.AppIconSize);
        settingContainer = findViewById(R.id.properties);
        chooseColor = findViewById(R.id.butChooseColor);
        transparentColor = findViewById(R.id.butTransparentColor);
        dragButton = findViewById(R.id.dragFloatindButton);
        dragAppPanel = findViewById(R.id.dragAppPanel);
        enableAnimation = findViewById(R.id.enableAnimation);
        enableVibration = findViewById(R.id.enableVibration);
        avoidKeyboard = findViewById(R.id.avoidKeyboard);
        buttonPosition = findViewById(R.id.buttonPosition);
        appOrder = findViewById(R.id.AppOrder);
        appLayout = findViewById(R.id.AppLayout);
        appAnim = findViewById(R.id.AppAnim);
        startOnBoot = findViewById(R.id.startOnBoot);
        blacklistButton = findViewById(R.id.black_list);
        serviceLauncher = (SwitchCompat) getLayoutInflater().inflate(R.layout.launch_layout, null);
        FrameLayout blacklistContainerView = (FrameLayout) getLayoutInflater().inflate(R.layout.app_list, null);
        blacklistView = blacklistContainerView.findViewById(R.id.app_list);

        blacklistDialog = new Dialog(SettingActivity.this);
        blacklistDialog.setContentView(blacklistContainerView);
        blacklistDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                editor.putStringSet(SwitcherApplication.APP_PREFERENCES_COMMON_BLACKLIST, blacklist);
                editor.apply();
                sendParamWithCheck(SwitcherService.ACTION_APPS_VISIBILITY, 0);
                sendParamWithCheck(SwitcherService.ACTION_UPDATE_BLACKLIST, 0);
            }
        });
        //FIXME WTF?
        Window window = blacklistDialog.getWindow();
        WindowManager.LayoutParams wlp;
        wlp = window.getAttributes();
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(wlp);
    }

    private void setListeners() {
        buttonPosition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (buttonPosition.getTag() == null) {
                    sendParamWithCheck(SwitcherService.ACTION_CHANGE_BUTTON_POSITION, position);
                } else {
                    buttonPosition.setTag(null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        appAnim.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (appAnim.getTag() == null) {
                    sendParamWithCheck(SwitcherService.ACTION_CHANGE_APPS_ANIM, position);
                } else {
                    appAnim.setTag(null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        appLayout.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (appLayout.getTag() == null) {
                    sendParamWithCheck(SwitcherService.ACTION_CHANGE_APPS_LAYOUT, position);
                    switch (position) {
                        case WindowContainer.VERTICAL:
                            appAnim.setAdapter(getAnimAdapter(R.array.app_ver_anim));
                            break;
                        case WindowContainer.HORIZONTAL:
                            appAnim.setAdapter(getAnimAdapter(R.array.app_hor_anim));
                            break;
                    }
                } else {
                    appLayout.setTag(null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        appOrder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (appOrder.getTag() == null) {
                    sendParamWithCheck(SwitcherService.ACTION_CHANGE_APPS_ORDER, position);
                } else {
                    appOrder.setTag(null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        transparentColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putInt(SwitcherApplication.APP_PREFERENCES_BUTTON_COLOR, Color.TRANSPARENT);
                editor.apply();
                sendParamWithCheck(SwitcherService.ACTION_CHANGE_BUTTON_COLOR, Color.TRANSPARENT);
            }
        });
        serviceLauncher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    if (checkPermissions()) {
                        boolean areNotificationsEnabled = false;
                        if (Build.VERSION.SDK_INT > 23) {
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(SettingActivity.this);
                            areNotificationsEnabled = notificationManager.areNotificationsEnabled();
                        }


                        if (areNotificationsEnabled) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                            builder.setTitle(R.string.title_notification);
                            builder.setMessage(R.string.message_hide_notifications);
                            builder.setCancelable(false);
                            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    launchService();
                                }
                            });
                            builder.setPositiveButton(R.string.button_hide, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Intent intent = new Intent();
                                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");

                                    //for Android 5-7
                                    intent.putExtra("app_package", getPackageName());
                                    intent.putExtra("app_uid", getApplicationInfo().uid);

                                    // for Android O
                                    intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());

                                    startActivityForResult(intent, PERMISSION_REQUEST_CODE);
                                    serviceLauncher.setChecked(false);

                                }
                            });

                            builder.create().show();
                        } else {
                            launchService();
                        }
                    } else {
                        isChecked = false;
                        buttonView.setChecked(isChecked);
                    }
                } else {
                    saveSettings();
                    sendParamWithCheck(SwitcherService.ACTION_FINISH, 666);
                }
            }
        });

        chooseColor.setOnClickListener(new View.OnClickListener() {

            int defaultColor = ContextCompat.getColor(SettingActivity.this, R.color.defaultColor);
            int oldColor = defaultColor;

            @Override
            public void onClick(View view) {
                oldColor = settings.getInt(SwitcherApplication.APP_PREFERENCES_BUTTON_COLOR, defaultColor);
                sendParamWithCheck(SwitcherService.ACTION_APPS_VISIBILITY, 1);
                colorPickerDialog = ColorPickerDialogBuilder
                        .with(SettingActivity.this)
                        .setTitle(getResources().getString(R.string.choose_color))
                        .initialColor(oldColor == Color.TRANSPARENT ? defaultColor : oldColor)
                        .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                        .density(12)
                        .setPositiveButton(getResources().getString(R.string.button_ok), new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                editor.putInt(SwitcherApplication.APP_PREFERENCES_BUTTON_COLOR, selectedColor);
                                editor.apply();
                                sendParamWithCheck(SwitcherService.ACTION_CHANGE_BUTTON_COLOR, selectedColor);
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendParamWithCheck(SwitcherService.ACTION_CHANGE_BUTTON_COLOR, oldColor);
                            }
                        })
                        .setOnColorChangedListener(new OnColorChangedListener() {
                            @Override
                            public void onColorChanged(int color) {
                                sendParamWithCheck(SwitcherService.ACTION_CHANGE_BUTTON_COLOR, color);
                            }
                        })
                        .build();
                colorPickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        sendParamWithCheck(SwitcherService.ACTION_APPS_VISIBILITY, 0);
                        colorPickerDialog = null;
                    }
                });
                colorPickerDialog.show();
            }
        });
        SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (seekBar.getId()) {
                    case R.id.buttonThickness:
                        sendParamWithCheck(SwitcherService.ACTION_CHANGE_BUTTON_THICKNESS, progress);
                        break;
                    case R.id.buttonLength:
                        sendParamWithCheck(SwitcherService.ACTION_CHANGE_BUTTON_LENGTH, progress);
                        break;
                    case R.id.AppCount:
                        sendParamWithCheck(SwitcherService.ACTION_CHANGE_APPS_COUNT, progress);
                        break;
                    case R.id.AppIconSize:
                        sendParamWithCheck(SwitcherService.ACTION_CHANGE_APPS_ICON_SIZE, progress);
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

        thicknessChange.setOnSeekBarChangeListener(seekListener);
        lengthChange.setOnSeekBarChangeListener(seekListener);
        appCount.setOnSeekBarChangeListener(seekListener);
        appIconSize.setOnSeekBarChangeListener(seekListener);

        dragButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sendParamWithCheck(SwitcherService.ACTION_ALLOW_DRAG_BUTTON, isChecked ? 1 : 0);
            }
        });
        dragAppPanel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sendParamWithCheck(SwitcherService.ACTION_ALLOW_DRAG_APPS, isChecked ? 1 : 0);
            }
        });
        enableAnimation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sendParamWithCheck(SwitcherService.ACTION_LAUNCH_ANIMATION_ENABLE, isChecked ? 1 : 0);
            }
        });
        enableVibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sendParamWithCheck(SwitcherService.ACTION_LAUNCH_VIBRATION_ENABLE, isChecked ? 1 : 0);
            }
        });
        avoidKeyboard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sendParamWithCheck(SwitcherService.ACTION_BUTTON_AVOID_KEYBOARD, isChecked ? 1 : 0);
            }
        });
        startOnBoot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveSettings();
            }
        });
        blacklistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendParamWithCheck(SwitcherService.ACTION_APPS_VISIBILITY, 1);
                blacklistDialog.show();
            }
        });
        blacklistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AppViewHolder holder = (AppViewHolder) view.getTag();
                String packageName = appList.get(i).getPackageName();
                if (blacklist.contains(packageName)) {
                    blacklist.remove(packageName);
                    holder.hidden.setChecked(false);
                } else {
                    blacklist.add(packageName);
                    holder.hidden.setChecked(true);
                }
            }
        });
    }

    private void launchService() {
        saveSettings();
        Intent intent =new Intent(getApplicationContext(), SwitcherService.class);
        intent.putExtra(SwitcherService.PARAM, 0);
        startService(intent);
    }

    private boolean isFirstAppLaunch() {
        return !settings.contains(SwitcherApplication.APP_PREFERENCES_APP_COUNT);
    }

    private boolean isFirstServiceLaunch() {
        return !settings.contains(SwitcherApplication.APP_PREFERENCES_SERVICE_FIRST_LAUNCH);
    }

    private void loadSettings() {

        serviceLauncher.setChecked(isSwitcherServiceRunning());
        thicknessChange.setProgress(settings.getInt(SwitcherApplication.APP_PREFERENCES_BUTTON_THICKNESS, 40));
        lengthChange.setProgress(settings.getInt(SwitcherApplication.APP_PREFERENCES_BUTTON_LENGTH, 80));
        appCount.setProgress(settings.getInt(SwitcherApplication.APP_PREFERENCES_APP_COUNT, 4));
        appIconSize.setProgress(settings.getInt(SwitcherApplication.APP_PREFERENCES_APP_ICON_SIZE, 35));
        buttonPosition.setTag(0);
        buttonPosition.setSelection(settings.getInt(SwitcherApplication.APP_PREFERENCES_BUTTON_POSITION, 1), false);
        appOrder.setTag(0);
        appOrder.setSelection(settings.getInt(SwitcherApplication.APP_PREFERENCES_APP_ORDER, 1), false);
        appLayout.setTag(0);
        appLayout.setSelection(settings.getInt(SwitcherApplication.APP_PREFERENCES_APP_LAYOUT, 0), false);


        switch (appLayout.getSelectedItemPosition()) {
            case WindowContainer.VERTICAL:
                appAnim.setAdapter(getAnimAdapter(R.array.app_ver_anim));
                break;
            case WindowContainer.HORIZONTAL:
                appAnim.setAdapter(getAnimAdapter(R.array.app_hor_anim));
                break;
        }
        appAnim.setTag(0);
        appAnim.setSelection(settings.getInt(SwitcherApplication.APP_PREFERENCES_APP_ANIM, 1), false);

        enableAnimation.setChecked(settings.getBoolean(SwitcherApplication.APP_PREFERENCES_APP_USE_ANIMATION, true));
        enableVibration.setChecked(settings.getBoolean(SwitcherApplication.APP_PREFERENCES_APP_USE_VIBRATION, false));
        avoidKeyboard.setChecked(settings.getBoolean(SwitcherApplication.APP_PREFERENCES_BUTTON_AVOID_KEYBOARD, false));
        dragButton.setChecked(settings.getBoolean(SwitcherService.ACTION_ALLOW_DRAG_BUTTON, false));
        dragAppPanel.setChecked(settings.getBoolean(SwitcherService.ACTION_ALLOW_DRAG_APPS, false));
        startOnBoot.setChecked(settings.contains(SwitcherApplication.APP_PREFERENCES_COMMON_START_ON_BOOT));


        appList = Utils.getApps(getPackageManager());
        Collections.sort(appList, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo first, AppInfo second) {
                return first.getName().compareTo(second.getName());
            }
        });
        if (!settings.contains(SwitcherApplication.APP_PREFERENCES_COMMON_BLACKLIST)) {
            editor.putStringSet(SwitcherApplication.APP_PREFERENCES_COMMON_BLACKLIST, new HashSet<String>(appList.size() / 2));
            editor.apply();
        }
        blacklist = settings.getStringSet(SwitcherApplication.APP_PREFERENCES_COMMON_BLACKLIST, new HashSet<String>());
        adapterBlacklist = new BaseAdapter() {
            @Override
            public int getCount() {
                return appList.size();
            }

            @Override
            public Object getItem(int i) {
                return null;
            }

            @Override
            public long getItemId(int i) {
                return 0;
            }

            @Override
            public View getView(int position, View view, ViewGroup viewGroup) {
                AppViewHolder holder;
                if (view == null) {
                    view = getLayoutInflater().inflate(R.layout.app_list_row, viewGroup, false);
                    holder = new AppViewHolder(view);
                    view.setTag(holder);
                } else {
                    holder = (AppViewHolder) view.getTag();
                }
                AppInfo app = appList.get(position);
                holder.icon.setImageDrawable(app.getIcon(getPackageManager()));
                holder.name.setText(app.getName());
                holder.hidden.setChecked(blacklist.contains(app.getPackageName()));
                return view;
            }
        };
        blacklistView.setAdapter(adapterBlacklist);
    }

    private void saveSettings() {
        editor.putInt(SwitcherApplication.APP_PREFERENCES_BUTTON_THICKNESS, thicknessChange.getProgress());
        editor.putInt(SwitcherApplication.APP_PREFERENCES_BUTTON_LENGTH, lengthChange.getProgress());
        editor.putInt(SwitcherApplication.APP_PREFERENCES_BUTTON_POSITION, buttonPosition.getSelectedItemPosition());
        editor.putInt(SwitcherApplication.APP_PREFERENCES_APP_COUNT, appCount.getProgress());
        editor.putInt(SwitcherApplication.APP_PREFERENCES_APP_ICON_SIZE, appIconSize.getProgress());
        editor.putInt(SwitcherApplication.APP_PREFERENCES_APP_ORDER, appOrder.getSelectedItemPosition());
        editor.putInt(SwitcherApplication.APP_PREFERENCES_APP_LAYOUT, appLayout.getSelectedItemPosition());
        editor.putInt(SwitcherApplication.APP_PREFERENCES_APP_ANIM, appAnim.getSelectedItemPosition());
        editor.putBoolean(SwitcherApplication.APP_PREFERENCES_APP_USE_ANIMATION, enableAnimation.isChecked());
        editor.putBoolean(SwitcherApplication.APP_PREFERENCES_APP_USE_VIBRATION, enableVibration.isChecked());
        editor.putBoolean(SwitcherApplication.APP_PREFERENCES_BUTTON_AVOID_KEYBOARD, avoidKeyboard.isChecked());
        if (startOnBoot.isChecked())
            editor.putInt(SwitcherApplication.APP_PREFERENCES_COMMON_START_ON_BOOT, 0);
        else editor.remove(SwitcherApplication.APP_PREFERENCES_COMMON_START_ON_BOOT);
        editor.putBoolean(SwitcherService.ACTION_ALLOW_DRAG_BUTTON, dragButton.isChecked());
        editor.putBoolean(SwitcherService.ACTION_ALLOW_DRAG_APPS, dragAppPanel.isChecked());
        editor.putStringSet(SwitcherApplication.APP_PREFERENCES_COMMON_BLACKLIST, blacklist);

        editor.apply();
    }

    private void sendParamWithCheck(String action, int param) {
        if (SwitcherApplication.serviceIsRunning.get()) {
            sendParam(action, param);
        }
    }

    private void sendParam(String action, int param) {
        Intent in = new Intent(this, SwitcherService.class);
        in.setAction(action);
        in.putExtra(SwitcherService.PARAM, param);
        startService(in);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isSwitcherServiceRunning() {
        return SwitcherApplication.serviceIsRunning.get();
    }

    private ArrayAdapter getAnimAdapter(int resId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, resId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private boolean permissionIsGranted(String permission) {
        if (permission.equals(Settings.ACTION_USAGE_ACCESS_SETTINGS)) {
            boolean granted = false;
            AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), getPackageName());

            if (mode == AppOpsManager.MODE_DEFAULT) {
                granted = (checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
            } else {
                granted = (mode == AppOpsManager.MODE_ALLOWED);
            }
            return granted;
        }
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
                || checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void sendPermissionRequest(String permission, boolean newMethod, int requestCode) {

        if (newMethod) {
            requestPermissions(new String[]{permission}, requestCode);
        } else {
            Intent intent = new Intent(permission);
            startActivityForResult(intent, requestCode);
        }

    }

    private boolean checkPermissions() {

        boolean granted = true;
        if (!permissionIsGranted(Settings.ACTION_USAGE_ACCESS_SETTINGS)) {
            requestPermission(Settings.ACTION_USAGE_ACCESS_SETTINGS, false, getResources().getString(R.string.usage_access_permission), USAGE_ACCESS_PERMISSION_REQUEST_CODE);
            granted = false;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            requestPermission(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, false, getResources().getString(R.string.overlay_permission), PERMISSION_REQUEST_CODE);
            granted = false;
        }
        return granted;

    }

    private void requestPermission(String permission, boolean newMethod, String message, int requestCode) {
        if (permissionIsGranted(permission))
            return;

        if (newMethod) {
            if (shouldShowRequestPermissionRationale(permission)) {
                showPermissionDialog(permission, newMethod, message, requestCode);

            } else {

                sendPermissionRequest(permission, newMethod, requestCode);
            }
        } else {
            showPermissionDialog(permission, newMethod, message, requestCode);
        }


    }

    private void showPermissionDialog(final String permission, final boolean newMethod, String message, final int requestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.grant, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sendPermissionRequest(permission, newMethod, requestCode);
            }
        });

        builder.create().show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (checkPermissions() && isFirstServiceLaunch()) {
            serviceLauncher.setChecked(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private class AppViewHolder {

        private final ImageView icon;
        private final TextView name;
        private final AppCompatCheckBox hidden;

        public AppViewHolder(View view) {
            icon = view.findViewById(R.id.app_icon);
            name = view.findViewById(R.id.app_name);
            hidden = view.findViewById(R.id.hidden);
        }
    }

}



/*    MobileAds.initialize(this, getResources().getString(R.string.unit_id));

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.ad_fullscreen));
        mInterstitialAd.loadAd(new AdRequest.Builder()
                .addTestDevice("2EF0BBC16E5B4F63573C3867F95EC667")
                .build());
        mInterstitialAd.setAdListener(new AdListener() {

            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder()
                        .addTestDevice("2EF0BBC16E5B4F63573C3867F95EC667")
                        .build());
            }
        });*/
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