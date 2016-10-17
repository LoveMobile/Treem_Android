package com.treem.treem.activities.main;

import android.view.MotionEvent;

/**
 * Interface to allow fragments handle touch events
 */
public interface DispatchTouchEventListener {
    /**
     * Listener for touch events
     */
    interface OnDispatchTouchListener{
        /**
         * On touch event
         * @param ev the event
         * @return true if handled
         */
        boolean onTouchEvent(MotionEvent ev);
    }

    /**
     * Register touch event listener
     * @param listener the listener
     */
    void registerTouchEventListener(OnDispatchTouchListener listener);

    /**
     * Unregister touch event listener
     * @param listener the listener
     */
    void unregisterTouchEventListener(OnDispatchTouchListener listener);
}
