package com.example.erros.myll;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by user on 07.09.2017.
 */

public class FloatingWindowContainer {

    private final int BottomTop = 0;
    private final int RightLeft = 1;
    private final int TopBottom = 2;
    private final int LeftRight = 3;
    private int sweepDirection = BottomTop;

    // layouts
    public static final int VERTICAL = 0;
    public static final int HORIZONTAL = 1;

    private WindowManager windowManager;
    private Context context;
    private ISwitcherService service;

    // touch panel
    private ImageView touchPanel;
    private WindowManager.LayoutParams butParams;

    // iconViews panel
    private LinearLayout iconContainer;
    private ArrayList<ImageView> iconViews;
    private WindowManager.LayoutParams iconPanelParams;

    private ImageView backgroundWindow;

    private Animation firstAnim;
    private Animation secondAnim;


    private Point screenSize = new Point();
    private int pointCount;
    private int maxCount;

    // icon
    private int iconSize;
    private int iconSizeInc;
    private int iconSizeMin;


    private int incX;
    private int incY;
    private int incWidth;
    private int incHeight;


    private boolean iconOrder;
    private int iconPanelLayout;
    private int iconAnim;

    public boolean iconPanelIsVisible = true;

    public FloatingWindowContainer(Context context,ISwitcherService service, WindowManager windowManager, int maxCount,
                                   int pointCount, int iconLayout, boolean iconOrder, int buttonWidth, int buttonHeight,
                                   int buttonX, int buttonY, int sweepDirection, int iconSize, int iconPanelX, int iconPanelY, int iconAnim)
    {
        this.context = context;
        this.service = service;
        this.windowManager = windowManager;
        this.pointCount = pointCount;
        this.maxCount = maxCount;
        this.iconOrder = iconOrder;
        this.iconPanelLayout = iconLayout;
        this.sweepDirection = sweepDirection;
        updateScreenSize();
        this.iconSize = calculteIconSize(iconSize);
        setAnim(iconAnim);
        initialiseBackgroung();
        initialiseButton(touchPanel, butParams, buttonX, buttonY, buttonHeight, buttonWidth);
        initialiseIconPanel(true, iconPanelX, iconPanelY);

    }
    private void calculateIncs()
    {
        incX = screenSize.x / pointCount + 1;
        incY = screenSize.y / pointCount - 1;
        incWidth = incHeight = Math.max(screenSize.y, screenSize.x) / 2 / pointCount;
        iconSizeMin = Math.max(screenSize.y, screenSize.x) / 20;
        int iconSizeMax = Math.max(screenSize.y, screenSize.x) / 8;
        iconSizeInc = (iconSizeMax - iconSizeMin) / 20;

    }
    private void initialiseIconPanel(boolean newpanel, int iconPanelX, int iconPanelY)
    {
        // new panel is new then iconPanelX and iconPanelY is the values of Spinners else it is the real position on the scren

        iconViews = new ArrayList<ImageView>();
        for(int i=0; i < maxCount; i++){
            iconViews.add(new ImageView(context));
        }
        if(iconContainer != null)
        {
            iconContainer.removeAllViews();
        }
        else {
            iconContainer = new LinearLayout(context);
            iconContainer.setOrientation( iconPanelLayout == VERTICAL ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL );
        }

        if(iconPanelIsVisible) {
            iconContainer.setVisibility(View.VISIBLE);
            iconContainer.setBackgroundColor(Color.BLUE);
        }
        else {
            iconContainer.setVisibility(View.GONE);
            iconContainer.setBackgroundColor(Color.TRANSPARENT);
        }

        int width = 0, height = 0;
        switch (iconContainer.getOrientation())
        {
            case LinearLayout.HORIZONTAL:
                width = iconSize * maxCount;
                height = iconSize;
                break;
            case LinearLayout.VERTICAL:
                height = iconSize * maxCount;
                width = iconSize;
                break;
        }
        int appX = iconPanelX, appY = iconPanelY;
        if(newpanel) {
            switch (iconPanelLayout) {
                case VERTICAL:
                    appX = iconPanelX * (incX - (iconSize / pointCount));
                    appY = iconPanelY * (incY - (iconSize * maxCount / pointCount));
                    break;
                case HORIZONTAL:
                    appX = iconPanelX * (incX - (iconSize * maxCount / pointCount));
                    appY = iconPanelY * (incY - (iconSize / pointCount));
                    break;
            }
        }

        if(appX + width > screenSize.x)
        {
            appX = screenSize.x - width;
        }
        if(appY + height > screenSize.y)
        {
            appY = screenSize.y - height;
        }

        iconPanelParams = getLayoutParams(appX, appY, width, height );

        for(ImageView image: iconViews) {
            iconContainer.addView(image,new LinearLayout.LayoutParams(iconSize, iconSize));
        }
        if(newpanel) {
            windowManager.addView(iconContainer, iconPanelParams);
        }
        else recycleViews();
    }
    private void initialiseBackgroung()
    {
        backgroundWindow = new ImageView(context);
        backgroundWindow.setImageResource(R.drawable.background);
        backgroundWindow.setAlpha(0.5f);
        backgroundWindow.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        WindowManager.LayoutParams params = getLayoutParams(0, 0, screenSize.x, screenSize.y);
        windowManager.addView(backgroundWindow, params);
        hideBackground();
    }

