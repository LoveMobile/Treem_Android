package com.treem.treem.models.profile;

/**
 * Tree settings for user
 */
public class UserTreeSettings {
    public short push_notif; //push notification settings. 1 = TRUE, 0 = FALSE

    /**
     * Get is push set
     * @return true if push for current tree set
     */
    public boolean isPushSet(){
        return push_notif==1;
    }

    /**
     * Set push notification setting
     * @param isSet true if push enabled
     */
    public void setPush(boolean isSet){
        push_notif =(short)(isSet?1:0);
    }
}
