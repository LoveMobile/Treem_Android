package com.treem.treem.helpers.recyclerview;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 *
 */
public abstract class EndlessRecyclerViewLinearScrollListener extends RecyclerView.OnScrollListener {
    // The minimum amount of items to have below your current scroll position
    // before loading more.
    private static final int visibleThreshold = 5;
    private boolean loading = true;

    private boolean isLoadingAllow = false;

    private LinearLayoutManager mLinearLayoutManager;

    public EndlessRecyclerViewLinearScrollListener(LinearLayoutManager layoutManager) {
        this.mLinearLayoutManager = layoutManager;
    }

    // This happens many times a second during a scroll, so be wary of the code you place here.
    // We are given a few useful parameters to help us work out if we need to load some more data,
    // but first we check if we are waiting for the previous load to finish.
    @Override
    public void onScrolled(RecyclerView view, int dx, int dy) {
        int lastVisibleItem = mLinearLayoutManager.findLastVisibleItemPosition();
        int totalItems = mLinearLayoutManager.getItemCount();
        if (isLoadingAllow) {
            // If it isn’t currently loading, we check to see if we have breached
            // the visibleThreshold and need to reload more data.
            // If we do need to reload some more data, we execute onLoadMore to fetch the data.
            if (!loading && (totalItems - lastVisibleItem <= visibleThreshold)) {
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

    public void setAllowLoadMore(boolean isAllowLoadMore) {
        this.isLoadingAllow = isAllowLoadMore;
    }
}