    private void initialiseButton(ImageView view, WindowManager.LayoutParams params, int x, int y, int height, int width) {
        touchPanel = new ImageView(context);
        touchPanel.setImageResource(R.drawable.touchpanel);
        touchPanel.setOnTouchListener(new View.OnTouchListener() {
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
                        windowManager.removeView(touchPanel);
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
                        int width = iconContainer.getWidth();
                        int height = iconContainer.getHeight();
                        if(visible) {
                            for (ImageView im : iconViews) {
                                im.startAnimation(secondAnim);
                            }
                        }
                        int[] location = new int[2];
                        iconContainer.getLocationOnScreen(location);
                        if (initialTouchX > location[0]
                                && initialTouchX < location[0] + width
                                && initialTouchY > location[1]
                                && initialTouchY < location[1] + height) {

                            int numIcon = 0, perIcon = 0;
                            switch (iconPanelLayout) {
                                case VERTICAL:
                                    perIcon = height / iconViews.size();
                                    numIcon = (int) (height - (initialTouchY - location[1])) / perIcon;
                                    break;
                                case HORIZONTAL:
                                    perIcon = width / iconViews.size();
                                    numIcon = (int) (width - (initialTouchX - location[0])) / perIcon;
                                    break;
                            }
                            if (!iconOrder) numIcon = maxCount - numIcon - 1;
                            service.startApplication(numIcon);

                        }
                        visible = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        boolean doIt = false;
                        switch (sweepDirection) {
                            case BottomTop:
                                if (initialTouchY - event.getRawY() > screenSize.y * 0.05 && !visible) {
                                    doIt = true;
                                }
                                break;
                            case TopBottom:
                                if (event.getRawY() - initialTouchY > screenSize.y * 0.05 && !visible) {
                                    doIt = true;
                                }
                                break;
                            case RightLeft:
                                if (initialTouchX - event.getRawX() > screenSize.x * 0.05 && !visible) {
                                    doIt = true;
                                }
                                break;
                            case LeftRight:
                                if (event.getRawX() - initialTouchX > screenSize.x * 0.05 && !visible) {
                                    doIt = true;
                                }
                                break;
                            default:
                                sweepDirection = BottomTop;
                                break;
                        }
                        if (doIt) {
                            service.updateAppList();
                            iconContainer.setVisibility(View.VISIBLE);
                            showBackground();
                            for (ImageView im : iconViews) {
                                im.startAnimation(firstAnim);
                            }
                            visible = true;
                        }
                        break;
                }
                return false;
            }
        });
        int butWidth = calculateWidth(width);
        int butHeight = calculateHeight(height);
        int butX = calculateX(butWidth, x);
        int butY = calculateY(butHeight, y);
        butParams = getLayoutParams(butX, butY, butWidth, butHeight);
        windowManager.addView(touchPanel, butParams);
    }
    private void recycleViews()
    {
       // Log.e("RECYCLE", postMethod + "");
            touchPanel.post(new Runnable() {
                @Override
                public void run() {
                    windowManager.updateViewLayout(touchPanel, butParams);
                }
            });
            iconContainer.post(new Runnable() {
                @Override
                public void run() {
                    windowManager.updateViewLayout(iconContainer, iconPanelParams);
                }
            });


    }
    private int calculteIconSize( int value )
    {
        return iconSizeMin + iconSizeInc * value;
    }
    private int calculateX(int width, int value)
    {
        if(value == pointCount)
            return screenSize.x - width;
        return //value * ((screenSize.x - width) / pointCount);
         value * (incX - ( width / pointCount));
    }
    private void changeX(WindowManager.LayoutParams params, int value)
    {
        params.x = calculateX(params.width, value);
    }
    private int calculateY(int height, int value)
    {
        if(value == pointCount)
            return screenSize.y - height;
       return //value * ((screenSize.y - height) / pointCount);
         value * (incY - ( height / pointCount));
    }
    private void changeY(WindowManager.LayoutParams params, int value)
    {
        params.y = calculateY(params.height, value);
    }
    private int calculateHeight(int value)
    {
        return value * incHeight;
    }

    private void changeHeight(WindowManager.LayoutParams params, int value)
    {
        int currValue = params.y / (incY - ( params.height / pointCount));
        params.height = calculateHeight(value);
        params.y = calculateY(params.height, currValue);
    }
    private int calculateWidth(int value)
    {
        return incWidth * value;
    }

    private void changeWidth(WindowManager.LayoutParams params, int value)
    {
        int currValue = params.x / (incX - (params.width / pointCount));
        params.width = calculateWidth(value);
        params.x = calculateX(params.width, currValue);
    }
    public void changeButtonWidth(int value)
    {
        changeWidth(butParams, value);
        recycleViews();
    }
    public void changeButtonHeight(int value)
    {
        changeHeight(butParams, value);
        recycleViews();
    }
    public void changeButtonX(int value)
    {
        changeX(butParams, value);
        recycleViews();
    }
    public void changeButtonY(int value)
    {
        changeY(butParams, value);
        recycleViews();
    }
    public void ChangeIconPanelX(int value)
    {
        changeX(iconPanelParams, value);
        recycleViews();
    }
    public void ChangeIconPanelY(int value)
    {
        changeY(iconPanelParams, value);
        recycleViews();
    }
    public void setSweepDirection( int sweepDirection )
    {
        this.sweepDirection = sweepDirection;
    }
    public void changeIconOrder( boolean iconOrder )
    {
        this.iconOrder = iconOrder;
    }
    public void setMaxCount( int maxCount )
    {
        this.maxCount = maxCount;
        initialiseIconPanel(false, iconPanelParams.x, iconPanelParams.y);
    }
    public void changeIconSize( int value )
    {
        this.iconSize = calculteIconSize(value);
        initialiseIconPanel(false, iconPanelParams.x, iconPanelParams.y);
    }
    public void setIconViews(ArrayList<Drawable> icons)
    {
        int i = iconOrder ? maxCount - 1 : 0;
        for (ImageView im : iconViews) {
            if( icons.size()>i)
            {
                im.setImageDrawable(icons.get(i));
            }
            else {
                im.setImageDrawable(null);
            }
            if(iconOrder) --i;
            else ++i;
        }
    }
    public void setAnim( int animation )
    {
        this.iconAnim = animation;
        switch (iconPanelLayout)
        {
            case VERTICAL:
                if(animation == 0) {
                    firstAnim = AnimationUtils.loadAnimation(context, R.anim.emerge_from_left);
                    secondAnim = AnimationUtils.loadAnimation(context, R.anim.hide_from_left);
                } else {
                    firstAnim = AnimationUtils.loadAnimation(context, R.anim.emerge_from_right);
                    secondAnim = AnimationUtils.loadAnimation(context, R.anim.hide_from_right);
                }
                break;
            case HORIZONTAL:
                if(animation == 0) {
                    firstAnim = AnimationUtils.loadAnimation(context, R.anim.emerge_from_bottom);
                    secondAnim = AnimationUtils.loadAnimation(context, R.anim.hide_from_bottom);
                } else {
                    firstAnim = AnimationUtils.loadAnimation(context, R.anim.emerge_from_top);
                    secondAnim = AnimationUtils.loadAnimation(context, R.anim.hide_from_top);
                }
                break;
        }
        secondAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                hideBackground();
                if(!iconPanelIsVisible) {
                    iconContainer.setVisibility(View.GONE);
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void showIconPanel()
    {
        iconContainer.setVisibility(View.VISIBLE);
        iconContainer.setBackgroundColor(Color.BLUE);
        iconPanelIsVisible = true;
        recycleViews();
    }
    public void hideIconPanel()
    {
        iconContainer.setVisibility(View.GONE);
        iconContainer.setBackgroundColor(Color.TRANSPARENT);
        iconPanelIsVisible = false;
        recycleViews();
        hideBackground();
    }
    private void showBackground()
    {
        backgroundWindow.setVisibility(View.VISIBLE);
    }
    private void hideBackground()
    {
        backgroundWindow.setVisibility(View.GONE);
    }

    public void changeIconLayout(int layout)
    {
        switch (layout) {
            case VERTICAL:
                iconContainer.setOrientation(LinearLayout.VERTICAL);
                iconPanelParams.width = iconSize;
                iconPanelParams.height = iconSize * maxCount;
                changeY(iconPanelParams, 0);
                break;
            case HORIZONTAL:
                iconContainer.setOrientation(LinearLayout.HORIZONTAL);
                iconPanelParams.width = iconSize * maxCount;
                iconPanelParams.height = iconSize;
                changeX(iconPanelParams, 0);
                break;
        }
        recycleViews();
    }

    public void removeViews()
    {
        windowManager.removeView(iconContainer);
        windowManager.removeView(touchPanel);
    }
    public void updateScreenSize()
    {
        windowManager.getDefaultDisplay().getSize(screenSize);
        calculateIncs();
    }


    private WindowManager.LayoutParams getLayoutParams(int x, int y, int width, int height){
        WindowManager.LayoutParams Params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_PHONE, // TYPE_APPLICATION_OVERLAY
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        Params.gravity = Gravity.TOP | Gravity.LEFT;
        Params.x=x;
        Params.y=y;
        // //(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, y, getResources().getDisplayMetrics());
        Params.width= width;
        Params.height= height;
        return Params;
    }

    /*

      if(!mSettings.contains(FloatingSwitcher.APP_PREFERENCES_POINT_COUNT)) return;
        int pointCount = mSettings.getInt(APP_PREFERENCES_POINT_COUNT, defaultInc);
        int butWidth = mSettings.getInt(APP_PREFERENCES_BUTTON_WIDTH, butWidth) * incWidth;
        butHeight = mSettings.getInt(APP_PREFERENCES_BUTTON_HEIGHT, butHeight) * incHeight;
        butY = mSettings.getInt(APP_PREFERENCES_BUTTON_Y, butY) * (incY - (butHeight/pointCount));
        butX = mSettings.getInt(APP_PREFERENCES_BUTTON_X, butX) * (incX - (butWidth/pointCount));
        sweepDirection = mSettings.getInt(APP_PREFERENCES_SWEEP_DIRECTION, BottomTop);
        maxcount = mSettings.getInt(APP_PREFERENCES_APP_COUNT, defaultInc) + 1;
        iconCurPos = mSettings.getInt(APP_PREFERENCES_APP_ICON_SIZE, 0);
        iconSize = iconSizeMin + iconSizeInc * iconCurPos;
        iconOrder = mSettings.getInt(APP_PREFERENCES_APP_ORDER, 0) == 0;
        applayout = mSettings.getInt(APP_PREFERENCES_APP_LAYOUT, applayout);
        switch (applayout)
        {
            case VERTICAL:
                appX = mSettings.getInt(APP_PREFERENCES_APP_X, appX) * (incX - (iconSize / pointCount));
                appY = mSettings.getInt(APP_PREFERENCES_APP_Y, appY) * (incY - (iconSize * maxcount / pointCount));
                break;
            case HORIZONTAL:
                appX = mSettings.getInt(APP_PREFERENCES_APP_X, appX) * (incX - (iconSize * maxcount / pointCount));
                appY = mSettings.getInt(APP_PREFERENCES_APP_Y, appY) * (incY - (iconSize / pointCount));
                break;
        }
        setAnim(applayout, 0);
        appanim = mSettings.getInt(APP_PREFERENCES_APP_ANIM, appanim);



        butParams = getLayoutParams(butX, butY, butWidth, butHeight);
        initialiseIconPanel(true);
    */


}
