package com.viewpager.dndp.views;

import android.view.View;

/**
 * primarily used for onTouchListners to detect double tap events
 */
public class DNDDoubleTap {
    static final String TAG ="DNDDoubleTap";

    private long lastTouchTime = 0;
    private long currentTouchTime = 0;
    private int maxDuration;
    private View lastView;


    /**
     * Ensure that the dblClick must occur only to that view,
     * if the user manages to dblClick from one view to another and done before the max_duration limit
     * then check if the recent view clicked is the same as the current view,
     * the last view must be equal to current view clicked to callback.
     * @param view - used to check if the last_view.
     * @param event - callback when double clicked.
     */
    void doubleTap(View view, IDNDPager.ActionEvent event){
        lastTouchTime = currentTouchTime;
        currentTouchTime = System.currentTimeMillis();

        if (currentTouchTime - lastTouchTime < maxDuration) {
            if(lastView == view){
                event.onExecute();
            }
            lastTouchTime = 0;
            currentTouchTime = 0;
        }
        lastView = view;

    }


    public int getMaxDuration() {
        return maxDuration;
    }

    /**
     *
     * @param maxDuration - the max duration between clicks in milliseconds
     */
    public void setMaxDuration(int maxDuration) {
        this.maxDuration = maxDuration;
    }
}
