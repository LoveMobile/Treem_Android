package com.treem.treem.models.content;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;

import com.treem.treem.helpers.Utils;

import java.io.IOException;

/**
 * Upload item model
 */
public class UploadItem {
    private static final int imageTypeJpg = 0;
    private static final int imageTypePng = 1;
    private static final int imageTypeGif = 2;
    public String buffer; // base64 string of file bytes
    public long buffer_size; // number of original file bytes
    public int type; // type is an enum of the file type, values are defined below
    public short profile; // used for images, indicates if it is a profile image or not, 1=True, 0 or null =False
    public int orientation; // used for videos to know how video was recorded, see “orientation” below for more details

    /**
     * Create upload image object from uri
     * @param context base context
     * @param uri image uri
     * @param isProfile is this a profile image
     * @return the new instance of upload image object
     * @throws IOException on failed to read image
     * @throws OutOfMemoryError on failed to create base64 string from image file
     */
    public static UploadItem uploadImage(Context context, Uri uri, boolean isProfile)throws IOException,OutOfMemoryError{
        UploadItem item = fillWithImage(context,uri);
        if (item!=null){
            item.profile = (short)(isProfile?1:0);
            String mimeType = Utils.getMimeType(context,uri);
            if (mimeType!=null){
                if (mimeType.contains("image")){
                    if (mimeType.contains("png"))
                        item.type = imageTypePng;
                    else if (mimeType.contains("jpg")||mimeType.contains("jpeg"))
                        item.type = imageTypeJpg;
                    else if (mimeType.contains("gif"))
                        item.type = imageTypeGif;
                    return item;
                }
            }

        }
        return null;
    }

    /**
     * Create new object with image data
     * @param context base context
     * @param uri image uri
     * @return new object or null if failed to cerate new object
     * @throws IOException on failed read image
     * @throws OutOfMemoryError on failed to create base 64 string from image
     */
    private static UploadItem fillWithImage(Context context,Uri uri) throws IOException,OutOfMemoryError{
        if (uri==null)
            return null;
        byte[] image = Utils.readImageAsByteArray(context, uri);
        UploadItem item = new UploadItem();
        if (image != null) {
            item.buffer = Base64.encodeToString(image, Base64.DEFAULT);
            item.buffer_size = image.length;
            return item;
        }
        return null;
    }
}
