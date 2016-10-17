package com.treem.treem.models.content;

import com.google.gson.reflect.TypeToken;
import com.treem.treem.models.post.Post;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Media {
    public static final Type LIST_TYPE = new TypeToken<ArrayList<Media>>() {
    }.getType();
    public static final int mediaTypeImage = 0;
    public static final int mediaTypeVideo = 1;
    private long c_id;
    private int c_type;
    private String c_url;
    private String c_stream_url;

    private int c_width;
    private int c_height;

    public Media(Post post) {
        c_id = post.getContentId();
        c_type = post.getContentType();
        c_url = post.getContentUrl();
        c_stream_url = post.getContentStreamUrl();
        c_width = post.getContentWidth();
        c_height = post.getContentHeight();
    }

    public long getId() {
        return c_id;
    }

    public int getType() {
        return c_type;
    }

    public String getUrl() {
        return c_url;
    }

    public String getStreamUrl() {
        return c_stream_url;
    }

    public int getWidth() {
        return c_width;
    }

    public int getHeight() {
        return c_height;
    }
}
