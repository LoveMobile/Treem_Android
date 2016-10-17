package com.treem.treem.models.content;

/**
 * Video detail model
 */
public class Video {
    private long c_id; //content_id
    private String v_url; //video url
    private String t_url; //thumbnail url
    private String t_stream_url; //stream url, no security required
    private int t_width; //thumbnail width
    private int t_height; //thumbnail height
    private String create_date; //format: yyyy-MM-ddTHH:mm:ss.sssssssZ
    private short owner; //1=True, NULL=False
    public long getId(){
        return c_id;
    }
    public String getVideoUrl(){
        return v_url;
    }
}
