package com.erros.kvasmax.switcher;

import android.app.AppOpsManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingActivity extends AppCompatActivity {

    final static int PERMISSION_REQUEST_CODE = 102;
    final static int USAGE_ACCESS_PERMISSION_REQUEST_CODE = 101;
    final static String VERSION_CODE = "VERSION_CODE";

    private SettingsManager settingsManager;

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

    Set<String> blacklist;
    List<AppInfo> appList;
    BaseAdapter adapterBlacklist;

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
        new AlertDialog.Builder(this)
                .setTitle(R.string.how_to_use)
                .setMessage(R.string.how_to_use_explanation)
                .setPositiveButton(android.R.string.ok, (__, i) -> {
                })
                .create().show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        dragButton.setChecked(false);
        dragAppPanel.setChecked(false);
        saveSettings();

        sendParamWithCheck(SwitcherService.ACTION_APPS_VISIBILITY, 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!blacklistDialog.isShowing() && colorPickerDialog == null)
            sendParamWithCheck(SwitcherService.ACTION_APPS_VISIBILITY, 0);
    }

    public void initialise() {

        settingsManager = SettingsManager.getInstance(this);

        setBottomOffset();
        findViews();
        setAdapters();
        loadSettings();
        setListeners();

        if (settingsManager.isFirstServiceLaunch()) {
            launchServiceWithCheck(true);
        } else {
            checkPermissions();
        }
        if (settingsManager.isFirstAppLaunch()) {
            showTip();
        }

    }

    private void setBottomOffset() {
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        LinearLayout bottom = findViewById(R.id.bottomPadding);
        ViewGroup.LayoutParams params = bottom.getLayoutParams();
        offset = (int) (size.y * 0.7);
        params.height = offset;
        bottom.setLayoutParams(params);
    }

    private void setAdapters() {
        buttonPosition.setAdapter(getAnimAdapter(R.array.button_positions));
        appOrder.setAdapter(getAnimAdapter(R.array.apporder));
        appLayout.setAdapter(getAnimAdapter(R.array.applayout));
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
        blacklistDialog.setOnCancelListener(__ -> {
            settingsManager.saveBlacklist(blacklist);
            sendParamWithCheck(SwitcherService.ACTION_APPS_VISIBILITY, 0);
            sendParamWithCheck(SwitcherService.ACTION_UPDATE_BLACKLIST, 0);
        });

        alignBlacklistCheckboxesToRight();
    }

    private void alignBlacklistCheckboxesToRight() {
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
        transparentColor.setOnClickListener(view -> {
            settingsManager.saveButtonColor(Color.TRANSPARENT);
            sendParamWithCheck(SwitcherService.ACTION_CHANGE_BUTTON_COLOR, Color.TRANSPARENT);
        });
        serviceLauncher.setOnClickListener(__ -> {
            launchServiceWithCheck(serviceLauncher.isChecked());
        });

        chooseColor.setOnClickListener(new View.OnClickListener() {

            int defaultColor = settingsManager.getButtonDefaultColor();
            int oldColor = defaultColor;

            @Override
            public void onClick(View view) {
                oldColor = settingsManager.getButtonColor();
                sendParamWithCheck(SwitcherService.ACTION_APPS_VISIBILITY, 1);
                colorPickerDialog = ColorPickerDialogBuilder
                        .with(SettingActivity.this)
                        .setTitle(getResources().getString(R.string.choose_color))
                        .initialColor(oldColor == Color.TRANSPARENT ? defaultColor : oldColor)
                        .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                        .density(12)
                        .setPositiveButton(getResources().getString(R.string.button_ok), (dialog, selectedColor, allColors) -> {
                            settingsManager.saveButtonColor(selectedColor);
                            sendParamWithCheck(SwitcherService.ACTION_CHANGE_BUTTON_COLOR, selectedColor);
                        })
                        .setNegativeButton(getResources().getString(R.string.button_cancel), (dialog, which) -> sendParamWithCheck(SwitcherService.ACTION_CHANGE_BUTTON_COLOR, oldColor))
                        .setOnColorChangedListener(color -> sendParamWithCheck(SwitcherService.ACTION_CHANGE_BUTTON_COLOR, color))
                        .build();
                colorPickerDialog.setOnDismissListener(dialogInterface -> {
                    sendParamWithCheck(SwitcherService.ACTION_APPS_VISIBILITY, 0);
                    colorPickerDialog = null;
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

        dragButton.setOnCheckedChangeListener((__, isChecked) -> sendParamWithCheck(SwitcherService.ACTION_ALLOW_DRAG_BUTTON, isChecked ? 1 : 0));
        dragAppPanel.setOnCheckedChangeListener((__, isChecked) -> sendParamWithCheck(SwitcherService.ACTION_ALLOW_DRAG_APPS, isChecked ? 1 : 0));
        enableAnimation.setOnCheckedChangeListener((__, isChecked) -> sendParamWithCheck(SwitcherService.ACTION_LAUNCH_ANIMATION_ENABLE, isChecked ? 1 : 0));
        enableVibration.setOnCheckedChangeListener((__, isChecked) -> sendParamWithCheck(SwitcherService.ACTION_LAUNCH_VIBRATION_ENABLE, isChecked ? 1 : 0));
        avoidKeyboard.setOnCheckedChangeListener((__, isChecked) -> sendParamWithCheck(SwitcherService.ACTION_BUTTON_AVOID_KEYBOARD, isChecked ? 1 : 0));
        startOnBoot.setOnCheckedChangeListener((__, isChecked) -> saveSettings());
        blacklistButton.setOnClickListener(__ -> {
            sendParamWithCheck(SwitcherService.ACTION_APPS_VISIBILITY, 1);
            blacklistDialog.show();
        });
        blacklistView.setOnItemClickListener((__, view, i, l) -> {
            AppViewHolder holder = (AppViewHolder) view.getTag();
            String packageName = appList.get(i).getPackageName();
            if (blacklist.contains(packageName)) {
                blacklist.remove(packageName);
                holder.hidden.setChecked(false);
            } else {
                blacklist.add(packageName);
                holder.hidden.setChecked(true);
            }
        });
    }

    private void launchServiceWithCheck(boolean isChecked) {
        if (isChecked) {
            if (checkPermissions()) {
                serviceLauncher.setChecked(true);
                boolean areNotificationsEnabled = false;
                if (Build.VERSION.SDK_INT > 23) {
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(SettingActivity.this);
                    areNotificationsEnabled = notificationManager.areNotificationsEnabled();
                }
                if (areNotificationsEnabled) {
                    showDisableNotificationDialog(this::launchService);
                } else {
                    launchService();
                }
            } else {
                serviceLauncher.setChecked(false);
            }
        } else {
            serviceLauncher.setChecked(false);
            saveSettings();
            sendParamWithCheck(SwitcherService.ACTION_FINISH, 666);
        }
    }

    private void launchService() {
        saveSettings();
        Intent intent = new Intent(getApplicationContext(), SwitcherService.class);
        intent.putExtra(SwitcherService.PARAM, 0);
        startService(intent);
    }

    private void showDisableNotificationDialog(Runnable runnable) {
        new AlertDialog.Builder(SettingActivity.this)
                .setTitle(R.string.title_notification)
                .setMessage(R.string.message_hide_notifications)
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel, (__, i) -> runnable.run())
                .setPositiveButton(R.string.button_hide, (dialog, which) -> {

                    Intent intent = new Intent();
                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                    //for Android 5-7
                    intent.putExtra("app_package", getPackageName());
                    intent.putExtra("app_uid", getApplicationInfo().uid);
                    // for Android O
                    intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());

                    startActivityForResult(intent, PERMISSION_REQUEST_CODE);

                }).create().show();
    }

    private void loadSettings() {

        serviceLauncher.setChecked(isSwitcherServiceRunning());
        thicknessChange.setProgress(settingsManager.getButtonThickness());
        lengthChange.setProgress(settingsManager.getButtonLength());
        appCount.setProgress(settingsManager.getAppCount());
        appIconSize.setProgress(settingsManager.getAppIconSize());
        buttonPosition.setTag(0);
        buttonPosition.setSelection(settingsManager.getButtonPosition(), false);
        appOrder.setTag(0);
        appOrder.setSelection(settingsManager.getAppOrder(), false);
        appLayout.setTag(0);
        appLayout.setSelection(settingsManager.getAppLayout(), false);

        switch (appLayout.getSelectedItemPosition()) {
            case WindowContainer.VERTICAL:
                appAnim.setAdapter(getAnimAdapter(R.array.app_ver_anim));
                break;
            case WindowContainer.HORIZONTAL:
                appAnim.setAdapter(getAnimAdapter(R.array.app_hor_anim));
                break;
        }
        appAnim.setTag(0);
        appAnim.setSelection(settingsManager.getAppBarAnimation(), false);

        enableAnimation.setChecked(settingsManager.isAnimatingSwitching());
        enableVibration.setChecked(settingsManager.isVibratingOnSwitch());
        avoidKeyboard.setChecked(settingsManager.isAvoidingKeyboard());
        startOnBoot.setChecked(settingsManager.isStartingOnBoot());

        loadBlacklist();
    }

    private void loadBlacklist() {
        appList = Utils.getApps(getPackageManager());
        Collections.sort(appList, (first, second) -> first.getName().compareTo(second.getName()));
        if (settingsManager.getBlacklist() == null) {
            settingsManager.saveBlacklist(new HashSet<>(appList.size() / 2));
        }
        blacklist = settingsManager.getBlacklist();
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
        settingsManager.saveButtonThickness(thicknessChange.getProgress());
        settingsManager.saveButtonLength(lengthChange.getProgress());
        settingsManager.saveButtonPosition(buttonPosition.getSelectedItemPosition());
        settingsManager.saveAppCount(appCount.getProgress());
        settingsManager.saveAppIconSize(appIconSize.getProgress());
        settingsManager.saveAppOrder(appOrder.getSelectedItemPosition());
        settingsManager.saveAppLayout(appLayout.getSelectedItemPosition());
        settingsManager.saveAppBarAnimation(appAnim.getSelectedItemPosition());
        settingsManager.saveAnimatingSwitching(enableAnimation.isChecked());
        settingsManager.saveVibratingOnSwitch(enableVibration.isChecked());
        settingsManager.saveAvoidingKeyboard(avoidKeyboard.isChecked());
        settingsManager.saveStartingOnBoot(startOnBoot.isChecked());
        settingsManager.saveBlacklist(blacklist);
        settingsManager.saveDragableFloatingButton(dragButton.isChecked());
        settingsManager.saveDragableAppBar(dragAppPanel.isChecked());
    }

    private void sendParamWithCheck(String action, int param) {
        if (isSwitcherServiceRunning()) {
            Intent intent = new Intent(this, SwitcherService.class);
            intent.setAction(action);
            intent.putExtra(SwitcherService.PARAM, param);
            startService(intent);
        }
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
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.grant, (__, i) -> sendPermissionRequest(permission, newMethod, requestCode))
                .create().show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (settingsManager.isFirstServiceLaunch()) {
            launchServiceWithCheck(true);
        }
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