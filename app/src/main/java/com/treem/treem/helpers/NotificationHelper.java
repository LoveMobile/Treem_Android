package com.treem.treem.helpers;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

public class NotificationHelper {
    /**
     * Show error notification
     * @param context base context
     * @param anchor anchor view to show snack bar if needed
     * @param notification notification text
     */
    public static void showError(Context context, View anchor, String notification){
        Toast.makeText(context, notification, Toast.LENGTH_SHORT).show();
    }

    /**
     *
     * Show error notification
     * @param context base context
     * @param anchor anchor view to show snack bar if needed
     * @param notificationId notification string id
     */
    public static void showError(Context context, View anchor, int notificationId){
        showError(context,anchor,context.getString(notificationId));
    }
    /**
     * Show error notification
     * @param notification notification text
     */
    public static void showError(Context context, String notification){
        Toast.makeText(context, notification, Toast.LENGTH_SHORT).show();
    }

    /**
     *
     * Show error notification
     * @param context base context
     * @param notificationId notification string id
     */
    public static void showError(Context context, int notificationId){
        showError(context,context.getString(notificationId));
    }
    /**
     * Show success notification
     * @param notification notification text
     */
    public static void showSuccess(Context context, String notification){
        Toast.makeText(context, notification, Toast.LENGTH_SHORT).show();
    }

    /**
     *
     * Show success notification
     * @param context base context
     * @param notificationId notification string id
     */
    public static void showSuccess(Context context, int notificationId){
        showSuccess(context,context.getString(notificationId));
    }

}
