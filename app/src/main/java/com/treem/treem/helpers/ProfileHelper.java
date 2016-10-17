package com.treem.treem.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.treem.treem.models.profile.UserProfile;

/**
 * Created by Alexey Rogovoy (lexapublic@gmail.com) on 22.06.2016.
 */
public class ProfileHelper {
    private static final String PREF_PROFILE = "pref.profile";
    private static ProfileHelper sInstance;
    private SharedPreferences settings;
    private UserProfile profile;

    public ProfileHelper(Context context) {
        settings = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static ProfileHelper getInstance(Context context){
        if (sInstance==null)
            sInstance = new ProfileHelper(context);
        return sInstance;
    }
    public UserProfile getProfile(){
        if (profile==null)
            loadProfile();
        return profile;
    }

    private void loadProfile() {
        String json = settings.getString(PREF_PROFILE,null);
        if (json!=null){
            try{
                profile = new Gson().fromJson(json,UserProfile.class);
            }
            catch (JsonSyntaxException e){
                e.printStackTrace();
            }
        }
    }

    public void setProfile(UserProfile profile){
        this.profile = profile;
        if (profile==null)
            clearProfile();
        else{
            try{
                settings.edit().putString(
                        PREF_PROFILE,new Gson().toJson(profile,UserProfile.class)).apply();
            }
            catch (JsonSyntaxException e){
                e.printStackTrace();
            }
        }

    }
    public void clearProfile(){
        settings.edit().remove(PREF_PROFILE).apply();
        profile = null;
        sInstance = null;
    }
}
