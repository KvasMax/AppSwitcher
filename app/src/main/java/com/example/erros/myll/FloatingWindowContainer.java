package com.example.erros.myll;

import android.content.Context;
import android.content.res.Configuration;
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
    private ImageView buttonView;
    private WindowManager.LayoutParams butParams;

    // iconViews panel
    private LinearLayout iconContainer;
    private ArrayList<ImageView> iconViews;
    private WindowManager.LayoutParams iconPanelParams;

    private ImageView backgroundView;
    private WindowManager.LayoutParams backgroundParams;

    private Animation firstAnim;
    private Animation secondAnim;


    private Point screenSize = new Point();
    private Point fullScreenSize = new Point();
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
    private boolean isDraggableFloatingButton = false;
    private boolean isDraggableIconPanel = false;

    private int screenOrientation;
    private int buttonPortraitX;
    private int buttonPortraitY;
    private int buttonLandscapeX;
    private int buttonLandscapeY;
    private int buttonColor = Color.TRANSPARENT;

    private int statusBarHeight = 0;

    public FloatingWindowContainer(Context context,ISwitcherService service, WindowManager windowManager, int maxCount,
                                   int pointCount, int iconLayout, boolean iconOrder, int buttonWidth, int buttonHeight,
                                   int buttonPortraitX, int buttonPortraitY, int buttonLandscapeX, int buttonLandscapeY,
                                   int sweepDirection, int iconSize, int iconPanelX, int iconPanelY, int iconAnim, int buttonOrientation, int buttonColor)
    {
        this.context = context;
        this.service = service;
        this.windowManager = windowManager;
        this.pointCount = pointCount;
        this.maxCount = maxCount;
        this.iconOrder = iconOrder;
        this.iconPanelLayout = iconLayout;
        this.sweepDirection = sweepDirection;
        this.screenOrientation = buttonOrientation;
        this.buttonPortraitX = buttonPortraitX;
        this.buttonPortraitY = buttonPortraitY;
        this.buttonLandscapeX = buttonLandscapeX;
        this.buttonLandscapeY = buttonLandscapeY;
        this.buttonColor = buttonColor;
        updateScreenSize();
        this.iconSize = calculteIconSize(iconSize);
        setAnim(iconAnim);

        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight =  context.getResources().getDimensionPixelSize(resourceId);
        }
        initialiseBackground();
        initialiseButton(buttonView, butParams, buttonHeight, buttonWidth);
        initialiseIconPanel(true, iconPanelX, iconPanelY);
        initialiseFullScreenDetector();

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
      /*  if(newpanel) {
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
        }*/

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
            iconContainer.setOnTouchListener(new View.OnTouchListener() {
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = iconPanelParams.x;
                            initialY = iconPanelParams.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_UP:

                            break;
                        case MotionEvent.ACTION_MOVE:
                            if(isDraggableIconPanel) {
                                iconPanelParams.x = initialX - (int) (initialTouchX - event.getRawX());
                                iconPanelParams.y = initialY - (int) (initialTouchY - event.getRawY());
                                recycleViews();
                            }
                            break;
                    }
                    return false;
                }
            });
        }
        else recycleViews();
    }
    private void initialiseBackground()
    {
        backgroundView = new ImageView(context);
        backgroundView.setImageResource(R.drawable.background);
        backgroundView.setAlpha(0.5f);
        backgroundView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        backgroundParams = getLayoutParams(0, 0, fullScreenSize.x, fullScreenSize.y);
        windowManager.addView(backgroundView, backgroundParams);
        hideBackground();
    }

    private void initialiseButton(ImageView view, WindowManager.LayoutParams params, int height, int width) {
        buttonView = new ImageView(context);
        //buttonView.setImageResource(R.drawable.touchpanel);
        buttonView.setBackgroundColor(buttonColor);
        buttonView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            boolean visible = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = butParams.x;
                        initialY = butParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        if(!isDraggableFloatingButton) {
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            int width = iconContainer.getWidth();
                            int height = iconContainer.getHeight();
                            if (visible) {
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
                        } else {
                            switch (screenOrientation)
                            {
                                case Configuration.ORIENTATION_PORTRAIT:
                                    roundPosition();
                                    buttonPortraitX = butParams.x;
                                    buttonPortraitY = butParams.y;
                                    buttonLandscapeX = (int)Math.ceil(((double) (buttonPortraitX + butParams.width) / screenSize.x) * screenSize.y - butParams.width);
                                    buttonLandscapeY = (int)Math.ceil(((double)(buttonPortraitY + butParams.height) / screenSize.y) * screenSize.x - butParams.height);
                                    break;
                                case Configuration.ORIENTATION_LANDSCAPE:
                                    roundPosition();
                                    buttonLandscapeX = butParams.x;
                                    buttonLandscapeY = butParams.y;
                                    buttonPortraitX = (int)Math.ceil(((double) (buttonLandscapeX + butParams.width) / screenSize.x) * screenSize.y - butParams.width);
                                    buttonPortraitY = (int)Math.ceil(((double)(buttonLandscapeY + butParams.height) / screenSize.y) * screenSize.x - butParams.height);
                                    break;
                            }
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(!isDraggableFloatingButton) {
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
                        }
                        else {

                            int newX = initialX - (int) (initialTouchX - event.getRawX());
                            int newY = initialY - (int) (initialTouchY - event.getRawY());
                            if(newX >= 0 && screenSize.x >= newX + butParams.width)
                            {
                                butParams.x = newX;
                            }
                            if(newY >= 0 &&  screenSize.y >= newY + butParams.height)
                            {
                                butParams.y = newY;

                            }
                            recycleViews();
                        }
                        break;
                }
                return false;
            }
        });
        int butWidth = calculateWidth(width);
        int butHeight = calculateHeight(height);
        //int butX = calculateX(butWidth, x);
       // int butY = calculateY(butHeight, y);
        switch (screenOrientation)
        {
            case Configuration.ORIENTATION_PORTRAIT:
                butParams = getLayoutParams(buttonPortraitX, buttonPortraitY, butWidth, butHeight);
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                butParams = getLayoutParams(buttonLandscapeX, buttonLandscapeY, butWidth, butHeight);
                break;
        }

        windowManager.addView(buttonView, butParams);
    }
    private void roundPosition()
    {
        if(butParams.x < screenSize.x * 0.01f) {
            butParams.x = 0;
        }
        if(screenSize.x - (butParams.x + butParams.width)  < screenSize.x * 0.01f) {
            butParams.x = screenSize.x - butParams.width;
        }
        if(butParams.y < screenSize.y * 0.01f) {
            butParams.y = 0;
        }
        if(screenSize.y - (butParams.y + butParams.height)  < screenSize.y * 0.01f) {
            butParams.y = screenSize.y - butParams.height;
        }
    }
    private void initialiseFullScreenDetector()
    {
        LinearLayout fullScreenDetectorView = new LinearLayout(context);
        WindowManager.LayoutParams fullScreenDetectorParams = getLayoutParams(0 ,0, WindowManager.LayoutParams.MATCH_PARENT, 0);
        fullScreenDetectorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int i) {
                Log.e("SCREEN", screenSize.x + " " + screenSize.y);
                Log.e("SCREEN", fullScreenSize.x + " " + fullScreenSize.y);
                Log.e("BUTTON POS", butParams.x + " " + butParams.y);
                Log.e("BUTTON PARAM", butParams.width + " " + butParams.height);

                if( i == 0)
                {
                    switch (screenOrientation)
                    {
                        case Configuration.ORIENTATION_PORTRAIT:
                            butParams.x = buttonPortraitX;
                            butParams.y = buttonPortraitY;
                            break;
                        case Configuration.ORIENTATION_LANDSCAPE:
                            butParams.x = buttonLandscapeX;
                            butParams.y = buttonLandscapeY;
                            break;
                    }
                } else {
                    if(butParams.x + butParams.width == screenSize.x)
                    {
                        butParams.x += fullScreenSize.x - screenSize.x;
                        Log.e("FULL", "EXPAND X");
                    }
                    if(butParams.y + butParams.height == screenSize.y)
                    {
                        butParams.y += fullScreenSize.y - screenSize.y;
                        Log.e("FULL", "EXPAND Y");
                    } else if( butParams.y != 0){
                        butParams.y += statusBarHeight;
                    }
                }
                recycleViews();
            }
        });
        windowManager.addView(fullScreenDetectorView, fullScreenDetectorParams);
    }
    private void recycleViews()
    {
       // Log.e("RECYCLE", postMethod + "");
            buttonView.post(new Runnable() {
                @Override
                public void run() {
                    windowManager.updateViewLayout(buttonView, butParams);
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
        backgroundView.setVisibility(View.VISIBLE);
    }
    private void hideBackground()
    {
        backgroundView.setVisibility(View.GONE);
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
        windowManager.removeView(buttonView);
    }
    public void updateScreenSize()
    {
        windowManager.getDefaultDisplay().getSize(screenSize);
        windowManager.getDefaultDisplay().getRealSize(fullScreenSize);
        calculateIncs();
        if(backgroundParams != null) {
            backgroundParams.height = screenSize.y;
            backgroundParams.width = screenSize.x;
            windowManager.updateViewLayout(backgroundView, backgroundParams);
        }
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
    public void dragFloatingButton(boolean value)
    {
        isDraggableFloatingButton = value;
    }

    public void dragIconPanel(boolean value)
    {
        isDraggableIconPanel = value;
    }

    public Point getButtonPortraitPosition()
    {
        return new Point(buttonPortraitX, buttonPortraitY);
    }

    public Point getButtonLandscapePosition()
    {
        return new Point(buttonLandscapeX, buttonLandscapeY);
    }
    public Point getIconPanelPortraitPosition()
    {
        return new Point(iconPanelParams.x, iconPanelParams.y);
    }

    public void rotateScreen(int orientation)
    {
      //  Log.e("BUTTON", (butParams.x + butParams.width)+ " ");
      //  Log.e("BUTTON", ((Math.floor((double) (oldX + butParams.width) / screenSize.y)) + " "));
       // Log.e("BUTTON", (screenSize.y - butParams.width) + " ");
      //  Log.e("BUTTON", (((Math.floor((double) (oldX + butParams.width) / screenSize.y)) * (screenSize.y - butParams.width))) + " ");
        screenOrientation = orientation;
        switch (screenOrientation)
        {
            case Configuration.ORIENTATION_PORTRAIT:
                butParams.x = buttonPortraitX;
                butParams.y = buttonPortraitY;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                butParams.x = buttonLandscapeX;
                butParams.y = buttonLandscapeY;
                break;
        }

        Log.e("BUTTON Portrait", buttonPortraitX + " " + buttonPortraitY);
        Log.e("BUTTON Landscape", buttonLandscapeX + " " + buttonLandscapeY);
        recycleViews();
    }
    public void setButtonColor(int color)
    {
        buttonView.setBackgroundColor(color);
    }

}
