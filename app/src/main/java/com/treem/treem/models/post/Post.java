package com.treem.treem.models.post;

import com.google.gson.reflect.TypeToken;
import com.treem.treem.models.user.User;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Post content
 */
public class Post {
    public static final Type LIST_TYPE = new TypeToken<ArrayList<Post>>() {}.getType();
    private long p_id;                      // post_id
    private Long u_id;                      // user_id
    private String msg;                     // post message
    private short tgd;                      // indicates if current user is tagged, 1=True, NULL=False
    private String l_url;                   // link url
    private String l_title;                 // link title
    private String l_img_url;               // link image url
    private String l_desc;                  // link description
    private Long c_id;                      // content id
    private short c_type;                   // content type, 0=IMAGE, 1=VIDEO
    private String c_url;                   // content url ( either thumbnail or feed url)
    private String c_stream_url;            // url that can be used to stream the content (no additional secruity required)
    private int c_width;                    // content width ( either thumbnail or feed url)
    private int c_height;                   // content height ( either thumbnail or feed url)
    private List<Reaction> react_cnt;       // users reaction to this post
    private int s_react;                    // self reaction (current user’s reaction to post)
    private int share_cnt;                  // share count
    private int cmt_cnt;                    // reply count (comment count)
    private int shareable;                  // indicates if post can be shared, 1=True, NULL=False
    private Long sh_id;                     // share_id
    private Long sh_u_id;                   // user_id of person who shared post
    private String sh_msg;                  // message from when it was shared
    private String sh_date;                 // date post was shared  format: yyyy-MM-ddTHH:mm:ss.sssssssZ
    private short sh_editable;              // indicates if user can edit share, 1=True, NULL=False
    private short v_once;                   // indicates if this post can only be viewed once, 1=True, NULL=False
    private String p_date;                  // post date (last modified)  format: yyyy-MM-ddTHH:mm:ss.sssssssZ
    private String expires;                 // date post expires  format: yyyy-MM-ddTHH:mm:ss.sssssssZ
    private short editable;                 // indicates if current user can edit post, 1=True, NULL=False
    private String p_color;                 // color of branch path for the current user (in all feed / trunk only when posted in a specific branch)
    private Set<User> users;               // list of users in post, if these users are in previous posts, they will not be duplicated in later posts

    public boolean isContentPost() {
        return c_id!=null;
    }
    public boolean isImageContent(){
        return isContentPost()&&c_type!=1;
    }
    public boolean isVideoContent(){
        return isContentPost()&&c_type==1;
    }

    public boolean isLinkPost() {
        return l_url!=null;
    }
    public boolean isSharedPost(){
        return sh_id!=null;
    }

    public Long getShareUserId() {
        return sh_u_id;
    }

    public Set<User> getUsers() {
        return users;
    }

    public Long getUserId() {
        return u_id;
    }

    public String getShareDate() {
        return sh_date;
    }

    public String getShareText() {
        return sh_msg;
    }

    public String getDate() {
        return p_date;
    }
    public String getText() {
        return msg;
    }

    public int getCommentsCount() {
        return cmt_cnt;
    }
    public int getReactsCount() {
        return react_cnt!=null?react_cnt.size():0;
    }
    public int getSharesCount(){
        return share_cnt;
    }
    public boolean isShareable(){
        return shareable==1;
    }

    public List<Reaction> getReacts() {
        return react_cnt;
    }

    public boolean isUserTagged() {
        return tgd==1;
    }

    public String getContentStreamUrl() {
        return c_stream_url;
    }

    public int getContentWidth() {
        return c_width;
    }
    public int getContentHeight() {
        return c_height;
    }

    public String getLinkTitle() {
        return l_title;
    }
    public String getLinkDescription() {
        return l_desc;
    }
    public String getLinkImage() {
        return l_img_url;
    }


    public long getContentId() {
        return c_id;
    }

    public int getContentType() {
        return c_type;
    }

    public String getContentUrl() {
        return c_url;
    }

    public String getLinkUrl() {
        return l_url;
    }

    public void addUser(User user) {
        if (users==null)
            users = new HashSet<>();
        if (user!=null)
            users.add(user);
    }

    /**
     * Get user by user id
     * @param id user id
     * @return user by user id
     */
    public User getUser(Long id) {
        if (users==null||id==null)
            return null;
        for (User user:users){
            if (id.equals(user.getId()))
                return user;
        }
        return null;
    }
}
