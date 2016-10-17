package com.treem.treem.services.Treem;

import android.support.annotation.Nullable;
import android.util.Pair;

import com.treem.treem.application.ApplicationPreferencesManager;
import com.treem.treem.models.signup.SignupVerificationResponse;

/**
 * Created by Matthew Walker on 2/17/16.
 * Copyright (c) 2016 Treem LLC. All rights reserved.
 */
public class TreemOAuthUserToken {
    public final static TreemOAuthUserToken SHARED_INSTANCE = new TreemOAuthUserToken();

    private static final String keyUserToken        = "userToken";
    private static final String keyUserTokenSecret  = "userTokenSecret";
    private static final String keyUserStatus  = "userStatus";

    public static final int UserStatusFullUser = 0;
    public static final int UserStatusTempVerified = 1;
    public static final int UserStatusTempUnverified = 2;


    public void clearAccessTokens() {
        // clear device specific tokens (not UUID)
        ApplicationPreferencesManager prefs = ApplicationPreferencesManager.SHARED_INSTANCE;

        prefs.removevalue(keyUserToken);
        prefs.removevalue(keyUserTokenSecret);
    }

    boolean isUserAuthenticated() {
        return false;
    }

    public boolean setAccessTokens(SignupVerificationResponse response) {
        ApplicationPreferencesManager prefs = ApplicationPreferencesManager.SHARED_INSTANCE;

        prefs.setValue(keyUserToken, response.oauth_token);
        prefs.setValue(keyUserTokenSecret, response.oauth_token_secret);

        return true;
    }

    public boolean tokensAreSet() {
        ApplicationPreferencesManager prefs = ApplicationPreferencesManager.SHARED_INSTANCE;

        String token = prefs.getValue(keyUserToken);

        if (token.isEmpty()) {
            return false;
        }

        String tokenSecret = prefs.getValue(keyUserTokenSecret);

        return (!tokenSecret.isEmpty());
    }
    /**
     * get oAuth user tokens
     * @return the pair of user token and user secret key or null if tokens not set
     */
    @Nullable
    public Pair<String, String> getUserTokens() {
        ApplicationPreferencesManager prefs = ApplicationPreferencesManager.SHARED_INSTANCE;

        String userToken      = "";
        String userSecret   = "";

        userToken     = prefs.getValue(keyUserToken);
        userSecret  = prefs.getValue(keyUserTokenSecret);
        // check is token set
        if (userToken.isEmpty() || userSecret.isEmpty()) {
            return null;
        }

        return Pair.create(userToken, userSecret);
    }

    /**
     * Check is user registered
     * @return true if user registered
     */
    public boolean isUserRegistered() {
        ApplicationPreferencesManager prefs = ApplicationPreferencesManager.SHARED_INSTANCE;
        return prefs.getIntValue(keyUserStatus,-1)==UserStatusFullUser;
    }

    /**
     * Set user status
     * @param userStatus the user status
     * 0 - full registered
     * 1 - verified
     * 2 - unverified
     */

    public void setUserStatus(int userStatus){
        ApplicationPreferencesManager prefs = ApplicationPreferencesManager.SHARED_INSTANCE;
        prefs.setIntValue(keyUserStatus,userStatus);
    }

    public void clearUserStatus(){
        ApplicationPreferencesManager prefs = ApplicationPreferencesManager.SHARED_INSTANCE;
        prefs.removevalue(keyUserStatus);
    }

}
