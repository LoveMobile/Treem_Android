package com.treem.treem.services.Treem;

import com.github.scribejava.core.model.Verb;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Matthew Walker on 2/26/16.
 * Copyright (c) 2016 Treem LLC. All rights reserved.
 */
public class TreemAuthorizationService {
    public static void authorizeApp(TreemServiceRequest request, String answerId, String questionId) {
        request.url     = TreemService.AUTHORIZATION_SERVICE_URL;
        request.method  = Verb.POST;

        // set the body data
        JSONObject json = new JSONObject();

        try {
            json.put("a_id", answerId);
            json.put("id", questionId);
        }
        catch(JSONException e) {
            // json parsing exception
        }

        request.bodyData = json;

        TreemService.SHARED_INSTANCE.performRequest(request);
    }

    public static void getChallengeQuestion(TreemServiceRequest request) {
        request.url     = TreemService.AUTHORIZATION_SERVICE_URL;
        request.method  = Verb.GET;

        TreemService.SHARED_INSTANCE.performRequest(request);
    }
}
