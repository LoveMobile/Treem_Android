package com.treem.treem.models.alert;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.treem.treem.models.user.User;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Alert class
 */
public class Alert {
    public static final int REASON_PENDING_FRIEND_REQUEST = 0;
    public static final int REASON_ACCEPTED_FRIEND_REQUEST = 1;
    public static final int REASON_ACCEPTED_FRIEND_INVITE = 2;
    public static final int REASON_POST_REPLY = 3;
    public static final int REASON_POST_REACTION = 4;
    public static final int REASON_POST_SHARE = 5;
    public static final int REASON_POST_ABUSE_SENT = 6;
    public static final int REASON_POST_ABUSE_REVOKED = 7;
    public static final int REASON_COMMENT_REPLY = 8;
    public static final int REASON_TAGGED_POST = 9;
    //some chat alerts take up types 10 and 11
    public static final int REASON_BRANCH_SHARE = 12;
    public static final int REASON_POST_REPLY_REACTION = 13;

    // this is purely for DB record retrieval
    public static final int REASON_NON_REQUEST_ALERTS = 99;

    public static final int REASON_POST_UPLOAD_FINISHED = 100;
    public static final int REASON_CHAT_UPLOAD_FINISHED = 101;
    public static final int REASON_REPLY_UPLOAD_FINISHED = 102;

    public static final Type LIST_TYPE = new TypeToken<ArrayList<Alert>>() {
    }.getType();

    public static final Type SET_TYPE = new TypeToken<HashSet<Alert>>() {
    }.getType();

    @SerializedName("a_id")
    @Expose
    private long alertId; // alert_id
    @SerializedName("fr_usr")
    @Expose
    private User userFrom;
    @SerializedName("s_id")
    @Expose
    private long sourceId; // source_id
    @SerializedName("reason")
    @Expose
    private int reason; // alert reason (type of alert), see below for details
    @SerializedName("viewed")
    @Expose
    private short viewed; // Indicates if the alert has been viewed,1=TRUE, 0 = FALSE

    @SerializedName("created")
    @Expose
    private String created; // alert create date  format: yyyy-MM-ddTHH:mm:ss.sssssssZ

    public long getAlertId() {
        return alertId;
    }

    public User getUserFrom() {
        return userFrom;
    }

    public long getSourceId() {
        return sourceId;
    }

    public int getReason() {
        return reason;
    }

    public String getCreated() {
        return created;
    }

    public short getViewed() {
        return viewed;
    }

    public boolean isViewed() {
        return viewed == 1;
    }

    public void setViewed(boolean viewed) {
        this.viewed = (short)(viewed?1:0);
    }

    public int getReasonInt() {
        return (int) reason;
    }
    public static class AlertJson{
        public static final Type LIST_TYPE = new TypeToken<ArrayList<AlertJson>>() {
        }.getType();

        public static final Type SET_TYPE = new TypeToken<HashSet<AlertJson>>() {
        }.getType();

        public long a_id;
        public long s_id;
        public short viewed;
        public AlertJson(Alert alert){
            if (alert==null)
                return;
            a_id = alert.getAlertId();
            s_id = alert.getSourceId();
            viewed = (short)(alert.isViewed()?1:0);
        }
    }
}