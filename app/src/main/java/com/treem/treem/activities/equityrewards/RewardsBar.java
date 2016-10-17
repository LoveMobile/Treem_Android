package com.treem.treem.activities.equityrewards;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.treem.treem.R;

/**
 * Reward color bar
 */
public class RewardsBar extends FrameLayout {
    //Layout with pointer
    private ViewGroup layoutPointer;
    //last pointer value
    private int pointer = 0;

    public RewardsBar(Context context) {
        super(context);
        init(null, 0);
    }

    public RewardsBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public RewardsBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(@SuppressWarnings("UnusedParameters") AttributeSet attrs, @SuppressWarnings("UnusedParameters") int defStyle) {
        // Inflate layout for widget
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_rewards_bar,this,false);
        //get pointer layout
        layoutPointer = (ViewGroup)view.findViewById(R.id.layoutPointer);
        //add inflated view to widget
        addView(view);
    }

    /**
     * Set pointer value
     * @param value in percent
     */
    public void setPointer(double value){
        this.pointer = (int) Math.round(value);
        updatePointer();
    }

    /**
     * Update pointer position
     */
    private void updatePointer() {
        int width = getWidth();
        int padding = getResources().getDimensionPixelOffset(R.dimen.reward_mark_width_half);
        width-=padding*2;
        int position = width*pointer/100;
        layoutPointer.setPadding(position,0,0,0);
    }

    /**
     * Get ponter value
     * @return pointer value
     */
    @SuppressWarnings("unused")
    public int getPointer(){
        return pointer;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        updatePointer();
    }
}
