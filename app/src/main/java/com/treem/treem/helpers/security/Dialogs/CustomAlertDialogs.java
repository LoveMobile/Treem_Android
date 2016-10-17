package com.treem.treem.helpers.security.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;

import com.treem.treem.R;

/**
 * Created by Matthew Walker on 2/22/16.
 * Copyright (c) 2016 Treem LLC. All rights reserved.
 */

public class CustomAlertDialogs {

    // show generic error alert (fallback for more specific error views)
    public static void showGeneralErrorAlertDialog(Context context) {
        Resources resources = context.getResources();

        // no callbacks
        AlertDialogOptions options = new AlertDialogOptions();
        options.title = resources.getString(R.string.error);
        options.message = resources.getString(R.string.error_general_message);

        CustomAlertDialogs.showCustomErrorAlertDialog(context, options);
    }

    // show custom error alert
    public static void showCustomErrorAlertDialog(Context context, final AlertDialogOptions options) {
        // make sure options are passed
        if (options == null) {
            CustomAlertDialogs.showGeneralErrorAlertDialog(context);
            return;
        }

        // create the alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(options.title);
        builder.setMessage(options.message);

        // set positive button text and click event
        builder.setPositiveButton(options.positiveButtonTextId, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                options.positiveOnClick();
            }
        });

        // set negative button text and click event (if using)
        if (options.showNegativeButton) {
            builder.setNegativeButton(options.negativeButtonTextId, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    options.negativeOnClick();
                }
            });
        }

        builder.setCancelable(true);

        AlertDialog alert = builder.create();

        // add dialog callbacks if given
        alert.show();
    }
}
