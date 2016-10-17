package com.treem.treem.models.user;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashSet;

/**
 * User for remove
 */
public class UserRemove {
    public static final Type SET_TYPE = new TypeToken<HashSet<UserRemove>>() {
    }.getType();
    public Long id;
    public Long iv_id;
    public UserRemove(User user){
        if (user==null)
            return;
        if (user.getInviteId()>0)
            iv_id = user.getInviteId();
        id = user.getId();
    }
}
