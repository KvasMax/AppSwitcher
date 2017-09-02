package com.example.erros.myll;

/**
 * Created by erros on 25.04.16.
 */
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.erros.myll.di.components.DaggerSwitcherComponent;
import com.example.erros.myll.di.modules.SwitcherModule;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class FloatingSwitcher extends Service {
    static int maxcount=6;

    private WindowManager windowManager;
    private SharedPreferences mSettings;
    UsageStatsManager mUsageStatsManager;
    ActivityManager am;

    ImageView touchpanel;
    List<UsageStats> stats;
    ArrayList<ImageView> apps;
    LinearLayout appsContainer;

    @Inject
    SwitcherContainer swi;

    LayoutParams butParams;
    LayoutParams appsParams;

    Animation firstAnim;
    Animation secondAnim;

    Point size;
    int butWidth=70;
    int butHeight=10;
    int butX=0;
    int butY=0;
    int pointCount;
    int defaultInc=10;
    int incX;
    int incY;
    int incWidth=defaultInc;
    int incHeight=defaultInc;
    int appX=0;
    int appY=0;
    boolean apporder=true;
    int applayout=0;
    int appanim=0;
    //Constantsinc
    public  static final String ACTION_INI ="change your behaviour, fucking asshole";
    public  static final String ACTION_FINISH ="finish yourself, dickhead";

    public  static final String ACTION_CHANGE_X ="change x, fucking asshole";
    public  static final String ACTION_CHANGE_Y ="change y, fucking asshole";
    public  static final String ACTION_CHANGE_WiDTH ="change width, fucking asshole";
    public  static final String ACTION_CHANGE_HEIGHT ="change height, fucking asshole";

    public  static final String ACTION_CHANGE_SWEEPDIRECTION="sweep sweep";
    public  static final String ACTION_CHANGE_APPS_X="appps xx xx";
    public  static final String ACTION_CHANGE_APPS_Y="apppsyyy y y yyy";
    public  static final String ACTION_CHANGE_APP_COUNT="apppsyyy y y yyy count";
    public  static final String ACTION_CHANGE_APP_ORDER="ACTION_CHANGE_APP_ORDER";
    public  static final String ACTION_CHANGE_APP_LAYOUT="ACTION_CHANGE_APP_LAYOUT";
    public  static final String ACTION_CHANGE_APP_ANIM="ACTION_CHANGE_APP_ANIM";

    public  static final String PARAM="this is my new property, fucking switcher";
    public  static final String RECEIVER = "paramparampam";

    public static final String APP_PREFERENCES="switchersettings";
    public static final String APP_PREFERENCES_POINT_COUNT="switchersettings";
    public static final String APP_PREFERENCES_BUTTON_X="buttonX";
    public static final String APP_PREFERENCES_BUTTON_Y="buttonY";
    public static final String APP_PREFERENCES_BUTTON_WIDTH="buttonWidth";
    public static final String APP_PREFERENCES_BUTTON_HEIGHT="buttonHeight";
    public static final String APP_PREFERENCES_SWEEP_DIRECTION="sweepppdirectionsweep";

    public static final String APP_PREFERENCES_APP_COUNT="APP_PREFERENCES_APP_COUNT";
    public static final String APP_PREFERENCES_APP_ORDER="APP_PREFERENCES_APP_ORDER";
    public static final String APP_PREFERENCES_APP_LAYOUT="APP_PREFERENCES_APP_LAYOUT";
    public static final String APP_PREFERENCES_APP_ANIM="APP_PREFERENCES_APP_ANIM";
    public static final String APP_PREFERENCES_APP_X="APP_PREFERENCES_APP_X";
    public static final String APP_PREFERENCES_APP_Y="APP_PREFERENCES_APP_Y";


    //Sweep Constants
    public static final int BottomTop=0;
    public static final int RightLeft=1;
    public static final int TopBottom=2;
    public static final int LeftRight=3;
    int sweepDirection=BottomTop;

    public static final int VERTICAL=0;
    public static final int HORIZONTAL=1;

    int picSize=100;

    public void onCreate() {
        super.onCreate();
        mUsageStatsManager = (UsageStatsManager) getApplication().getSystemService(Activity.USAGE_STATS_SERVICE);
        am=(ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        //PackageManager pm=getApplication().getPackageManager();
        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
       // swi=new SwitcherContainer(pm,getPackageName(), maxcount);
        DaggerSwitcherComponent.builder().switcherModule(new SwitcherModule(getApplication())).build().inject(this);
        size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        mSettings = getApplication().getSharedPreferences(FloatingSwitcher.APP_PREFERENCES, Context.MODE_PRIVATE);
        initialise();
        update();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null && intent.getAction()!=null)
        {
            if (intent.getAction().equals(ACTION_INI)) {
                pointCount =  intent.getIntExtra(PARAM, defaultInc);
                getIncs();
            }
            else if (intent.getAction().equals(ACTION_FINISH)) {
                windowManager.removeView(appsContainer);
                windowManager.removeView(touchpanel);
                stopSelf();
            } else if (intent.getAction().equals(ACTION_CHANGE_X)) {
                PropertyChanger(butParams, intent.getIntExtra(PARAM, defaultInc), ACTION_CHANGE_X);
            }
            else if (intent.getAction().equals(ACTION_CHANGE_Y)) {
                PropertyChanger(butParams,intent.getIntExtra(PARAM, defaultInc),ACTION_CHANGE_Y);
            }
            else if( intent.getAction().contains(ACTION_CHANGE_WiDTH))
            {
                PropertyChanger(butParams, intent.getIntExtra(PARAM, defaultInc), ACTION_CHANGE_WiDTH);
            }else if( intent.getAction().contains(ACTION_CHANGE_HEIGHT))
            {
                PropertyChanger(butParams, intent.getIntExtra(PARAM, defaultInc), ACTION_CHANGE_HEIGHT);
            }
            else if(intent.getAction().equals(ACTION_CHANGE_SWEEPDIRECTION))
            {
                sweepDirection=intent.getIntExtra(PARAM, BottomTop);
            }
            else if(intent.getAction().equals(ACTION_CHANGE_APPS_X))
            {
                PropertyChanger(appsParams,intent.getIntExtra(PARAM, defaultInc),ACTION_CHANGE_X);
            }
            else if(intent.getAction().equals(ACTION_CHANGE_APPS_Y))
            {
                PropertyChanger(appsParams,intent.getIntExtra(PARAM, defaultInc),ACTION_CHANGE_Y);
            }else if(intent.getAction().equals(ACTION_CHANGE_APP_COUNT))
            {
                maxcount=intent.getIntExtra(PARAM, defaultInc)+1;
                initialiseAppPanel(true);
            }else if(intent.getAction().equals(ACTION_CHANGE_APP_ORDER))
            {
                apporder =intent.getIntExtra(PARAM, 0)==0;

            }else if(intent.getAction().equals(ACTION_CHANGE_APP_LAYOUT))
            {
                applayout =intent.getIntExtra(PARAM, 0);
               /* windowManager.removeView(appsContainer);
                appsContainer.removeAllViews();
*/
                switch (applayout)
                {
                    case VERTICAL:
                        appsContainer.setOrientation(LinearLayout.VERTICAL);
                        appsParams.width=picSize;
                        appsParams.height=picSize*maxcount;
                        PropertyChanger(appsParams,0,ACTION_CHANGE_Y);
                        break;
                    case HORIZONTAL:
                        appsContainer.setOrientation(LinearLayout.HORIZONTAL);
                        appsParams.width=picSize*maxcount;
                        appsParams.height=picSize;
                        PropertyChanger(appsParams,0,ACTION_CHANGE_X);
                        break;
                }
               /* for(ImageView im: apps) {
                    appsContainer.addView(im);
                }*/
               // windowManager.removeView(appsContainer);
               // windowManager.addView(appsContainer, appsParams);
            }
            windowManager.updateViewLayout(touchpanel, butParams);
            windowManager.updateViewLayout(appsContainer, appsParams);
        }
        return START_STICKY;
    }
    private void update(){
        long currentTime = System.currentTimeMillis();
        // get usage stats for the last 10 seconds
        int minutes=5;
        stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 1000 * 60 * minutes, currentTime);
        if(stats.size()<maxcount){
            minutes=1440;
            stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 1000 * 60 * minutes, currentTime);
        }
        swi.update(stats);
        int i= apporder ? apps.size()-1 : 0;
        if(swi.switchapps.size()!=0) {
            for (ImageView im : apps) {
                if( ( apporder || !apporder) && swi.switchapps.size()>i) {
                    AppInfo app = swi.switchapps.get(i);
                    im.setImageDrawable(app.getIcon());
                }
                else im.setImageDrawable(null);
                if(apporder) --i;
                else ++i;
            }
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.lackOfApps, Toast.LENGTH_SHORT);
        }
      /*  for(int i=0;i<stats.size();i++) {
            if (stats.get(i).getPackageName().contains(currpackage) || stats.get(i).getPackageName().contains("launcher") || stats.get(i).getPackageName().contains("systemui")) {
                stats.remove(i);
            }
        }*
        proclist =am.getRunningAppProcesses();*/
    }
    private void initialise(){




        touchpanel = new ImageView(this);
        touchpanel.setImageResource(R.drawable.touchpanel);
        touchpanel.setOnTouchListener(new View.OnTouchListener() {
            WindowManager.LayoutParams paramsT = butParams;
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private long touchStartTime = 0;
            boolean visible = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //remove face bubble on long press
                   /* if(System.currentTimeMillis()-touchStartTime>ViewConfiguration.getLongPressTimeout() && initialTouchX== event.getX()){
                        windowManager.removeView(touchpanel);
                        stopSelf();
                        return false;
                    }*/
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                            /*touchStartTime = System.currentTimeMillis();
                            initialX = butParams.x;
                            initialY = butParams.y;*/
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        int width = appsContainer.getWidth();
                        int height = appsContainer.getHeight();
                        for (ImageView im : apps) {
                            im.startAnimation(secondAnim);
                        }
                        int[] location = new int[2];
                        appsContainer.getLocationOnScreen(location);
                        if (initialTouchX > location[0]
                                && initialTouchX < location[0] + width
                                && initialTouchY > location[1]
                                && initialTouchY < location[1] + height) {

                            int numApp = 0, perApp = 0;
                            switch (applayout) {
                                case VERTICAL:
                                    perApp = height / apps.size();
                                    numApp = (int) (height - (initialTouchY - location[1])) / perApp;
                                    break;
                                case HORIZONTAL:
                                    perApp = width / apps.size();
                                    numApp = (int) (width - (initialTouchX - location[0])) / perApp;
                                    break;
                            }
                            if (!apporder) numApp = maxcount - numApp - 1;
                            if (swi.switchapps.size() > numApp) {
                                AppInfo appInf = swi.switchapps.get(numApp);

                                Intent in = new Intent(Intent.ACTION_MAIN);
                                in.setClassName(appInf.getPackagename(), appInf.getClassname());
                                //  in.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                in.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); // with animation or not
                                startActivity(in);
                            }

                        }
                        visible = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        boolean doIt = false;
                        switch (sweepDirection) {
                            case BottomTop:
                                if (initialTouchY - event.getRawY() > size.y * 0.05 && !visible) {
                                    doIt = true;
                                }
                                break;
                            case TopBottom:
                                if (event.getRawY() - initialTouchY > size.y * 0.05 && !visible) {
                                    doIt = true;
                                }
                                break;
                            case RightLeft:
                                if (initialTouchX - event.getRawX() > size.x * 0.05 && !visible) {
                                    doIt = true;
                                }
                                break;
                            case LeftRight:
                                if (event.getRawX() - initialTouchX > size.x * 0.05 && !visible) {
                                    doIt = true;
                                }
                                break;
                            default:
                                sweepDirection = BottomTop;
                                break;
                        }
                        if (doIt) {
                            update();
                            appsContainer.setVisibility(View.VISIBLE);
                            for (ImageView im : apps) {
                                im.startAnimation(firstAnim);
                            }
                            visible = true;
                        }
                        break;
                }
                return false;
            }
        });
        loadSettings();
        butParams = getLayoutParams(butX, butY, butWidth, butHeight);
        initialiseAppPanel(false);
        windowManager.addView(touchpanel, butParams);
        windowManager.addView(appsContainer, appsParams);

    }

    private LayoutParams getLayoutParams(int x, int y, int width, int height){
        LayoutParams Params = new WindowManager.LayoutParams(
                LayoutParams.TYPE_PHONE, // TYPE_APPLICATION_OVERLAY
                LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        Params.gravity = Gravity.TOP | Gravity.LEFT;
        Params.x=x;
        Params.y=y;
       // //(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, y, getResources().getDisplayMetrics());
        Params.width= width;
        Params.height= height;
        return Params;
    }

    private void loadSettings()
    {
        if(!mSettings.contains(FloatingSwitcher.APP_PREFERENCES_POINT_COUNT)) return;
        pointCount=mSettings.getInt(APP_PREFERENCES_POINT_COUNT, defaultInc);
        getIncs();
        butWidth=mSettings.getInt(APP_PREFERENCES_BUTTON_WIDTH, butWidth)*incWidth;
        butHeight= mSettings.getInt(APP_PREFERENCES_BUTTON_HEIGHT, butHeight)*butHeight;
        butY= mSettings.getInt(APP_PREFERENCES_BUTTON_Y, butY) * (incY-( butHeight/pointCount));;
        butX=mSettings.getInt(APP_PREFERENCES_BUTTON_X, butX)* (incX-(butWidth/pointCount));
        sweepDirection=mSettings.getInt(APP_PREFERENCES_SWEEP_DIRECTION, BottomTop);
        maxcount=mSettings.getInt(APP_PREFERENCES_APP_COUNT, defaultInc)+1;
        apporder=mSettings.getInt(APP_PREFERENCES_APP_ORDER, 0)==0;
        applayout=mSettings.getInt(APP_PREFERENCES_APP_LAYOUT, applayout);
        switch (applayout)
        {
            case VERTICAL:
                appX=mSettings.getInt(APP_PREFERENCES_APP_X, appX)* (incX-(picSize/pointCount));
                appY=mSettings.getInt(APP_PREFERENCES_APP_Y, appY)* (incY-(picSize*maxcount/pointCount));
                break;
            case HORIZONTAL:
                appX=mSettings.getInt(APP_PREFERENCES_APP_X, appX)* (incX-(picSize*maxcount/pointCount));
                appY=mSettings.getInt(APP_PREFERENCES_APP_Y, appY)* (incY-(picSize/pointCount));
                break;
        }
        setAnim(applayout, 0);
        appanim=mSettings.getInt(APP_PREFERENCES_APP_ANIM, appanim);
    }
    private void initialiseAppPanel(boolean newpanel)
    {
        apps=new ArrayList<ImageView>();
        for(int i=0;i<maxcount;i++){
            apps.add(new ImageView(this));
        }
        //im.setImageAlpha(0);
        appsContainer =new LinearLayout(getApplication());
        appsContainer.setOrientation(LinearLayout.VERTICAL);
        for(ImageView im: apps) {
            appsContainer.addView(im,new LinearLayout.LayoutParams(picSize,picSize));
        }
        appsContainer.setVisibility(View.GONE);
        appsParams =getLayoutParams(appX, appY, picSize, picSize * maxcount);
        if(newpanel) windowManager.addView(appsContainer, appsParams);
        swi.maxcount=maxcount;
    }
    private void PropertyChanger(LayoutParams params, int value, String action)
    {
         if (action.equals(ACTION_CHANGE_X)) {
             params.x = value * (incX-(params.width/pointCount));
         }
         else if (action.equals(ACTION_CHANGE_Y)) {
             params.y = value * (incY-( params.height/pointCount));
        }
         else if( action.equals(ACTION_CHANGE_WiDTH)) {
             int currValue = params.x / (incX-(params.width/pointCount));
             params.width=value * incWidth;
             params.x = currValue * (incX-(params.width/pointCount));
        }
         else if( action.equals(ACTION_CHANGE_HEIGHT)) {
             int currValue = params.y / (incY-( params.height/pointCount));
             params.height=value * incHeight;
             params.y = currValue * (incY-( params.height/pointCount));
        }
    }
    private void getIncs()
    {
        incX=size.x/pointCount+1;
        incY=size.y/pointCount-1;
    }
    private void setAnim(int layout, int anim)
    {
        switch (layout)
        {
            case VERTICAL:
                firstAnim = AnimationUtils.loadAnimation(getApplication(), R.anim.emerge_from_left);
                secondAnim = AnimationUtils.loadAnimation(getApplication(), R.anim.hide_from_left);
                break;
            case HORIZONTAL:
                firstAnim = AnimationUtils.loadAnimation(getApplication(), R.anim.emerge_from_bottom);
                secondAnim = AnimationUtils.loadAnimation(getApplication(), R.anim.hide_from_bottom);
                break;
        }
        secondAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                appsContainer.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }


}
