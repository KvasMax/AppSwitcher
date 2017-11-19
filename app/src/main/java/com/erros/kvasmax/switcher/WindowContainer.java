package com.erros.kvasmax.switcher;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
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

public class WindowContainer {

    private final int BUTTON_POSITION_BOTTOM = 0;
    private final int BUTTON_POSITION_RIGHT = 1;
    private final int BUTTON_POSITION_LEFT = 2;

    private final int APP_ANIMATION_SLIDE = 0;
    private final int APP_ANIMATION_SLIDE_ANOTHER = 1;
    private final int APP_ANIMATION_EMERGE = 2;


    // layouts
    public static final int VERTICAL = 0;
    public static final int HORIZONTAL = 1;

    private WindowManager windowManager;
    private Context context;
    private ISwitcherService service;

    // touch button
    private ImageView buttonView;
    private WindowManager.LayoutParams buttonParams;

    // iconViews
    private LinearLayout iconBar;
    private ArrayList<ImageView> iconViews;
    private WindowManager.LayoutParams iconBarParams;

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

    //private int incX;
    //private int incY;
    //private int incWidth;
    //private int incHeight;

    private int incrementLength;
    private int incrementThickness;


    private boolean iconOrder;
    private int iconBarLayout;
    private int iconAnim;

    public boolean iconBarIsVisible = true;
    private boolean isDraggableFloatingButton = false;
    private boolean isDraggableIconBar = false;
    private boolean avoidKeyboard = false;

    private int screenOrientation;
    private int buttonPosition;
    private int buttonPortraitX;
    private int buttonPortraitY;
    private int buttonLandscapeX;
    private int buttonLandscapeY;
    private int buttonColor = Color.TRANSPARENT;

    int distanceX;
    int distanceY;

    private int statusBarHeight = 0;


    public WindowContainer(Context context, ISwitcherService service, WindowManager windowManager, int maxCount,
                           int pointCount, int iconBarLayout, boolean iconOrder, int buttonPosition, int buttonThickness, int buttonLength,
                           int buttonPortraitX, int buttonPortraitY, int buttonLandscapeX, int buttonLandscapeY,
                           int iconSize, int distanceX, int distanceY, int iconAnim, int screenOrientation, int buttonColor, boolean avoidKeyboard) {
        this.context = context;
        this.service = service;
        this.windowManager = windowManager;
        this.pointCount = pointCount;
        this.maxCount = maxCount;

        this.iconOrder = iconOrder;
        this.iconBarLayout = iconBarLayout;
        this.buttonPosition = buttonPosition;
        this.avoidKeyboard = avoidKeyboard;
        this.screenOrientation = screenOrientation;
        this.buttonPortraitX = buttonPortraitX;
        this.buttonPortraitY = buttonPortraitY;
        this.buttonLandscapeX = buttonLandscapeX;
        this.buttonLandscapeY = buttonLandscapeY;
        this.distanceX = distanceX;
        this.distanceY = distanceY;

        this.buttonColor = buttonColor;
        updateScreenSize();
        this.iconSize = calculateIconSize(iconSize);
        setAnimation(iconAnim);

        calculateStatusBarHeight();
        initialiseButton(buttonLength, buttonThickness);
        initialiseIconBar(true);
    }

    public WindowContainer(Context context, ISwitcherService service, WindowManager windowManager, int maxCount, int pointCount,
                           int screenOrientation, int iconBarLayout, boolean iconOrder, int iconAnim, int buttonPosition, int buttonThickness,
                           int buttonLength, int buttonColor, int iconSize, boolean avoidKeyboard) {
        this.context = context;
        this.service = service;
        this.windowManager = windowManager;
        this.pointCount = pointCount;
        this.maxCount = maxCount;
        this.avoidKeyboard = avoidKeyboard;

        updateScreenSize();
        this.buttonColor = buttonColor;
        this.iconSize = calculateIconSize(iconSize);

        this.iconOrder = iconOrder;
        this.iconBarLayout = iconBarLayout;
        this.buttonPosition = buttonPosition;
        this.screenOrientation = screenOrientation;
        setAnimation(iconAnim);

        int buttonLengthPixels = getLength(buttonLength);
        if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
            this.buttonPortraitX = 0;
            this.buttonPortraitY = (screenSize.y - buttonLengthPixels) / 2;
            this.distanceX = -screenSize.x / 6;
            this.distanceY = -screenSize.y / 8;
        } else {
            this.buttonLandscapeX = 0;
            this.buttonLandscapeY = (screenSize.y - buttonLengthPixels) / 2;
            this.distanceX = -screenSize.x / 12;
            this.distanceY = -screenSize.y / 16;
        }

