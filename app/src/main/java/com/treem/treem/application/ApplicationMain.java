package com.treem.treem.application;

import android.app.Application;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.treem.treem.BuildConfig;
import com.treem.treem.helpers.ProfileHelper;
import com.treem.treem.services.Treem.TreemOAuthConsumerToken;
import com.treem.treem.services.Treem.TreemOAuthUserToken;

/**
 * Created by Matthew Walker on 2/17/16.
 * Copyright (c) 2016 Treem LLC. All rights reserved.
 */
public class ApplicationMain extends Application {

    private static Context context;

    /* Static application settings */

    // true if debugging, false for release mode
    public final static boolean DEBUG                       = BuildConfig.DEBUG; // note: if false, since this value is static/final, the compiler should remove if statements if this variable is the only clause

    public final static ApplicationMain SHARED_INSTANCE     = new ApplicationMain();

    // treem service configuration settings
    public final static String TREEM_SERVICE_DOMAIN         = "treemtest.com"; // do not add trailing slash
    public final static String AWS_COGNITO_PROVIDER_NAME    = "awscognito.treemtest.com";

    // sinch configuration settings
    public final static String SINCH_APPLICATION_KEY        = "80c2e932-89d5-41c8-8d43-d709dba5e08c";
    public final static String SINCH_APPLICATION_SECRET     = "T8IXV391+EWxeAunFfcXcw==";
    public final static String SINCH_ENVIRONMENT_HOST       = "sandbox.sinch.com";


    /**
     * Minimal age for registration
     */
    public static final int MIN_AGE_YEARS = 13;

    /**
     * Max length of registration fields
     */
    public static final int MAX_REGISTRATION_FIELD_LENGTH = 100;
    private static boolean isExpiredRequested = false;

    /* Application methods */

    public static Context getAppContext() {
        return ApplicationMain.context;
    }

    // check if current device is fully authenticated
    public boolean isDeviceAuthenticated() {
        // check if device tokens are set
        if (TreemOAuthConsumerToken.SHARED_INSTANCE.deviceSpecificUUIDAndTokensAreSet()) {
            return true;
        }
        else {
            // if at least one token not set, clear all tokens for consistency safety
            TreemOAuthConsumerToken.SHARED_INSTANCE.clearDeviceSpecificTokens();

            return false;
        }
    }

    // check if current user is authenticated
    public boolean isUserAuthenticated() {
        return TreemOAuthUserToken.SHARED_INSTANCE.tokensAreSet();
    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void onCreate() {
        super.onCreate();
        ApplicationMain.context = getApplicationContext();
    }
    public void userTokenExpired(boolean clearConsumerToken){
        if (!isExpiredRequested) {
            ProfileHelper.getInstance(this).clearProfile();
            SessionExpiredActivity.showExpiredDialog(context, clearConsumerToken);
            setExpiredRequested(true);
        }
    }
    public static void setExpiredRequested(boolean requested){
        isExpiredRequested = requested;
    }
}
