package com.treem.treem.services.Treem;

import android.content.Context;

import com.treem.treem.R;

import java.util.EnumSet;
import java.util.HashMap;

/**
 * Created by Matthew Walker on 2/17/16.
 * Copyright (c) 2016 Treem LLC. All rights reserved.
 */

public enum TreemServiceResponseCode {
    INTERNAL_SERVER_ERROR(-2),
    NETWORK_ERROR(-1),
    GENERIC_RESPONSE_CODE_1(1),
    GENERIC_RESPONSE_CODE_2(2),
    GENERIC_RESPONSE_CODE_3(3),
    GENERIC_RESPONSE_CODE_4(4),
    GENERIC_RESPONSE_CODE_5(5),
    GENERIC_RESPONSE_CODE_6(6),
    GENERIC_RESPONSE_CODE_7(7),
    GENERIC_RESPONSE_CODE_8(8),
    GENERIC_RESPONSE_CODE_9(9),
    GENERIC_RESPONSE_CODE_10(10),
    SUCCESS(0),
    DISABLED_CONSUMER_KEY(90),
    DISABLED_OAUTH_TOKEN(91),
    INVALID_ACCESS_TOKEN(92),
    INVALID_HEADER(93),
    INVALID_SIGNATURE_METHOD(94),
    REQUEST_WAS_USED(95),
    REQUEST_HAS_EXPIRED(96),
    INVALID_CONSUMER_KEY(97),
    INVALID_SIGNATURE(98),
    OTHER_ERROR(99),
    LOCKED_OUT(100),
    INVALID_SESSION(101),
    CANCELED(999);

    private static final HashMap<Integer, TreemServiceResponseCode> mappedOrdinals = new HashMap<>();

    static {
        for (TreemServiceResponseCode s : EnumSet.allOf(TreemServiceResponseCode.class))
            mappedOrdinals.put(s.getRawValue(), s);
    }

    private int rawValue;

    TreemServiceResponseCode(int rawValue) {
        this.rawValue = rawValue;
    }

    public int getRawValue() {
        return rawValue;
    }

    public static TreemServiceResponseCode get(int rawValue) {
        return mappedOrdinals.get(rawValue);
    }

    /**
     * Get response code description
     * @param context base context
     * @param code code to get description for
     * @return the code description
     */
    public static String getDescription(Context context, TreemServiceResponseCode code) {
        if (context==null)
            return null;
        if (code==null)
            return context.getString(R.string.error_general_message);
        switch (code) {
            case SUCCESS:
                return context.getString(R.string.treem_response_code_success);
            case DISABLED_CONSUMER_KEY:
                return context.getString(R.string.treem_response_code_disabled_consumer_key);
            case DISABLED_OAUTH_TOKEN:
                return context.getString(R.string.treem_response_code_disabled_oauth_token);
            case INVALID_ACCESS_TOKEN:
                return context.getString(R.string.treem_response_code_invalid_user_token_passed);
            case INVALID_HEADER:
                return context.getString(R.string.treem_response_code_invalid_header);
            case INVALID_SIGNATURE_METHOD:
                return context.getString(R.string.treem_response_code_invalid_signature_method);
            case REQUEST_WAS_USED:
                return context.getString(R.string.treem_response_code_request_was_used);
            case REQUEST_HAS_EXPIRED:
                return context.getString(R.string.treem_response_code_request_has_expired);
            case INVALID_CONSUMER_KEY:
                return context.getString(R.string.treem_response_code_invalid_consumer_key);
            case INVALID_SIGNATURE:
                return context.getString(R.string.treem_response_code_invalid_signature);
            case OTHER_ERROR:
                return context.getString(R.string.treem_response_code_other_error);
            case LOCKED_OUT:
                return context.getString(R.string.treem_response_code_locked_out);
            case INVALID_SESSION:
                return context.getString(R.string.treem_response_code_invalid_session);
            case NETWORK_ERROR:
                return context.getString(R.string.treem_response_code_network_error);
            case CANCELED:
                return context.getString(R.string.treem_response_request_canceled);
            case INTERNAL_SERVER_ERROR:
                return context.getString(R.string.error_general_message);
            default:
                return context.getString(R.string.treem_response_code_general,code.getRawValue());

        }
    }

    /**
     * Get response code description
     * @param context base context
     * @return code description
     */
    public String getDescription(Context context) {
        return getDescription(context,this);
    }

    /**
     * Check is session expired
     * @return true if session expired
     */
    public boolean isSessionExpired(){
        return this==DISABLED_OAUTH_TOKEN||this==INVALID_ACCESS_TOKEN||isConsumerKeyExpired();
    }

    /**
     * Check is consumer key expired
     * @return true if consumer key expired
     */
    public boolean isConsumerKeyExpired(){
        return this==INVALID_CONSUMER_KEY||this==DISABLED_CONSUMER_KEY;
    }
}
