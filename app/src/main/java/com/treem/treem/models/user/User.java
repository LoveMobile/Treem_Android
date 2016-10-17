package com.treem.treem.models.user;

import android.graphics.Color;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * User implementation
 */
public class User {
    public static final Type SET_TYPE = new TypeToken<HashSet<User>>() {}.getType();
    public static final Type LIST_TYPE = new TypeToken<ArrayList<User>>() {}.getType();
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_FRIENDS = 2;
    public static final int STATUS_NOT_FRIENDS = 5;
    public static final int STATUS_INVITED = 6;
    public static final int STATUS_NOT_IN_TRIM = 7;

    @SerializedName("id")
    @Expose
    private Long id;
    @SerializedName("iv_id")
    @Expose
    private long inviteId;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("first")
    @Expose
    private String first;
    @SerializedName("last")
    @Expose
    private String last;
    @SerializedName("avatar")
    @Expose
    private String avatar;
    @SerializedName("avatar_stream_url")
    @Expose
    private String avatarStreamUrl;
    @SerializedName("phone")
    @Expose
    private String phone;
    @SerializedName("c_id")
    @Expose
    private Integer contactId;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("color")
    @Expose
    private List<String> colorsList;
    @SerializedName("on_branch")
    @Expose
    private Short onBranch;
    @SerializedName("self")
    @Expose
    private Short isSelf;
    private transient UserContact contact;
    private int mName;

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFirst() {
        return first;
    }

    public String getLast() {
        return last;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getAvatarStreamUrl() {
        return avatarStreamUrl;
    }

    public long getInviteId() {
        return inviteId;
    }

    public String getPhone() {
        if (phone==null&&contact!=null&&contact.getPhone()!=null)
            return contact.getPhone();
        return phone;
    }

    public Integer getContactId() {
        return contactId;
    }

    public Integer getStatus() {
        return status;
    }

    public List<String> getColorsList() {
        return colorsList;
    }

    public boolean getOnBranch() {
        return onBranch!=null&&onBranch==1;
    }
    public void setOnBranch(boolean onBranch) {
        this.onBranch = (short)(onBranch?1:0);
    }

    public void setUserContact(UserContact contact){
        this.contact = contact;
    }

    public UserContact getContact() {
        return contact;
    }


    public String getName() {
        String name="";
        if (id==null){
            if (contact!=null)
                name = contact.getName();
        }
        else{
            name = first==null?"":first;
            if (last!=null){
                if (name.length()!=0)
                    name+=" ";
                name+=last;
            }

        }
        return name;
    }

    public boolean isTreemUser() {
        return id != null;
    }
    public int getColor(int i){
        if (colorsList==null||colorsList.size()<=i||i<0)
            return 0;
        try{
            String color = colorsList.get(i);

            return Color.parseColor("#000000".substring(0,7-color.length())+color);
        }
        catch (NumberFormatException e){
            e.printStackTrace();
            return 0;
        }
    }


    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isSelf() {
        return isSelf!=null&&isSelf==1;
    }
}
