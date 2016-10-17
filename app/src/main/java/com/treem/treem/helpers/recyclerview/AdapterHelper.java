package com.treem.treem.helpers.recyclerview;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.treem.treem.R;
import com.treem.treem.models.user.User;

import java.util.List;

/**
 */
public class AdapterHelper {
    /**
     * Fill linear layout with branch colors
     * @param context base context
     * @param layoutColors layout to fill with colors
     * @param user user
     */
    public static void fillColors(Context context,LinearLayout layoutColors, User user){
        if (user!=null&&user.getColorsList()!=null&&user.getColorsList().size()>0&&context!=null) {
            int colorBranchSize = context.getResources().getDimensionPixelSize(R.dimen.member_branch_color_size);
            layoutColors.setVisibility(View.VISIBLE);
            layoutColors.removeAllViews();
            List<String> colors = user.getColorsList();
            if (colors != null) {
                for (int i = 0; i < colors.size(); i++) {
                    View v = new View(context);
                    int color = user.getColor(i);
                    v.setBackgroundColor(color);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(colorBranchSize, colorBranchSize);
                    params.setMargins(colorBranchSize, 0, 0, 0);
                    v.setLayoutParams(params);
                    layoutColors.addView(v, params);
                }
            }
        }
        else{
            layoutColors.setVisibility(View.GONE);
        }
    }
}
