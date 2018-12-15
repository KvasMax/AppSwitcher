package com.erros.kvasmax.switcher;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by user on 07.09.2017.
 */

public class ViewManipulator {

    // layouts
    static final int VERTICAL = 0;
    static final int HORIZONTAL = 1;
    private final int BUTTON_POSITION_BOTTOM = 0;
    private final int BUTTON_POSITION_RIGHT = 1;
    private final int BUTTON_POSITION_LEFT = 2;
    private final int APP_ANIMATION_SLIDE = 0;
    private final int APP_ANIMATION_SLIDE_ANOTHER = 1;
    private final int APP_ANIMATION_EMERGE = 2;


    private final WindowManager windowManager;
    int distanceX;
    int distanceY;
    private final Context context;
    private final ISwitcherService service;
    private boolean iconBarHaveToBeVisible = true;
    // touch button
    private CustomImageView buttonView;
    private WindowManager.LayoutParams buttonLayoutParams;
    // iconViews
    private CustomLinearLayout iconBar;
    private ImageView[] iconViews;
    private ScaleAnimation[] hideIconViewAnimations;
    private ScaleAnimation[] showIconViewAnimations;
    private View[] backViews;
    private ScaleAnimation[] hideBackViewAnimations;
    private ScaleAnimation[] showBackViewAnimations;
    private WindowManager.LayoutParams iconBarParams;
    private Animation[] entryAnimations;
    private Animation[] exitAnimations;
    private Point screenSize = new Point();
    private Point fullScreenSize = new Point();
    private int pointCount;
    private int maxCount;
    // icon
    private int iconSize;
    private int iconSizeInc;

    private int iconSizeMin;
    private int perIcon;
    private int incrementLength;
    private int incrementThickness;
    private boolean iconOrderIsDirect;
    private int iconBarLayout;
    private int iconAnim;
    private int iconBarBackgroundColor;
    private boolean isDraggableFloatingButton = false;
    private boolean isDraggableIconBar = false;
    private boolean avoidKeyboard = false;
    private boolean useDarkeningBackground = false;
    private int screenOrientation;
    private int buttonPosition;
    private int buttonPortraitX;
    private int buttonPortraitY;
    private int buttonLandscapeX;
    private int buttonLandscapeY;
    private int buttonColor = Color.TRANSPARENT;
    private int statusBarHeight = 0;

    private int animationDuration = 100;
    private float backlightMaxSize = 1f;
    private float backlightMinSize = 0.5f;

    private float iconMaxSize = 1f;
    private float iconMinSize = 0.75f;


    public ViewManipulator(Context context, ISwitcherService service, WindowManager windowManager, int maxCount,
                           int pointCount, int iconBarLayout, boolean iconOrderIsDirect, int buttonPosition, int buttonThickness, int buttonLength,
                           int buttonPortraitX, int buttonPortraitY, int buttonLandscapeX, int buttonLandscapeY,
                           int iconSize, int distanceX, int distanceY, int iconAnim, int screenOrientation, int buttonColor,
                           boolean avoidKeyboard, boolean useDarkeningBackground, int iconBarBackgroundColor) {

        this.context = context;
        this.service = service;
        this.windowManager = windowManager;

        updateScreenSize();

        this.pointCount = pointCount;

        calculateIncs();

        this.maxCount = maxCount;
        this.iconOrderIsDirect = iconOrderIsDirect;
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
        this.useDarkeningBackground = useDarkeningBackground;
        this.iconBarBackgroundColor = iconBarBackgroundColor;
        this.iconSize = calculateIconSize(iconSize);

        setAnimation(iconAnim);

        calculateStatusBarHeight();
        initialiseButton(buttonLength, buttonThickness);
        initialiseIconBar();

        addViews();

    }

    public ViewManipulator(Context context, ISwitcherService service, WindowManager windowManager, int maxCount, int pointCount,
                           int screenOrientation, int iconBarLayout, boolean iconOrderIsDirect, int iconAnim, int buttonPosition, int buttonThickness,
                           int buttonLength, int buttonColor, int iconSize, boolean avoidKeyboard, boolean useDarkeningBackground, int iconBarBackgroundColor) {

        this.context = context;
        this.service = service;
        this.windowManager = windowManager;

        updateScreenSize();

        this.pointCount = pointCount;

        calculateIncs();

        this.maxCount = maxCount;
        this.avoidKeyboard = avoidKeyboard;
        this.buttonColor = buttonColor;
        this.iconOrderIsDirect = iconOrderIsDirect;
        this.iconBarLayout = iconBarLayout;
        this.buttonPosition = buttonPosition;
        this.screenOrientation = screenOrientation;
        this.useDarkeningBackground = useDarkeningBackground;
        this.iconBarBackgroundColor = iconBarBackgroundColor;
        this.iconSize = calculateIconSize(iconSize);


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
        initialiseIconBar();
        calculateButtonCoordinates();

        addViews();
    }

