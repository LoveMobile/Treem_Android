package com.treem.treem.helpers.recyclerview;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Item padding decoration
 */
public class PaddingItemDecoration extends RecyclerView.ItemDecoration {
    private int padding = 0; //padding in pixels
    private int columnCount = 1; //grid columns count

    /**
     * Get new item decorator
     * @param padding padding in pixels
     * @param columnCount column count
     */
    public PaddingItemDecoration(int padding, int columnCount) {
        this.padding = padding;
        this.columnCount = columnCount;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        float leftPadding = padding/3f;
        float rightPadding = padding/3f;
        float topPadding = 0;
        float bottomPadding = padding;
        int position = parent.getChildAdapterPosition(view);
        if (position%columnCount==0) {
            leftPadding = 0;
            rightPadding = padding*2f/3f;
        }
        if ((position+1)%columnCount==0) {
            rightPadding = 0;
            leftPadding = padding*2f/3f;
        }
        outRect.set(Math.round(leftPadding),Math.round(topPadding),Math.round(rightPadding), Math.round(bottomPadding));
    }
}
