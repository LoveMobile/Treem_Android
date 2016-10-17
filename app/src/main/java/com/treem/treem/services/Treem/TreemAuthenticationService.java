package com.treem.treem.services.Treem;

import android.os.Build;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;

import com.github.scribejava.core.model.Verb;
import com.treem.treem.models.session.TreeSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Matthew Walker on 2/26/16.
 * Copyright (c) 2016 Treem LLC. All rights reserved.
 */
public class TreemAuthenticationService {
    private static final String keyUserName="username";
    private static final String keyFirstName="first";
    private static final String keyLastName="last";
    private static final String keyEmail="email";
    private static final String keyDateOfBirth="dob";

    private static final String pathCheckUserName = "username";
    private static final String pathLogout = "logout";

    public static void checkPhoneNumber(TreemServiceRequest request, String phone) {
        request.url     = TreemService.AUTHENTICATION_SERVICE_URL + "check";
        request.method  = Verb.POST;

        // set the body data
        JSONObject json = new JSONObject();

        try {
            json.put("phone", phone);
        }
        catch(JSONException e) {
            // json parsing exception
        }

        request.bodyData = json;

        TreemService.SHARED_INSTANCE.performRequest(request);
    }

    public static void resendVerificationCode(TreemServiceRequest request, String phone) {
        request.url     = TreemService.AUTHENTICATION_SERVICE_URL + "resend";
        request.method  = Verb.POST;

        // set the body data
        JSONObject json = new JSONObject();

        try {
            json.put("phone", phone);
        }
        catch(JSONException e) {
            // json parsing exception
        }

        request.bodyData = json;

        TreemService.SHARED_INSTANCE.performRequest(request);
    }

    public static void verifyUserDevice(TreemServiceRequest request, String phone, String code) {
        request.url     = TreemService.AUTHENTICATION_SERVICE_URL + "verify";
        request.method  = Verb.POST;

        // set the body data
        JSONObject json = new JSONObject();

        try {
            json.put("phone", phone);
            json.put("signup_code", code);
            json.put("device_os", "android");
            json.put("device_name", Build.DEVICE + " (" + Build.MODEL + ")");
            json.put("device_guid", Secure.ANDROID_ID);
            json.put("device_form_factor", Build.MODEL);
        }
        catch(JSONException e) {
            // json parsing exception
        }

        request.bodyData = json;

        TreemService.SHARED_INSTANCE.performRequest(request);
    }

    /**
     * Perform check user name exists request
     * @param request the request values
     * @param userName the user name to check
     */
    public static TreemService.NetworkRequestTask checkUserName(TreemServiceRequest request, String userName){
        //Encode phone number to url encoded string
        String encodedUserName  =userName;
        try{
            encodedUserName = URLEncoder.encode(userName,"UTF-8");
        }
        catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        request.url     = TreemService.AUTHENTICATION_SERVICE_URL + pathCheckUserName +"/"+encodedUserName;
        request.method  = Verb.GET;
        return TreemService.SHARED_INSTANCE.performRequest(request);
    }


    /**
     * Register user request
     * @param request request values
     * @param userName user name
     * @param firstName user's first name
     * @param lastName user's last name
     * @param email user's email
     * @param dateOfBirth user's birthday
     */
    public static void registerUser(@NonNull TreemServiceRequest request,
                                    @NonNull String userName,
                                    @NonNull String firstName,
                                    @NonNull String lastName,
                                    @NonNull String email,
                                    @NonNull String dateOfBirth) {
        request.url     = TreemService.AUTHENTICATION_SERVICE_URL;
        request.method  = Verb.POST;

        // set the body data
        JSONObject json = new JSONObject();

        try {
            json.put(keyUserName, userName);
            json.put(keyFirstName, firstName);
            json.put(keyLastName, lastName);
            json.put(keyEmail, email);
            json.put(keyDateOfBirth, dateOfBirth);
        }
        catch(JSONException e) {
            // json parsing exception
        }

        request.bodyData = json;

        TreemService.SHARED_INSTANCE.performRequest(request);
    }

    public static void checkPinCode(@NonNull TreeSession treeSession, @NonNull TreemServiceRequest request, @NonNull String pinCode) {
		request.url     		  	= TreemService.CHECK_PIN_URL;
		request.method  			= Verb.POST;
        request.HTTPCustomHeaders 	= treeSession.getTreemServiceHeader();

		JSONObject json = new JSONObject();

		try {
			json.put("pin", pinCode);
		} catch (JSONException e) {
			// json parsing exception
		}

		request.bodyData = json;

		TreemService.SHARED_INSTANCE.performRequest(request);
	}

    public static void setPinCode(@NonNull TreeSession treeSession,
                                  @NonNull TreemServiceRequest request,
                                  @NonNull String pinCode,
                                  @NonNull String currentPinCode) {
        request.url     		  	= TreemService.SET_PIN_URL;
        request.method  			= Verb.POST;
        request.HTTPCustomHeaders 	= treeSession.getTreemServiceHeader();

        JSONObject json = new JSONObject();

        try {
            json.put("pin", pinCode);

            if (currentPinCode != null && !currentPinCode.isEmpty()) {
                json.put("existing_pin", currentPinCode);
            }
        } catch (JSONException e) {
            // json parsing exception
        }

        request.bodyData = json;

        TreemService.SHARED_INSTANCE.performRequest(request);
    }

    public static void logout(TreemServiceRequest request){
        request.url = TreemService.AUTHENTICATION_SERVICE_URL+pathLogout;
        request.method = Verb.DELETE;

        TreemService.SHARED_INSTANCE.performRequest(request);
    }
}
