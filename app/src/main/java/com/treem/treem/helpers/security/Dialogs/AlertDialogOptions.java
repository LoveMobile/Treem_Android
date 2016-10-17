package com.treem.treem.helpers.security.Dialogs;

/**
 * Created by Matthew Walker on 2/22/16.
 * Copyright (c) 2016 Treem LLC. All rights reserved.
 */
public class AlertDialogOptions {
    public String title;
    public String message;
    public int positiveButtonTextId = android.R.string.ok;
    public int negativeButtonTextId = android.R.string.no;
    public boolean showNegativeButton;

    // extend this class and override these functions if using callbacks
    public void positiveOnClick() {}
    public void negativeOnClick() {}
}
