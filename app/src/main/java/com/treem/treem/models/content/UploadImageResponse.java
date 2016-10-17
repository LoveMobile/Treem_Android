package com.treem.treem.models.content;

/**
 * Upload image response class
 */
public class UploadImageResponse {
    public long c_id;
    public String url; // full sized image
    public String stream_url; // stream url, no security required
    public int width; // width of full sized image
    public int height; // height of full sized image
}
