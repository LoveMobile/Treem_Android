package com.treem.treem.helpers;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Some helper functions
 */
public class Utils {
    /**
     * The list of supported image types
     */
    public static final String[] supportedImages = new String[]{
            "png",
            "gif",
            "jpg",
            "jpeg"
    };

    public static boolean isEmptyAnswer(String data) {
        return TextUtils.isEmpty(data)||"\"\"".equals(data);
    }

    public static enum ColorTint{
        lightTintColor,
        darkTintColor
    }
    /**
     * Limit max length of edit field
     * @param edit edit text field
     * @param maxLength max length of edit field
     */
    public static void setMaxSymbols(EditText edit, int maxLength) {
        edit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
    }

    /**
     * Simple check is entered email valid
     * @param email email as a string
     * @return true if email is valid
     */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Create a new media file with provided extension
     * @param ext the extension
     * @return created file or null if failed
     */
    public static File createMediaFile(String ext) {
        File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        try {
            if (!root.exists())
                //noinspection ResultOfMethodCallIgnored
                root.mkdir();
            File mpDir = new File(root, "Treem");
            if (mpDir.exists() && !mpDir.isDirectory())
                //noinspection ResultOfMethodCallIgnored
                mpDir.delete();
            if (!mpDir.exists())
                //noinspection ResultOfMethodCallIgnored
                mpDir.mkdir();
            return File.createTempFile("media_", ext, mpDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Check is image supported by app?
     * @param subtype mime subtype
     * @return true if supported
     */
    public static boolean isImageSupported(String subtype) {
        subtype = subtype.toLowerCase();
        for (String type:supportedImages){
            if (type.contains(subtype))
                return true;
        }
        return false;
    }

    /**
     * Get image mime type
     * @param context the base context
     * @param uri the image uri
     * @return mimetype or null if can't get it
     */
    public static String getMimeType(Context context, Uri uri) {
        String[] columns = { MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.MIME_TYPE };

        Cursor cursor = context.getContentResolver().query(uri, columns, null, null, null);
        if (cursor==null) {
            return checkExtension(uri);
        }
        cursor.moveToFirst();

        int pathColumnIndex     = cursor.getColumnIndex( columns[0] );
        int mimeTypeColumnIndex = cursor.getColumnIndex( columns[1] );

        //noinspection unused
        String contentPath = cursor.getString(pathColumnIndex);
        String mimeType    = cursor.getString(mimeTypeColumnIndex);
        cursor.close();
        return mimeType;
    }

    /**
     * Check mimetype by file extension
     * @param uri the image uri
     * @return mimetype or null if not supported
     */
    private static String checkExtension(Uri uri) {
        if (uri==null)
            return null;
        String path = uri.toString().toLowerCase();
        for (String type:supportedImages){
            if (path.endsWith(type))
                return "image/"+type;
        }
        return null;
    }

    /**
     * Read image as a byte array
     * @param context base context
     * @param uri image uri
     * @return byte array with data
     * @throws IOException on error read the image file
     */
    public static byte[] readImageAsByteArray(Context context, Uri uri) throws IOException{
        InputStream iStream = null;
        try {
            iStream = context.getContentResolver().openInputStream(uri);
            if (iStream!=null) {
                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];

                int len = 0;
                while ((len = iStream.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, len);
                }
                return byteBuffer.toByteArray();
            }
        }
        finally {
            if (iStream!=null){
                try {
                    iStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Get contrast color for selected color
     * @param color selected color
     * @return dark ot light tint color should use
     */
    public static ColorTint getContrastColor(Integer color) {
        if (((Color.red(color)*0.299 + Color.green(color)*0.587 + Color.blue(color)*0.114)) > 186){
            return ColorTint.darkTintColor;
        }
        else
            return ColorTint.lightTintColor;
    }

    /**
     * Get darker of selected color to set to status bar
     * @param color selected color
     * @return darker color
     */
    public static int getDarkerColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f; // value component
        return Color.HSVToColor(hsv);
    }
    public static void updateStatusbarColor(Window window, int color, ColorTint tint) {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP&&window!=null) {//get darker of the selected color and set it to status bar
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){

                int newUiVisibility = window.getDecorView().getSystemUiVisibility();

                if(tint== Utils.ColorTint.lightTintColor)
                {
                    //Light Text to show up on your dark status bar
                    newUiVisibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
                else
                {
                    //Dark Text to show up on your light status bar
                    newUiVisibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }

                window.getDecorView().setSystemUiVisibility(newUiVisibility);

            }
        }
    }

}
