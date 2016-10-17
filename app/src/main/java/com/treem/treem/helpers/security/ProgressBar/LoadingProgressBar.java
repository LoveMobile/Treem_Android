package com.treem.treem.helpers.security.ProgressBar;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.treem.treem.R;

/**
 * Created by Matthew Walker on 2/26/16.
 * Copyright (c) 2016 Treem LLC. All rights reserved.
 */
public class LoadingProgressBar {
    View progressBarOverlay;
    ViewGroup viewGroupContainer;
    private int color;

    public LoadingProgressBar(ViewGroup viewGroupContainer) {
        this.viewGroupContainer = viewGroupContainer;
        this.color = Color.WHITE;
    }
    public LoadingProgressBar(ViewGroup viewGroupContainer,int color) {
        this.viewGroupContainer = viewGroupContainer;
        this.color = color;
    }

    public boolean isShown(){
        return progressBarOverlay!=null;
    }
    // show/hide loading progress bar
    public void toggleProgressBar(boolean show) {
        if (viewGroupContainer != null) {
            if (show) {
                if (isShown())
                    return;
                // get the layout inflater to inflate the layout
                LayoutInflater inflater = (LayoutInflater) viewGroupContainer.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                // add progress bar to the layout
                progressBarOverlay = inflater.inflate(R.layout.progress_bar_overlay, this.viewGroupContainer,false);
                progressBarOverlay.findViewById(R.id.progress_bar_overlay).setBackgroundColor(color);

                viewGroupContainer.addView(progressBarOverlay);
            } else if (progressBarOverlay != null) {
                // remove from layout
                viewGroupContainer.removeView(progressBarOverlay);

                progressBarOverlay = null;
            }
        }
    }
}
