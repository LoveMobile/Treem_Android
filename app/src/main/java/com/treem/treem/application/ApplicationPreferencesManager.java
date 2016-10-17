package com.treem.treem.application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Matthew Walker on 2/26/16.
 * Copyright (c) 2016 Treem LLC. All rights reserved.
 */
public class ApplicationPreferencesManager {
    public static final ApplicationPreferencesManager SHARED_INSTANCE = new ApplicationPreferencesManager();
    private final String SHARED_PREFERENCES_NAME = "com.treem.treem";

    // getValue, returns "" if no value found
    public String getValue(String key) {
        // get shared preferences
        SharedPreferences settings = this.getSharedPreferences();

        // return value at key
        String value = settings.getString(key, "");

        return value;
    }

    public void removevalue(String key) {
        // get shared preferences editor
        SharedPreferences settings = this.getSharedPreferences();
        SharedPreferences.Editor editor = settings.edit();

        editor.remove(key);
        editor.apply();
    }

    public void setValue(String key, String value) {
        // get shared preferences editor
        SharedPreferences settings = this.getSharedPreferences();
        SharedPreferences.Editor editor = settings.edit();

        // place the new value
        editor.putString(key, value);
        editor.apply();
    }

    private SharedPreferences getSharedPreferences() {
        Context appContext = ApplicationMain.getAppContext();

        return appContext.getSharedPreferences(SHARED_PREFERENCES_NAME, appContext.MODE_PRIVATE);
    }

    /**
     * Get integer value from application preferences
     * @param keyName key name
     * @param defaultValue default value for this key
     * @return value of the key or default value if key doesn't exists
     */
    public int getIntValue(String keyName, int defaultValue) {
        SharedPreferences settings = this.getSharedPreferences();
        return settings.getInt(keyName,defaultValue);
    }

    /**
     * Set integer value to the registry key
     * @param keyName the key name
     * @param value value to set
     */
    public void setIntValue(String keyName, int value) {
        SharedPreferences settings = this.getSharedPreferences();
        settings.edit().putInt(keyName,value).apply();
    }
}