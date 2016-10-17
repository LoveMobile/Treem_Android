package com.treem.treem.widget;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.treem.treem.R;
import com.treem.treem.activities.main.DispatchTouchEventListener;

/**
 * Base class for bottom view panel
 */
public class Panel extends FrameLayout implements DispatchTouchEventListener.OnDispatchTouchListener {
    private static final String TAG = Panel.class.getSimpleName();

    //Reference to activity that support dispatch events to child views
    private DispatchTouchEventListener dispatchListener;

    //Reference to parent fragment to handle show/view panel
    private OnVisibilityChangeListener visibilityListener;

    /**
     * Hide panel
     */
    public void hide() {
        if (getVisibility()!=GONE)
            setVisibility(GONE);
    }

    /**
     * Show panel
     */
    public void show() {
        if (getVisibility()!=VISIBLE)
            setVisibility(VISIBLE);
    }

    /**
     * Panel visibility change listener
     */
    public interface OnVisibilityChangeListener{
        /**
         * Panel show/hide listener
         * @param isShown is panel show or hide
         */
        void onPanelVisibilityChanged(boolean isShown);
    }

    public Panel(Context context) {
        this(context,null);
    }

    public Panel(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public Panel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Set panel visibility
     * @param visibility VISIBLE,GONE
     */
    @Override
    public void setVisibility(int visibility) {
        if (visibility==VISIBLE) { //on panel show
            //start animation
            Animation bottomUp = AnimationUtils.loadAnimation(getContext(),
                    R.anim.slide_up);
            startAnimation(bottomUp);
            super.setVisibility(VISIBLE);
            updateListeners(true); //inform parent about panel visible
        }
        else if (visibility==GONE){ //hide panel
            //start animation
            Animation bottomDown = AnimationUtils.loadAnimation(getContext(),
                    R.anim.slide_down);
            bottomDown.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    Panel.super.setVisibility(GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            startAnimation(bottomDown);
            //inform parent about panel hidden
            updateListeners(false);
        }
        else{
            //else do default
            super.setVisibility(visibility);
            updateListeners(false);
        }
    }

    /**
     * Update listeners
     * @param isShown is panel shown or hide
     */
    private void updateListeners(boolean isShown) {
        if (dispatchListener!=null) {
            if (isShown)
                dispatchListener.registerTouchEventListener(this); //subscribe to dispatch events on panel show
            else
                dispatchListener.unregisterTouchEventListener(this); //unsubscribe on hide
        }
        if (visibilityListener!=null){
            visibilityListener.onPanelVisibilityChanged(isShown); //inform parent about show/hide the panel
        }
    }

    /**
     * Set refernce to parent activity to subscribe/unsubscribe touch events
     * @param listener reference to parent activity
     */
    public void setDispatchTouchParent(DispatchTouchEventListener listener){
        dispatchListener = listener;
    }

    /**
     * Set visibility change listener
     * @param listener reference to listener
     */
    public void setVisibilityListener(OnVisibilityChangeListener listener){
        visibilityListener = listener;
    }

    /**
     * Handle parent activity touch events
     * @param event event itself
     * @return true if event handled and do not dispatch to other classes or false otherwise
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isShown()) { //we need to handle touches only if panel shown
            int[] coords = new int[2]; //get screen coords
            getLocationOnScreen(coords);
            RectF rc = new RectF(coords[0],coords[1],coords[0]+getWidth(),coords[1]+getHeight());
            //check is click inside panel
            boolean isClickInPanel = rc.contains(event.getRawX(),event.getRawY());
            if (!isClickInPanel) { //hide panel if not
                setVisibility(GONE);
            }
        }
        return false; //dispatch event to other objects
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (dispatchListener!=null) //unregister on detach
            dispatchListener.unregisterTouchEventListener(this);
        dispatchListener = null;
    }
}