    private void addViews() {
        windowManager.addView(buttonView, buttonLayoutParams);
        windowManager.addView(iconBar, iconBarParams);
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

    private void initialiseIconBar() {

        iconViews = new ImageView[maxCount];
        backViews = new View[maxCount];

        hideBackViewAnimations = new ScaleAnimation[maxCount];
        showBackViewAnimations = new ScaleAnimation[maxCount];
        showIconViewAnimations = new ScaleAnimation[maxCount];
        hideIconViewAnimations = new ScaleAnimation[maxCount];

        if (iconBar != null) {
            iconBar.removeAllViews();
        } else {
            iconBar = new CustomLinearLayout(context);
            iconBar.setBackgroundResource(R.drawable.rounded_corners);
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
                                view.performClick();
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
                                int newY = buttonPosition != BUTTON_POSITION_BOTTOM
                                        ? initialY - (int) (initialTouchY - event.getRawY()) : initialY + (int) (initialTouchY - event.getRawY());
                                if (newX >= 0 && screenSize.x >= newX + iconBarParams.width) {
                                    iconBarParams.x = newX;
                                }
                                if (newY >= 0 && screenSize.y >= newY + iconBarParams.height) {
                                    iconBarParams.y = newY;
                                }
                                updateIconBarLayout();
                                break;
                        }
                    }
                    return true;
                }
            });
        }

        iconBar.setOrientation(iconBarLayout == VERTICAL ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        changeIconBarVisibility(iconBarHaveToBeVisible);

        GradientDrawable drawable = (GradientDrawable) iconBar.getBackground();
        drawable.setColor(iconBarBackgroundColor);
        Rect paddings = new Rect();
        drawable.getPadding(paddings);
        int width = 0, height = 0;

        int padding = paddings.top;
        int iconActualSize = iconSize + padding * 2;

        switch (iconBar.getOrientation()) {
            case LinearLayout.HORIZONTAL:
                width = iconActualSize * maxCount + padding * 2;
                height = iconActualSize + 2 * padding;
                perIcon = width / maxCount + 1;
                break;
            case LinearLayout.VERTICAL:
                height = iconActualSize * maxCount + padding * 2;
                width = iconActualSize + 2 * padding;
                perIcon = height / maxCount + 1;
                break;
        }
        int appX = buttonLayoutParams.x - distanceX,
                appY = buttonLayoutParams.y - distanceY;

        iconBarParams = getLayoutParams(appX, appY, width, height);
        if (!iconBarHaveToBeVisible) {
            iconBarParams.flags = changeFlagsToUseDarkeningBehind(iconBarParams.flags, useDarkeningBackground);
        }
        iconBarParams.gravity = buttonLayoutParams.gravity;

        FrameLayout[] containers = new FrameLayout[maxCount];

        for (int index = 0; index < maxCount; index++) {

            ImageView image = new ImageView(context);
            iconViews[index] = image;
            image.setPadding(padding, padding, padding, padding);
            showIconViewAnimations[index] = createShowIconViewAnimation();
            hideIconViewAnimations[index] = createHideIconViewAnimation();

            View background = new View(context);
            backViews[index] = background;
            background.setVisibility(View.GONE);
            background.setBackgroundResource(R.drawable.circle);
            hideBackViewAnimations[index] = createHideBackViewAnimation(background);
            showBackViewAnimations[index] = createShowBackViewAnimation();

            FrameLayout container = new FrameLayout(context);
            containers[index] = container;
            container.addView(background, new FrameLayout.LayoutParams(iconActualSize, iconActualSize));
            container.addView(image, new LinearLayout.LayoutParams(iconActualSize, iconActualSize));
        }

        for (FrameLayout container : containers) {
            iconBar.addView(container, new LinearLayout.LayoutParams(iconActualSize, iconActualSize));
        }

        updateIconsAnimations();

    }

    private void calculateDistance() {
        distanceX = buttonLayoutParams.x - iconBarParams.x;
        distanceY = buttonLayoutParams.y - iconBarParams.y;
    }

    private void initialiseButton(int length, int thickness) {

        buttonView = new CustomImageView(context);
        buttonView.setBackgroundResource(R.drawable.rounded_corners);
        GradientDrawable drawable = (GradientDrawable) buttonView.getBackground();
        drawable.setColor(buttonColor);

        buttonView.setOnTouchListener(new View.OnTouchListener() {

            private int initialX;
            private int initialY;
            private float touchX;
            private float touchY;
            private boolean iconsAreUpToDate = false;
            private int checkedIconIndex = -1;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int[] location = new int[2];
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        iconsAreUpToDate = false;
                        initialX = buttonLayoutParams.x;
                        initialY = buttonLayoutParams.y;
                        touchX = event.getRawX();
                        touchY = event.getRawY();
                        if (!isDraggableFloatingButton) {
                            //TODO show background
                        }
                        v.performClick();
                        break;
                    case MotionEvent.ACTION_UP:
                        iconsAreUpToDate = false;
                        if (!isDraggableFloatingButton) {
                            if (iconBar.getVisibility() == View.VISIBLE) {
                                touchX = event.getRawX();
                                touchY = event.getRawY();
                                final int width = iconBar.getWidth();
                                final int height = iconBar.getHeight();
                                checkedIconIndex = -1;
                                iconBar.getLocationOnScreen(location);
                                if (isInsideView(location[0], location[1], width, height, touchX, touchY)) {

                                    int iconIndex = selectedIcon(location[0], location[1], touchX, touchY);
                                    if (iconOrderIsDirect) iconIndex = maxCount - iconIndex - 1;
                                    service.onTapIconWithIndex(iconIndex);

                                }
                                for (int i = 0; i < maxCount; i++) {
                                    final ImageView im = iconViews[i];
                                    im.clearAnimation();
                                    im.startAnimation(exitAnimations[i]);
                                }
                                for (View back : backViews) {
                                    back.clearAnimation();
                                    back.setVisibility(View.INVISIBLE);
                                }

                            }

                            //TODO show background

                        } else {

                            validateAndFixButtonPosition();
                            calculateButtonCoordinates();
                            service.saveWindowPositions();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:

                        if (!isDraggableFloatingButton) {
                            touchX = event.getRawX();
                            touchY = event.getRawY();
                            buttonView.getLocationOnScreen(location);

                            if (!iconsAreUpToDate
                                    && !isInsideView(location[0],
                                    location[1],
                                    buttonLayoutParams.width,
                                    buttonLayoutParams.height,
                                    touchX,
                                    touchY)) {

                                if (iconBar.getVisibility() == View.GONE) {

                                    iconsAreUpToDate = true;

                                    service.updateIcons();

                                    iconBar.clearAnimation();
                                    iconBar.setAlpha(0.0f);
                                    changeIconBarVisibility(true);

                                    iconBar.animate().alpha(1.0f).setDuration(100).setListener(new Animator.AnimatorListener() {

                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            for (int i = 0; i < maxCount; i++) {
                                                final ImageView im = iconViews[i];
                                                im.clearAnimation();
                                                im.startAnimation(entryAnimations[i]);
                                            }
                                        }

                                        @Override
                                        public void onAnimationStart(Animator animation) {
                                        }

                                        @Override
                                        public void onAnimationCancel(Animator animation) {
                                        }

                                        @Override
                                        public void onAnimationRepeat(Animator animation) {
                                        }

                                    }).start();


                                } else if (iconBarHaveToBeVisible
                                        && iconBar.getVisibility() == View.VISIBLE) {

                                    iconsAreUpToDate = true;

                                    service.updateIcons();

                                    for (int i = 0; i < maxCount; i++) {
                                        final ImageView im = iconViews[i];
                                        im.clearAnimation();
                                        im.startAnimation(entryAnimations[i]);
                                    }

                                }
                            }

                            if (iconBar.getVisibility() == View.VISIBLE) {

                                iconBar.getLocationOnScreen(location);
                                final int width = iconBarParams.width;
                                final int height = iconBarParams.height;
                                if (isInsideView(location[0], location[1], width, height, touchX, touchY)) {
                                    final int iconIndex = selectedIcon(location[0], location[1], touchX, touchY);
                                    if (iconIndex != checkedIconIndex) {
                                        showBacklightViewForIndex(iconIndex);
                                        hideIconViewForIndex(iconIndex);
                                        if (checkedIconIndex >= 0) {
                                            hideBacklightViewForIndex(checkedIconIndex);
                                            showIconViewForIndex(checkedIconIndex);
                                        }
                                        checkedIconIndex = iconIndex;
                                    }
                                } else {
                                    if (checkedIconIndex >= 0) {
                                        hideBacklightViewForIndex(checkedIconIndex);
                                        showIconViewForIndex(checkedIconIndex);
                                        checkedIconIndex = -1;
                                    }
                                }
                            }

                        } else {

                            final int newX = initialX - (int) (touchX - event.getRawX());
                            final int newY = initialY - (int) (touchY - event.getRawY());
                            if (buttonPosition == BUTTON_POSITION_BOTTOM
                                    && newX >= 0
                                    && screenSize.x >= newX + buttonLayoutParams.width) {

                                buttonLayoutParams.x = newX;
                                buttonLayoutParams.y = 0;

                            }
                            if (buttonPosition != BUTTON_POSITION_BOTTOM
                                    && newY >= 0
                                    && screenSize.y >= newY + buttonLayoutParams.height) {

                                buttonLayoutParams.y = newY;
                                buttonLayoutParams.x = 0;

                            }
                            updateButtonLayout();
                        }
                        break;
                    case MotionEvent.ACTION_OUTSIDE:
                        //TODO handle touches outside
                        break;
                }
                return true;
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

    }

    private ScaleAnimation createHideIconViewAnimation() {
        ScaleAnimation animation = new ScaleAnimation(iconMaxSize, iconMinSize, iconMaxSize, iconMinSize,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(animationDuration);
        animation.setFillAfter(true);
        animation.setFillBefore(true);
        return animation;
    }

    private ScaleAnimation createShowIconViewAnimation() {
        ScaleAnimation animation = new ScaleAnimation(iconMinSize, iconMaxSize, iconMinSize, iconMaxSize,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(animationDuration);
        animation.setFillAfter(true);
        animation.setFillBefore(true);
        return animation;
    }

    private ScaleAnimation createHideBackViewAnimation(@NonNull final View view) {
        ScaleAnimation animation = new ScaleAnimation(backlightMaxSize, backlightMinSize, backlightMaxSize, backlightMinSize,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(animationDuration);
        animation.setFillAfter(true);
        animation.setFillBefore(false);
        animation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        return animation;
    }

    private ScaleAnimation createShowBackViewAnimation() {
        ScaleAnimation animation = new ScaleAnimation(backlightMinSize, backlightMaxSize, backlightMinSize, backlightMaxSize,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(animationDuration);
        animation.setFillAfter(true);
        animation.setFillBefore(true);
        return animation;
    }

    private void hideIconViewForIndex(final int index) {
        View view = iconViews[index];
        view.clearAnimation();
        view.startAnimation(hideIconViewAnimations[index]);
    }

    private void showIconViewForIndex(final int index) {
        View view = iconViews[index];
        view.clearAnimation();
        view.startAnimation(showIconViewAnimations[index]);
    }

    private void showBacklightViewForIndex(final int index) {
        View view = backViews[index];
        view.setVisibility(View.VISIBLE);
        view.clearAnimation();
        view.startAnimation(showBackViewAnimations[index]);
    }

    private void hideBacklightViewForIndex(final int index) {
        View view = backViews[index];
        view.clearAnimation();
        view.startAnimation(hideBackViewAnimations[index]);
    }

    private boolean isInsideView(int x, int y, int width, int height, float touchX, float touchY) {
        return touchX > x
                && touchX < x + width
                && touchY > y
                && touchY < y + height;
    }

    private int selectedIcon(int x, int y, float touchX, float touchY) {
        switch (iconBarLayout) {
            case VERTICAL:
            default:
                return (int) ((touchY - y) / perIcon);
            case HORIZONTAL:
                return (int) ((touchX - x) / perIcon);
        }
    }

    private void validateAndFixButtonPosition() {
        if (buttonLayoutParams.x < screenSize.x * 0.01f) {
            buttonLayoutParams.x = 0;
        }
        if (screenSize.x - (buttonLayoutParams.x + buttonLayoutParams.width) < screenSize.x * 0.01f) {
            buttonLayoutParams.x = screenSize.x - buttonLayoutParams.width;
        }
        if (buttonLayoutParams.y < screenSize.y * 0.01f) {
            buttonLayoutParams.y = 0;
        }
        if (screenSize.y - (buttonLayoutParams.y + buttonLayoutParams.height) < screenSize.y * 0.01f) {
            buttonLayoutParams.y = screenSize.y - buttonLayoutParams.height;
        }
    }

    private void updateButtonLayout() {
        windowManager.updateViewLayout(buttonView, buttonLayoutParams);
    }

    private void updateIconBarLayout() {
        windowManager.updateViewLayout(iconBar, iconBarParams);
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
        changeThickness(buttonLayoutParams, value);
        updateButtonLayout();
    }

    public void changeButtonLength(int value) {
        changeLength(buttonLayoutParams, value);
        updateButtonLayout();
    }

    public void changeIconOrder(boolean iconOrderIsDirect) {
        this.iconOrderIsDirect = iconOrderIsDirect;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
        initialiseIconBar();
        updateIconBarLayout();
    }

    public void changeIconSize(int value) {
        this.iconSize = calculateIconSize(value);
        initialiseIconBar();
        updateIconBarLayout();
    }

    public void setIconViews(Drawable[] icons) {
        int i = iconOrderIsDirect ? maxCount - 1 : 0;
        for (ImageView im : iconViews) {
            if (icons.length > i) {
                im.setImageDrawable(icons[i]);
            } else {
                im.setImageDrawable(null);
            }
            if (iconOrderIsDirect) --i;
            else ++i;
        }
    }

    public void setAnimation(int animation) {
        this.iconAnim = animation;

        updateIconsAnimations();

    }

    private void updateIconsAnimations() {

        int entryAnimationRes = 0;
        int exitAnimationRes = 0;

        switch (iconBarLayout) {
            case VERTICAL:
                switch (iconAnim) {
                    case APP_ANIMATION_SLIDE:
                        entryAnimationRes = R.anim.slide_from_left;
                        exitAnimationRes = R.anim.slide_to_left;
                        break;
                    case APP_ANIMATION_SLIDE_ANOTHER:
                        entryAnimationRes = R.anim.slide_from_right;
                        exitAnimationRes = R.anim.slide_to_right;
                        break;
                    case APP_ANIMATION_EMERGE:
                        entryAnimationRes = R.anim.emerge;
                        exitAnimationRes = R.anim.plunge;
                        break;
                }
                break;
            case HORIZONTAL:
                switch (iconAnim) {
                    case APP_ANIMATION_SLIDE:
                        entryAnimationRes = R.anim.slide_from_bottom;
                        exitAnimationRes = R.anim.slide_to_bottom;
                        break;
                    case APP_ANIMATION_SLIDE_ANOTHER:
                        entryAnimationRes = R.anim.slide_from_top;
                        exitAnimationRes = R.anim.slide_to_top;
                        break;
                    case APP_ANIMATION_EMERGE:
                        entryAnimationRes = R.anim.emerge;
                        exitAnimationRes = R.anim.plunge;
                        break;
                }
                break;
        }

        if (entryAnimationRes == 0
                || exitAnimationRes == 0) {
            return;
        }

        this.entryAnimations = new Animation[maxCount];
        this.exitAnimations = new Animation[maxCount];

        for (int i = 0; i < maxCount; i++) {
            entryAnimations[i] = AnimationUtils.loadAnimation(context, entryAnimationRes);
            exitAnimations[i] = AnimationUtils.loadAnimation(context, exitAnimationRes);
        }

        for (Animation anim : exitAnimations) {
            anim.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (!iconBarHaveToBeVisible) {
                        changeIconBarVisibility(false);
                    }
                    for (ImageView im : iconViews) {
                        im.setImageDrawable(null);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationStart(Animation animation) {
                }
            });
        }
    }

    public void forceIconBarToBeVisible() {
        changeIconBarVisibility(true);
        iconBarHaveToBeVisible = true;
        iconBarParams.flags = changeFlagsToUseDarkeningBehind(iconBarParams.flags, false);
        updateIconBarLayout();
    }

    public void allowIconBarToBeHidden() {
        changeIconBarVisibility(false);
        iconBarHaveToBeVisible = false;
        iconBarParams.flags = changeFlagsToUseDarkeningBehind(iconBarParams.flags, useDarkeningBackground);
        updateIconBarLayout();
    }

    private void changeIconBarVisibility(boolean visible) {
        if (visible) {
            iconBar.setVisibility(View.VISIBLE);
        } else {
            iconBar.setVisibility(View.GONE);
        }
    }

    public void changeIconBarLayout(int layout) {
        iconBarLayout = layout;
        initialiseIconBar();
        switch (buttonPosition) {
            case BUTTON_POSITION_BOTTOM:
                iconBarParams.x = 0;
                iconBarParams.y = buttonLayoutParams.height;
                break;
            case BUTTON_POSITION_RIGHT:
            case BUTTON_POSITION_LEFT:
                iconBarParams.x = buttonLayoutParams.width;
                iconBarParams.y = 0;
                break;
        }
        calculateDistance();
        service.saveWindowPositions();
        updateIconBarLayout();
    }

    public void removeViews() {
        windowManager.removeView(iconBar);
        windowManager.removeView(buttonView);
    }

    private void updateScreenSize() {
        windowManager.getDefaultDisplay().getSize(screenSize);
        windowManager.getDefaultDisplay().getRealSize(fullScreenSize);
    }

    private WindowManager.LayoutParams getLayoutParams(int x, int y, int width, int height) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                getViewType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // | WindowManager.LayoutParams.FLAG_DIM_BEHIND,// | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSPARENT);
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        layoutParams.x = x;
        layoutParams.y = y;
        layoutParams.width = width;
        layoutParams.height = height;
        layoutParams.dimAmount = 0.5f;
        return layoutParams;
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
        calculateIncs();
        screenOrientation = orientation;
        switch (screenOrientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                buttonLayoutParams.x = buttonPortraitX;
                buttonLayoutParams.y = buttonPortraitY;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                buttonLayoutParams.x = buttonLandscapeX;
                buttonLayoutParams.y = buttonLandscapeY;
                break;
        }
        iconBarParams.x = buttonLayoutParams.x - distanceX;
        iconBarParams.y = buttonLayoutParams.y - distanceY;

        updateButtonLayout();
        updateIconBarLayout();

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
            x = buttonLayoutParams.x;
            y = buttonLayoutParams.y;
            length = buttonLayoutParams.height;
            thickness = buttonLayoutParams.width;
        } else {
            switch (screenOrientation) {
                case Configuration.ORIENTATION_PORTRAIT:
                    if (position == BUTTON_POSITION_BOTTOM) {
                        y = 0;
                        x = (int) Math.ceil(((double) (buttonPortraitY) / (screenSize.y - buttonLayoutParams.height)) * (screenSize.x - buttonLayoutParams.height));
                        thickness = buttonLayoutParams.width;
                        length = buttonLayoutParams.height;
                    } else {
                        x = 0;
                        y = (int) Math.ceil(((double) (buttonPortraitX) / (screenSize.x - buttonLayoutParams.width)) * (screenSize.y - buttonLayoutParams.width));
                        thickness = buttonLayoutParams.height;
                        length = buttonLayoutParams.width;
                    }
                    break;
                case Configuration.ORIENTATION_LANDSCAPE:
                    if (position == BUTTON_POSITION_BOTTOM) {
                        y = 0;
                        x = (int) Math.ceil(((double) (buttonLandscapeY) / (screenSize.y - buttonLayoutParams.height)) * (screenSize.x - buttonLayoutParams.height));
                        thickness = buttonLayoutParams.width;
                        length = buttonLayoutParams.height;
                    } else {
                        x = 0;
                        y = (int) Math.ceil(((double) (buttonLandscapeX) / (screenSize.x - buttonLayoutParams.width)) * (screenSize.y - buttonLayoutParams.width));
                        thickness = buttonLayoutParams.height;
                        length = buttonLayoutParams.width;
                    }
                    break;
            }
        }
        updateButtonLayoutParams(position, x, y, length, thickness);
        calculateButtonCoordinates();
        updateIconBarPosition(position, previousPosition);

        updateButtonLayout();
        updateIconBarLayout();

        service.saveWindowPositions();
    }

    public void avoidKeyboard(boolean value) {
        avoidKeyboard = value;
        buttonLayoutParams.flags = changeFlagsToAvoidKeyboard(buttonLayoutParams.flags, avoidKeyboard);
    }

    private void calculateButtonCoordinates() {
        switch (screenOrientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                buttonPortraitX = buttonLayoutParams.x;
                buttonPortraitY = buttonLayoutParams.y;
                buttonLandscapeX = (int) Math.ceil(((double) (buttonPortraitX) / (screenSize.x - buttonLayoutParams.width)) * (screenSize.y - buttonLayoutParams.width));
                buttonLandscapeY = (int) Math.ceil(((double) (buttonPortraitY) / (screenSize.y - buttonLayoutParams.height)) * (screenSize.x - buttonLayoutParams.height));
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                buttonLandscapeX = buttonLayoutParams.x;
                buttonLandscapeY = buttonLayoutParams.y;
                buttonPortraitX = (int) Math.ceil(((double) (buttonLandscapeX) / (screenSize.x - buttonLayoutParams.width)) * (screenSize.y - buttonLayoutParams.width));
                buttonPortraitY = (int) Math.ceil(((double) (buttonLandscapeY) / (screenSize.y - buttonLayoutParams.height)) * (screenSize.x - buttonLayoutParams.height));
                break;
        }
    }

    @SuppressLint("RtlHardcoded")
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
        if (buttonLayoutParams == null) {
            buttonLayoutParams = new WindowManager.LayoutParams(
                    getViewType(),
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,// | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSPARENT);
            // disable animations
            try {
                int currentFlags = (Integer) buttonLayoutParams.getClass().getField("privateFlags").get(buttonLayoutParams);
                buttonLayoutParams.getClass().getField("privateFlags").set(buttonLayoutParams, currentFlags | 0x00000040);
            } catch (Exception e) {
                // shit happens
            }
        }
        buttonLayoutParams.flags = changeFlagsToAvoidKeyboard(buttonLayoutParams.flags, avoidKeyboard);
        buttonLayoutParams.gravity = gravity;
        buttonLayoutParams.x = x;
        buttonLayoutParams.y = y;
        if (buttonPosition == BUTTON_POSITION_BOTTOM) {
            buttonLayoutParams.width = length;
            buttonLayoutParams.height = thickness;
        } else {
            buttonLayoutParams.width = thickness;
            buttonLayoutParams.height = length;
        }
    }


    private void updateIconBarPosition(int position, int previousPosition) {
        iconBarParams.gravity = buttonLayoutParams.gravity;
        if (position == BUTTON_POSITION_BOTTOM || previousPosition == BUTTON_POSITION_BOTTOM) {
            iconBarParams.y = screenSize.y - iconBarParams.height - iconBarParams.y - statusBarHeight;
        }
        if (position == BUTTON_POSITION_RIGHT || previousPosition == BUTTON_POSITION_RIGHT) {
            iconBarParams.x = screenSize.x - iconBarParams.width - iconBarParams.x;
        }

    }

    private int getViewType() {
        if (Build.VERSION.SDK_INT >= 26) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            return WindowManager.LayoutParams.TYPE_PHONE;
        }
    }

    public void useDarkeningBackground(boolean enabled) {
        useDarkeningBackground = enabled;
        if (!iconBarHaveToBeVisible) {
            iconBarParams.flags = changeFlagsToUseDarkeningBehind(iconBarParams.flags, enabled);
            updateIconBarLayout();
        }
    }

    public void changeIconBarBackgroundColor(int color) {
        iconBarBackgroundColor = color;
        GradientDrawable drawable = (GradientDrawable) iconBar.getBackground();
        drawable.setColor(iconBarBackgroundColor);
    }

    private int changeFlagsToAvoidKeyboard(int flags, boolean avoidKeyboard) {
        int flag = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        if (avoidKeyboard) {
            return flags | flag;
        } else {
            return flags & ~flag;
        }
    }

    private int changeFlagsToUseDarkeningBehind(int flags, boolean enabled) {
        int flag = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        if (enabled) {
            return flags | flag;
        } else {
            return flags & ~flag;
        }
    }

    private static class CustomImageView extends AppCompatImageView {

        public CustomImageView(Context context) {
            super(context);
        }

        public CustomImageView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public CustomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        public boolean performClick() {
            return super.performClick();
        }
    }

    private static class CustomLinearLayout extends LinearLayout {

        public CustomLinearLayout(Context context) {
            super(context);
        }

        public CustomLinearLayout(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public CustomLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        public CustomLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        @Override
        public boolean performClick() {
            return super.performClick();
        }
    }

}
