package com.treem.treem.activities.main;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.treem.treem.R;

/**
 * Settings toolbar views handler
 */
public class OtherToolbar {
    /**
     * Interface for back button listener
     */
    public interface OnBackClickListener{
        //On toolbar back button clicked
        void onBackClick();
    }
    //Back button
    private ImageButton btnBack;
    //Title
    private TextView textTitle;

    /**
     * Create instance of toolbar handler and init views from layout
     * @param layout parent toolbar layout
     */
    public OtherToolbar(ViewGroup layout) {
        btnBack = (ImageButton)layout.findViewById(R.id.toolbar_home);
        textTitle = (TextView)layout.findViewById(R.id.toolbar_title);
    }

    /**
     * Set toolbar title
     * @param titleId title string id
     */
    public void setTitle(int titleId){
        if (textTitle==null)
            return;
        if (titleId==0)
            textTitle.setText("");
        else
            textTitle.setText(titleId);
    }

    /**
     * Show or hide back button
     * @param visible is back button visible
     */
    public void setBackVisible(boolean visible){
        if (btnBack==null)
            return;
        btnBack.setVisibility(visible? View.VISIBLE: View.INVISIBLE); //show or hide the button
    }

    /**
     * Set back button click listener
     * @param listener click listener
     */
    public void setBackListener(final OnBackClickListener listener) {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener!=null)
                    listener.onBackClick();
            }
        });
    }
}