        calculateStatusBarHeight();
        initialiseButton(buttonLength, buttonThickness);
        initialiseIconBar(true);
        calculateButtonCoordinates();
    }

    private void calculateStatusBarHeight() {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
    }

    private void calculateIncs() {

        incrementLength = (int) Math.ceil((double) Math.min(screenSize.y, screenSize.x) / pointCount);
        incrementThickness = (int) Math.ceil((double) Math.min(screenSize.y, screenSize.x) / 10 / pointCount);
        iconSizeMin = (int) Math.ceil((double) Math.max(screenSize.y, screenSize.x) / 20);
        int iconSizeMax = Math.max(screenSize.y, screenSize.x) / 8;
        iconSizeInc = (int) Math.ceil((double) (iconSizeMax - iconSizeMin) / pointCount);
    }

    private void initialiseIconBar(boolean isNewBar) {

        iconViews = new ArrayList<>();
        for (int i = 0; i < maxCount; i++) {
            iconViews.add(new ImageView(context));
        }
        if (iconBar != null) {
            iconBar.removeAllViews();
        } else {
            iconBar = new LinearLayout(context);
            iconBar.setBackgroundResource(R.drawable.rounded_corners);
        }
        iconBar.setOrientation(iconBarLayout == VERTICAL ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        changeIconBarBackground(iconBarIsVisible);

        GradientDrawable drawable = (GradientDrawable) iconBar.getBackground();
        Rect margins = new Rect();
        drawable.getPadding(margins);
        int width = 0, height = 0;
        int padding = margins.top * 2;
        switch (iconBar.getOrientation()) {
            case LinearLayout.HORIZONTAL:
                width = iconSize * maxCount + padding;
                height = iconSize + padding;
                break;
            case LinearLayout.VERTICAL:
                height = iconSize * maxCount + padding;
                width = iconSize + padding;
                break;
        }
        int appX = buttonParams.x - distanceX, appY = buttonParams.y - distanceY;

        iconBarParams = getLayoutParams(appX, appY, width, height);
        iconBarParams.gravity = buttonParams.gravity;
        for (ImageView image : iconViews) {
            iconBar.addView(image, new LinearLayout.LayoutParams(iconSize, iconSize));
        }
        changeIconBarDim(iconBarIsVisible);
        if (isNewBar) {

            windowManager.addView(iconBar, iconBarParams);
            iconBar.setOnTouchListener(new View.OnTouchListener() {
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;

                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if (isDraggableIconBar) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                initialX = iconBarParams.x;
                                initialY = iconBarParams.y;
                                initialTouchX = event.getRawX();
                                initialTouchY = event.getRawY();
                                break;

                            case MotionEvent.ACTION_UP:
                                calculateDistance();
                                service.saveWindowPositions();
                                break;

                            case MotionEvent.ACTION_MOVE:
                                int newX = buttonPosition != BUTTON_POSITION_RIGHT
                                        ? initialX - (int) (initialTouchX - event.getRawX()) : initialX + (int) (initialTouchX - event.getRawX());
                                ;
                                int newY = buttonPosition != BUTTON_POSITION_BOTTOM
                                        ? initialY - (int) (initialTouchY - event.getRawY()) : initialY + (int) (initialTouchY - event.getRawY());
                                ;
                                if (newX >= 0 && screenSize.x >= newX + iconBarParams.width) {
                                    iconBarParams.x = newX;
                                }
                                if (newY >= 0 && screenSize.y >= newY + iconBarParams.height) {
                                    iconBarParams.y = newY;
                                }
                                recycleViews();
                                break;
                        }
                    }
                    return false;
                }
            });
        }

    }

    private void calculateDistance() {
        distanceX = buttonParams.x - iconBarParams.x;
        distanceY = buttonParams.y - iconBarParams.y;
    }

    private void initialiseButton(int length, int thickness) {
        buttonView = new ImageView(context);
        buttonView.setBackgroundColor(buttonColor);
        buttonView.setBackgroundResource(R.drawable.rounded_corners);
        GradientDrawable drawable = (GradientDrawable) buttonView.getBackground();

        drawable.setColor(buttonColor);
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
                        initialX = buttonParams.x;
                        initialY = buttonParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!isDraggableFloatingButton) {
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            int width = iconBar.getWidth();
                            int height = iconBar.getHeight();
                            if (visible) {
                                for (ImageView im : iconViews) {
                                    im.setVisibility(View.VISIBLE);
                                    im.startAnimation(secondAnim);
                                }
                            }
                            int[] location = new int[2];
                            iconBar.getLocationOnScreen(location);
                            if (initialTouchX > location[0]
                                    && initialTouchX < location[0] + width
                                    && initialTouchY > location[1]
                                    && initialTouchY < location[1] + height) {

                                int numIcon = 0, perIcon = 0;
                                switch (iconBarLayout) {
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
                            roundPosition();
                            calculateButtonCoordinates();
                            service.saveWindowPositions();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int[] location = new int[2];
                        buttonView.getLocationOnScreen(location);
                        if (!isDraggableFloatingButton) {
                            if (!visible) {

                                if (event.getRawY() < location[1]
                                        || event.getRawY() > location[1] + buttonParams.height
                                        || event.getRawX() < location[0]
                                        || event.getRawX() > location[0] + buttonParams.width) {
                                    service.updateAppList();
                                    iconBar.setVisibility(View.VISIBLE);
                                    for (ImageView im : iconViews) {
                                        im.startAnimation(firstAnim);
                                    }
                                    visible = true;
                                }

                            }
                        } else {

                            int newX = initialX - (int) (initialTouchX - event.getRawX());
                            int newY = initialY - (int) (initialTouchY - event.getRawY());
                            if (buttonPosition == BUTTON_POSITION_BOTTOM && newX >= 0 && screenSize.x >= newX + buttonParams.width) {
                                buttonParams.x = newX;
                                buttonParams.y = 0;
                            }
                            if (buttonPosition != BUTTON_POSITION_BOTTOM && newY >= 0 && screenSize.y >= newY + buttonParams.height) {
                                buttonParams.y = newY;
                                buttonParams.x = 0;
                            }
                            recycleViews();
                        }
                        break;
                }
                return false;
            }
        });
        int buttonThickness = getThickness(thickness),
                buttonLength = getLength(length);
        switch (screenOrientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                updateButtonLayoutParams(buttonPosition, buttonPortraitX, buttonPortraitY, buttonLength, buttonThickness);
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                updateButtonLayoutParams(buttonPosition, buttonLandscapeX, buttonLandscapeY, buttonLength, buttonThickness);
                break;
        }

        windowManager.addView(buttonView, buttonParams);
    }

    private void roundPosition() {
        if (buttonParams.x < screenSize.x * 0.01f) {
            buttonParams.x = 0;
        }
        if (screenSize.x - (buttonParams.x + buttonParams.width) < screenSize.x * 0.01f) {
            buttonParams.x = screenSize.x - buttonParams.width;
        }
        if (buttonParams.y < screenSize.y * 0.01f) {
            buttonParams.y = 0;
        }
        if (screenSize.y - (buttonParams.y + buttonParams.height) < screenSize.y * 0.01f) {
            buttonParams.y = screenSize.y - buttonParams.height;
        }
    }

    private void recycleViews() {
        buttonView.post(new Runnable() {
            @Override
            public void run() {
                windowManager.updateViewLayout(buttonView, buttonParams);
            }
        });
        iconBar.post(new Runnable() {
            @Override
            public void run() {
                windowManager.updateViewLayout(iconBar, iconBarParams);
            }
        });

    }

    private int calculateIconSize(int value) {
        return iconSizeMin + iconSizeInc * value;
    }

    private void changeLength(WindowManager.LayoutParams params, int value) {
        int newLength = getLength(value);
        if (buttonPosition == BUTTON_POSITION_BOTTOM) {
            params.width = newLength;
        } else {
            params.height = newLength;
        }
    }

    private void changeThickness(WindowManager.LayoutParams params, int value) {
        int newThickness = getThickness(value);
        if (buttonPosition == BUTTON_POSITION_BOTTOM) {
            params.height = newThickness;
        } else {
            params.width = newThickness;
        }
    }

    private int getThickness(int value) {
        return ++value * incrementThickness;
    }

    private int getLength(int value) {
        return ++value * incrementLength;
    }


    public void changeButtonThickness(int value) {
        changeThickness(buttonParams, value);
        recycleViews();
    }

    public void changeButtonLength(int value) {
        changeLength(buttonParams, value);
        recycleViews();
    }

    public void changeIconOrder(boolean iconOrder) {
        this.iconOrder = iconOrder;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
        initialiseIconBar(false);
        recycleViews();
    }

    public void changeIconSize(int value) {
        this.iconSize = calculateIconSize(value);
        initialiseIconBar(false);
        recycleViews();
    }

    public void setIconViews(ArrayList<Drawable> icons) {
        int i = iconOrder ? maxCount - 1 : 0;
        for (ImageView im : iconViews) {
            if (icons.size() > i) {
                im.setVisibility(View.INVISIBLE);
                im.setImageDrawable(icons.get(i));
            } else {
                im.setImageDrawable(null);
            }
            if (iconOrder) --i;
            else ++i;
        }
    }

    public void setAnimation(int animation) {
        this.iconAnim = animation;
        switch (iconBarLayout) {
            case VERTICAL:
                switch (animation) {
                    case APP_ANIMATION_SLIDE:
                        firstAnim = AnimationUtils.loadAnimation(context, R.anim.slide_from_left);
                        secondAnim = AnimationUtils.loadAnimation(context, R.anim.slide_to_left);
                        break;
                    case APP_ANIMATION_SLIDE_ANOTHER:
                        firstAnim = AnimationUtils.loadAnimation(context, R.anim.slide_from_right);
                        secondAnim = AnimationUtils.loadAnimation(context, R.anim.slide_to_right);
                        break;
                    case APP_ANIMATION_EMERGE:
                        firstAnim = AnimationUtils.loadAnimation(context, R.anim.emerge);
                        secondAnim = AnimationUtils.loadAnimation(context, R.anim.plunge);
                        break;

                }
                break;
            case HORIZONTAL:
                switch (animation) {
                    case APP_ANIMATION_SLIDE:
                        firstAnim = AnimationUtils.loadAnimation(context, R.anim.slide_from_bottom);
                        secondAnim = AnimationUtils.loadAnimation(context, R.anim.slide_to_bottom);
                        break;
                    case APP_ANIMATION_SLIDE_ANOTHER:
                        firstAnim = AnimationUtils.loadAnimation(context, R.anim.slide_from_top);
                        secondAnim = AnimationUtils.loadAnimation(context, R.anim.slide_to_top);
                        break;
                    case APP_ANIMATION_EMERGE:
                        firstAnim = AnimationUtils.loadAnimation(context, R.anim.emerge);
                        secondAnim = AnimationUtils.loadAnimation(context, R.anim.plunge);
                        break;
                }
                break;
        }
        secondAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (!iconBarIsVisible) {
                    iconBar.setVisibility(View.GONE);
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void showIconBar() {
        changeIconBarBackground(true);
        iconBarIsVisible = true;
        recycleViews();
    }

    public void hideIconBar() {
        changeIconBarBackground(false);
        iconBarIsVisible = false;
        recycleViews();
    }

    private void changeIconBarBackground(boolean visible) {
        GradientDrawable drawable = (GradientDrawable) iconBar.getBackground();
        if (visible) {
            iconBar.setVisibility(View.VISIBLE);
            drawable.setColor(Color.BLUE);
            drawable.setAlpha(128);
        } else {
            iconBar.setVisibility(View.GONE);
            drawable.setColor(Color.TRANSPARENT);
        }
        changeIconBarDim(visible);
    }

    private void changeIconBarDim(boolean visible) {
        if (iconBarParams != null) {
            if (visible) {
                iconBarParams.dimAmount = 0;
            } else {
                iconBarParams.dimAmount = 0.7f;
            }

        }
    }

    public void changeIconBarLayout(int layout) {
        iconBarLayout = layout;
        initialiseIconBar(false);
        switch (buttonPosition) {
            case BUTTON_POSITION_BOTTOM:
                iconBarParams.x = 0;
                iconBarParams.y = buttonParams.height;
                break;
            case BUTTON_POSITION_RIGHT:
            case BUTTON_POSITION_LEFT:
                iconBarParams.x = buttonParams.width;
                iconBarParams.y = 0;
                break;
        }
        calculateDistance();
        recycleViews();
    }

    public void removeViews() {
        windowManager.removeView(iconBar);
        windowManager.removeView(buttonView);
    }

    private void updateScreenSize() {
        windowManager.getDefaultDisplay().getSize(screenSize);
        windowManager.getDefaultDisplay().getRealSize(fullScreenSize);
        calculateIncs();
    }


    private WindowManager.LayoutParams getLayoutParams(int x, int y, int width, int height) {
        WindowManager.LayoutParams Params = new WindowManager.LayoutParams(
                getTypeView(), // TYPE_APPLICATION_OVERLAY ---- TYPE_SYSTEM_OVERLAY
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_DIM_BEHIND,// | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        Params.gravity = Gravity.TOP | Gravity.LEFT;
        Params.x = x;
        Params.y = y;
        Params.width = width;
        Params.height = height;
        return Params;
      /* FIXME  WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_DIM_BEHIND  | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |WindowManager.LayoutParams.FLAG_FULLSCREEN;*/
    }

    public void dragFloatingButton(boolean value) {
        isDraggableFloatingButton = value;
    }

    public void dragIconBar(boolean value) {
        isDraggableIconBar = value;
    }

    public Point getButtonPortraitPosition() {
        return new Point(buttonPortraitX, buttonPortraitY);
    }

    public Point getButtonLandscapePosition() {
        return new Point(buttonLandscapeX, buttonLandscapeY);
    }

    public Point getIconBarDistance() {
        return new Point(distanceX, distanceY);
    }

    public void rotateScreen(final int orientation) {
        updateScreenSize();
        screenOrientation = orientation;
        switch (screenOrientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                buttonParams.x = buttonPortraitX;
                buttonParams.y = buttonPortraitY;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                buttonParams.x = buttonLandscapeX;
                buttonParams.y = buttonLandscapeY;
                break;
        }
        iconBarParams.x = buttonParams.x - distanceX;
        iconBarParams.y = buttonParams.y - distanceY;

        recycleViews();

    }

    public void setButtonColor(int color) {
        GradientDrawable drawable = (GradientDrawable) buttonView.getBackground();
        drawable.setColor(color);
    }


    public void changeButtonPosition(int position) {
        int previousPosition = this.buttonPosition;
        this.buttonPosition = position;
        int x = 0, y = 0, length = 0, thickness = 0;

        if ((previousPosition == BUTTON_POSITION_LEFT && position == BUTTON_POSITION_RIGHT)
                || (previousPosition == BUTTON_POSITION_RIGHT && position == BUTTON_POSITION_LEFT)) {
            x = buttonParams.x;
            y = buttonParams.y;
            length = buttonParams.height;
            thickness = buttonParams.width;
        } else {
            switch (screenOrientation) {
                case Configuration.ORIENTATION_PORTRAIT:
                    if (position == BUTTON_POSITION_BOTTOM) {
                        y = 0;
                        x = (int) Math.ceil(((double) (buttonPortraitY) / (screenSize.y - buttonParams.height)) * (screenSize.x - buttonParams.height));
                        thickness = buttonParams.width;
                        length = buttonParams.height;
                    } else {
                        x = 0;
                        y = (int) Math.ceil(((double) (buttonPortraitX) / (screenSize.x - buttonParams.width)) * (screenSize.y - buttonParams.width));
                        thickness = buttonParams.height;
                        length = buttonParams.width;
                    }
                    break;
                case Configuration.ORIENTATION_LANDSCAPE:
                    if (position == BUTTON_POSITION_BOTTOM) {
                        y = 0;
                        x = (int) Math.ceil(((double) (buttonLandscapeY) / (screenSize.y - buttonParams.height)) * (screenSize.x - buttonParams.height));
                        thickness = buttonParams.width;
                        length = buttonParams.height;
                    } else {
                        x = 0;
                        y = (int) Math.ceil(((double) (buttonLandscapeX) / (screenSize.x - buttonParams.width)) * (screenSize.y - buttonParams.width));
                        thickness = buttonParams.height;
                        length = buttonParams.width;
                    }
                    break;
            }
        }
        updateButtonLayoutParams(position, x, y, length, thickness);
        calculateButtonCoordinates();
        updateIconBarPosition(position, previousPosition);
        recycleViews();
        service.saveWindowPositions();
    }

    public void avoidKeyboard(boolean value) {
        this.avoidKeyboard = value;
        buttonParams.flags = changeFlagsToAvoidKeyboard(buttonParams.flags, this.avoidKeyboard);
        recycleViews();
    }

    private void calculateButtonCoordinates() {
        switch (screenOrientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                buttonPortraitX = buttonParams.x;
                buttonPortraitY = buttonParams.y;
                buttonLandscapeX = (int) Math.ceil(((double) (buttonPortraitX) / (screenSize.x - buttonParams.width)) * (screenSize.y - buttonParams.width));
                buttonLandscapeY = (int) Math.ceil(((double) (buttonPortraitY) / (screenSize.y - buttonParams.height)) * (screenSize.x - buttonParams.height));
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                buttonLandscapeX = buttonParams.x;
                buttonLandscapeY = buttonParams.y;
                buttonPortraitX = (int) Math.ceil(((double) (buttonLandscapeX) / (screenSize.x - buttonParams.width)) * (screenSize.y - buttonParams.width));
                buttonPortraitY = (int) Math.ceil(((double) (buttonLandscapeY) / (screenSize.y - buttonParams.height)) * (screenSize.x - buttonParams.height));
                break;
        }
    }

    private void updateButtonLayoutParams(int position, int x, int y, int length, int thickness) {
        int gravity = 0;
        switch (position) {
            case BUTTON_POSITION_BOTTOM:
                gravity = Gravity.BOTTOM | Gravity.LEFT;
                break;
            case BUTTON_POSITION_RIGHT:
                gravity = Gravity.TOP | Gravity.RIGHT;
                break;
            case BUTTON_POSITION_LEFT:
                gravity = Gravity.TOP | Gravity.LEFT;
                break;
        }
        if (buttonParams == null) {
            buttonParams = new WindowManager.LayoutParams(
                    getTypeView(), // TYPE_APPLICATION_OVERLAY ---- TYPE_SYSTEM_OVERLAY
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,// | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT);
            buttonParams.flags = changeFlagsToAvoidKeyboard(this.buttonParams.flags, this.avoidKeyboard);
        }
        buttonParams.gravity = gravity;
        buttonParams.x = x;
        buttonParams.y = y;
        if (buttonPosition == BUTTON_POSITION_BOTTOM) {
            buttonParams.width = length;
            buttonParams.height = thickness;
        } else {
            buttonParams.width = thickness;
            buttonParams.height = length;
        }
    }


    private void updateIconBarPosition(int position, int previousPosition) {
        iconBarParams.gravity = buttonParams.gravity;
        if (position == BUTTON_POSITION_BOTTOM || previousPosition == BUTTON_POSITION_BOTTOM) {
            iconBarParams.y = screenSize.y - iconBarParams.height - iconBarParams.y - statusBarHeight;
        }
        if (position == BUTTON_POSITION_RIGHT || previousPosition == BUTTON_POSITION_RIGHT) {
            iconBarParams.x = screenSize.x - iconBarParams.width - iconBarParams.x;
        }

    }

    private int getTypeView() {
        if (Build.VERSION.SDK_INT >= 26) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            return WindowManager.LayoutParams.TYPE_PHONE;
        }

    }

    private int changeFlagsToAvoidKeyboard(int flags, boolean avoidKeyboard) {
        int flag = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        if (avoidKeyboard) {
            return flags | flag;
        } else {
            return flags & ~flag;
        }
    }

}
