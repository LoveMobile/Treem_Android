package com.treem.treem.helpers.recyclerview;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Endless recycler scroll listener for grid layout manager
 */
public abstract class EndlessRecyclerViewGridScrollListener extends RecyclerView.OnScrollListener {
    // The minimum amount of items to have below your current scroll position
    // before loading more.
    private static final int visibleRowThreshold = 3;
    private boolean loading = true;

    private boolean isLoadingAllow = false;

    private GridLayoutManager mLayoutManager;

    public EndlessRecyclerViewGridScrollListener(GridLayoutManager layoutManager) {
        this.mLayoutManager = layoutManager;
    }

    // This happens many times a second during a scroll, so be wary of the code you place here.
    // We are given a few useful parameters to help us work out if we need to load some more data,
    // but first we check if we are waiting for the previous load to finish.
    @Override
    public void onScrolled(RecyclerView view, int dx, int dy) {
        int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
        int totalItems = mLayoutManager.getItemCount();
        if (isLoadingAllow) {
            // If it isn’t currently loading, we check to see if we have breached
            // the visibleThreshold and need to reload more data.
            // If we do need to reload some more data, we execute onLoadMore to fetch the data.
            if (!loading && (totalItems - lastVisibleItem <= visibleRowThreshold*mLayoutManager.getSpanCount())) {
                loading = onLoadMore();
                isLoadingAllow = loading;
            }
        }
    }

    // Defines the process for actually loading more data based on page
    public abstract boolean onLoadMore();

    public void dataLoaded(boolean isLoadingAllow){
        this.isLoadingAllow = isLoadingAllow;
        this.loading = false;
    }

    public void reset(){
        this.isLoadingAllow = true;
        loading = false;
    }

    @SuppressWarnings("unused")
    public void setAllowLoadMore(boolean isAllowLoadMore) {
        this.isLoadingAllow = isAllowLoadMore;
    }
}