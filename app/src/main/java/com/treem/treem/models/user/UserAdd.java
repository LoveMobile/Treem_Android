package com.treem.treem.models.user;

import com.google.gson.reflect.TypeToken;
import com.treem.treem.helpers.security.Phone.PhoneUtil;

import java.lang.reflect.Type;
import java.util.HashSet;

/**
 * Add user class
 */
public class UserAdd {
    public static final Type SET_TYPE = new TypeToken<HashSet<UserAdd>>() {
    }.getType();

    public Long id;
    public String phone;
    public UserAdd(User user){
        if (user==null)
            return;
        id = user.getId();
        if (id==null)
            phone = PhoneUtil.getE164FormattedString(user.getPhone());
    }

    public UserAdd(Long id, String formattedPhone) {
        this.id = id;
        this.phone = formattedPhone;
    }
}
