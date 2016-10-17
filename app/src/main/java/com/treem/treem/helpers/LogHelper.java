package com.treem.treem.helpers;

import android.util.Log;

/**
 */
public class LogHelper {
    private static final int maxLength = 500;
    public static void d(String tag,String str){
        if (str==null){
            Log.d(tag,null);
            return;
        }
        int i=0;
        int length = str.length();
        do{
            Log.d(tag,str.substring(i,i+maxLength>length?length:i+maxLength));
            i+=maxLength;
        }while (i<length);
    }
}
