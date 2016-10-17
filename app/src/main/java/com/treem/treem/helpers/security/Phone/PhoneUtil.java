package com.treem.treem.helpers.security.Phone;

import android.content.res.Resources;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.treem.treem.application.ApplicationPreferencesManager;

/**
 * Created by Matthew Walker on 2/24/16.
 * Copyright (c) 2016 Treem LLC. All rights reserved.
 */
public class PhoneUtil {
    private static String keyPhoneNumber = "phoneNumber";

    // Return phone number formatted in e164 international standard
    // If format unsuccessful, null is returned
    public static String getE164FormattedString(String phone) {
        if (phone==null)
            return null;
        String formattedString = null;

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        try {
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phone, Resources.getSystem().getConfiguration().locale.getCountry());

            // if phone number valid
            if (phoneUtil.isValidNumber(numberProto)) {
                formattedString = phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164);
            }
        }
        catch (Exception e) {
            // do nothing (assume invalid number)
        }

        if ((formattedString != null) && (formattedString.isEmpty())) {
            formattedString = null;
        }

        return formattedString;
    }

    /**
     * Save phone number to settings
     * @param phone the phone number
     */
    public static void savePhone(String phone) {
        ApplicationPreferencesManager.SHARED_INSTANCE.setValue(keyPhoneNumber,phone);
    }

    /**
     * Get phone number from settings
     * @return phone number
     */
    public static String getPhone(){
        return ApplicationPreferencesManager.SHARED_INSTANCE.getValue(keyPhoneNumber);
    }
}
