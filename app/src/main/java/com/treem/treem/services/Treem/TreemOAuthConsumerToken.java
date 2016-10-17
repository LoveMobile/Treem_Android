package com.treem.treem.services.Treem;

import android.util.Pair;

import com.treem.treem.application.ApplicationPreferencesManager;
import com.treem.treem.helpers.security.Encryption;
import com.treem.treem.models.signup.SignupAuthorizeAppResponse;

import java.security.KeyStore;

/**
 * Created by Matthew Walker on 2/12/16.
 * Copyright (c) 2016 Treem LLC. All rights reserved.
 */
public class TreemOAuthConsumerToken {
    public final static TreemOAuthConsumerToken SHARED_INSTANCE = new TreemOAuthConsumerToken();

    private final byte[] appConsumerKey     = {56, 85, 49, 56, 67, 81, 98, 111, 120, 88, 109, 84, 97, 122, 116, 108, 104, 48, 66, 84, 66, 70, 101, 70, 84, 54, 83, 121, 81, 80, 87, 108, 48, 68, 49, 49, 50, 56, 65, 48, 45, 97, 99, 66, 46, 100, 116, 109, 50, 105, 105, 105, 65, 85, 95, 46, 65, 52};
    private final byte[] appConsumerSecret  = {56, 52, 55, 100, 98, 97, 57, 55, 45, 51, 51, 97, 97, 45, 52, 99, 51, 98, 45, 57, 56, 51, 51, 45, 49, 52, 101, 51, 98, 100, 55, 56, 98, 97, 56, 102, 95, 122, 65, 50, 75, 89, 122, 108, 45, 48, 54, 87, 87, 119, 69, 101, 118, 90, 52, 109, 115, 109, 103};

    private KeyStore keyStore = null;

    private static final String keyConsumerUUID         = "consumerUUID";
    private static final String keyConsumerToken        = "consumerToken";
    private static final String keyConsumerTokenSecret  = "consumerTokenSecret";

    // get oAuth consumer tokens
    public Pair<String, String> getConsumerTokens() {
        ApplicationPreferencesManager prefs = ApplicationPreferencesManager.SHARED_INSTANCE;

        String consumerKey      = "";
        String consumerSecret   = "";

        // check UUID first
        String UUID = prefs.getValue(keyConsumerUUID);

        if (UUID.isEmpty()) {
            // clear device tokens for consistency if UUID can't be retrieved
            // (consumer tokens will then default to app tokens rather than device specific)
            this.clearDeviceSpecificTokens();
        }
        else {
            // retrieve device specific credentials
            consumerKey     = prefs.getValue(keyConsumerToken);
            consumerSecret  = prefs.getValue(keyConsumerTokenSecret) + UUID;
        }

        // get app default tokens if no device specific tokens are set
        if (consumerKey.isEmpty() || consumerSecret.isEmpty()) {
            // get app defaults if no device specific consumer credentials set
            consumerKey = Encryption.SHARED_INSTANCE.getObfuscatedKeyWithClassTypes(appConsumerKey, "TreeActivity", "AlertsActivity", "SignupVerificationActivity");
            consumerSecret = Encryption.SHARED_INSTANCE.getObfuscatedKeyWithClassTypes(appConsumerSecret, "BranchActivity", "SignupPhoneActivity", "TreeAddFormActivity");
        }

        return Pair.create(consumerKey, consumerSecret);
    }

    public void clearDeviceSpecificTokens() {
        // clear device specific tokens (not UUID)
        ApplicationPreferencesManager prefs = ApplicationPreferencesManager.SHARED_INSTANCE;

        prefs.removevalue(keyConsumerToken);
        prefs.removevalue(keyConsumerTokenSecret);
    }

    public boolean deviceSpecificUUIDAndTokensAreSet() {
        ApplicationPreferencesManager prefs = ApplicationPreferencesManager.SHARED_INSTANCE;

        String uuid = prefs.getValue(keyConsumerUUID);

        if (uuid.isEmpty()) {
            return false;
        }

        String token = prefs.getValue(keyConsumerToken);

        if (token.isEmpty()) {
            return false;
        }

        String secret = prefs.getValue(keyConsumerTokenSecret);

        return (!secret.isEmpty());
    }

    // return true if tokens successfully set
    public boolean setDeviceSpecificUUIDAndTokens(SignupAuthorizeAppResponse response) {
        ApplicationPreferencesManager prefs = ApplicationPreferencesManager.SHARED_INSTANCE;

        prefs.setValue(keyConsumerUUID, response.oauth_consumer_uuid);
        prefs.setValue(keyConsumerToken, response.oauth_consumer_key);
        prefs.setValue(keyConsumerTokenSecret, response.oauth_consumer_secret);

        return true;
    }
}