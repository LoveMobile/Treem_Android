package com.treem.treem.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimestampUtils {

    /**
     * Return an ISO 8601 combined date and time string for current date/time
     *
     * @return String with format "yyyy-MM-dd'T'HH:mm:ss'Z'"
     */
    @SuppressWarnings("unused")
    public static String getISO8601StringForCurrentDate() {
        Date now = new Date();
        return getISO8601StringForDate(now);
    }

    /**
     * Return an ISO 8601 combined date and time string for specified date/time
     *
     * @param date
     *            Date
     * @return String with format "yyyy-MM-dd'T'HH:mm:ss'Z'"
     */
    public static String getISO8601StringForDate(Date date) {
        if (date==null)
            return "";
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

    /**
     * Parse date parameter from api
     * @param date parameter from api
     * @return parsed date or null if error
     */
    public static Date parseDate(String date) {
        if (date==null)
            return null;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Format date with default system format
     * @param date date to format
     * @return formated date
     */
    public static String formatDate(Date date) {
        if (date==null)
            return "";
        DateFormat df = SimpleDateFormat.getDateInstance();
        return df.format(date);
    }

    /**
     * Format date for api requests 5/20/2015 for example
     * @param date date to format
     * @return formatted date
     */
    public static String getFormattedDate(Date date) {
        @SuppressLint("SimpleDateFormat")
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        return df.format(date);
    }

    public static String friendlyFormatDate(Context context,Date date) {
        return (String)DateUtils.getRelativeTimeSpanString(date.getTime(),System.currentTimeMillis(),DateUtils.MINUTE_IN_MILLIS);
    }
    public static String friendlyFormatDateTime(Context context,Date date) {
        return (String)DateUtils.getRelativeDateTimeString(context,date.getTime(),DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
    }

}
