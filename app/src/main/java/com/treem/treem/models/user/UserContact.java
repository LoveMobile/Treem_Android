package com.treem.treem.models.user;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.google.gson.annotations.SerializedName;
import com.treem.treem.helpers.security.Phone.PhoneUtil;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class UserContact {
    @SerializedName("c_id")
    public Long contactId;
    @SerializedName("first")
    public String firstName;
    @SerializedName("last")
    public String lastName;
    @SerializedName("email_arr")
    public List<String> emails;
    @SerializedName("phone_arr")
    public List<String> phones;

    public transient Long phoneContactId;
    private String mName;

    public UserContact(){}
    public UserContact(String firstName, String lastName, String phone, List<String> emails){
        this.firstName = firstName;
        this.lastName = lastName;
        if (phone!=null){
            phones = new ArrayList<>();
            phones.add(phone);
        }
        this.emails = emails;
    }
    public static UserContact importFromContacts(Context context, long contactId){
        UserContact c = new UserContact();
        c.phoneContactId = contactId;
        c.getDataForContact(context, contactId);
        return c;
    }

    private void getDataForContact(Context context, long user_contact_id) {
        ContentResolver localContentResolver = context.getContentResolver();
        fillName(localContentResolver, user_contact_id);
        fillEmail(localContentResolver, user_contact_id);
        fillPhones(localContentResolver, user_contact_id);
    }
    private void fillEmail(ContentResolver localContentResolver, long user_contact_id) {
        Cursor emailCursor = localContentResolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                new String[]{Long.toString(user_contact_id)}, null);
        if (emailCursor != null) {
            try {
                if (emailCursor.moveToFirst()) {
                    emails = new ArrayList<>();
                    while (!emailCursor.isAfterLast()) {
                        String email = emailCursor.getString(
                                emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        emails.add(email);

                        emailCursor.moveToNext();
                    }
                }
            } finally {
                emailCursor.close();
            }
            if (emails!=null&&emails.size()==0)
                emails = null;
        }
    }

    private void fillPhones(ContentResolver localContentResolver, long user_contact_id) {
        Cursor phonesCursor = localContentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{Long.toString(user_contact_id)}, null);
        if (phonesCursor != null) {
            try {
                phones = new ArrayList<>();
                while (phonesCursor.moveToNext()) {
                    String number = phonesCursor.getString(phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String phone = PhoneUtil.getE164FormattedString(number);
                    if (phone!=null)
                        phones.add(phone);
                }
            } finally {
                phonesCursor.close();
            }
        }
        if (phones!=null&&phones.size()==0)
            phones = null;
    }

    private void fillName(ContentResolver localContentResolver, long user_contact_id) {
        Cursor nameCursor = localContentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                null,
                ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
                new String[]{Long.toString(user_contact_id), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE}, null);
        if (nameCursor != null) {
            try {
                if (nameCursor.moveToFirst()) {
                    String givenName = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
                    String familyName = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
                    String displayName = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME));
                    if (givenName != null || familyName != null) {
                        firstName = givenName;
                        lastName = familyName;
                    } else {
                        firstName = displayName;
                    }
                }
            } finally {
                nameCursor.close();
            }
        }

    }

    public String getName() {
        String name = firstName==null?"":firstName;
        if (lastName!=null){
            if (name.length()!=0)
                name+=" ";
            name+=lastName;
        }
        return name;
    }

    public String getPhone() {
        return phones != null && phones.size() > 0 ? phones.get(0) : null;
    }
}
