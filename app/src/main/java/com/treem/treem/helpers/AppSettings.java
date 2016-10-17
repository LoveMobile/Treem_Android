package com.treem.treem.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 */
public class AppSettings {
    private static final String PREF_MEMBERS_CONTACT_PERMISSION_ASKED = "pref.members.permission.contacts.asked";
    private final SharedPreferences settings;

    public AppSettings(Context context){
        settings = PreferenceManager.getDefaultSharedPreferences(context);
    }
    public boolean isMembersContactPermissionsAsked(){
        return settings.getBoolean(PREF_MEMBERS_CONTACT_PERMISSION_ASKED,false);
    }
    public void setMembersContactPermissionAsked(boolean asked){
        settings.edit().putBoolean(PREF_MEMBERS_CONTACT_PERMISSION_ASKED,asked).apply();
    }
}
