package com.treem.treem.services.Treem;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.DefaultApi10a;
import com.github.scribejava.core.exceptions.OAuthConnectionException;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.google.gson.Gson;
import com.treem.treem.application.ApplicationMain;
import com.treem.treem.helpers.LogHelper;
import com.treem.treem.services.oauth.AdvancedOAuthRequest;

import java.io.UnsupportedEncodingException;

/**
 * Created by Matthew Walker on 2/12/16.
 * Copyright (c) 2016 Treem LLC. All rights reserved.
 */
public class TreemService extends DefaultApi10a {
    public final static TreemService SHARED_INSTANCE = new TreemService();

    private final static String DEBUG_TAG = "TREEM_SERVICE_CALL";

    // main uri
    private static final String BASE_SERVICE_URL            = "https://" + ApplicationMain.TREEM_SERVICE_DOMAIN + "/";

    // service uris
    public static final String ALERTS_SERVICE_URL          = "https://alerts." + ApplicationMain.TREEM_SERVICE_DOMAIN + "/";
    public static final String AUTHENTICATION_SERVICE_URL  = "https://authentication." + ApplicationMain.TREEM_SERVICE_DOMAIN + "/";
    public static final String CHECK_PIN_URL               = AUTHENTICATION_SERVICE_URL + "checkpin";
    public static final String SET_PIN_URL                 = AUTHENTICATION_SERVICE_URL + "setpin";
    public static final String AUTHORIZATION_SERVICE_URL   = "https://authorization." + ApplicationMain.TREEM_SERVICE_DOMAIN + "/";
    public static final String BRANCH_SERVICE_URL          = "https://branches." + ApplicationMain.TREEM_SERVICE_DOMAIN + "/";
    public static final String CHAT_SERVICE_URL            = "https://chat." + ApplicationMain.TREEM_SERVICE_DOMAIN + "/";
    public static final String CONTENT_SERVICE_URL         = "https://content." + ApplicationMain.TREEM_SERVICE_DOMAIN + "/";
    public static final String EQUITY_SERVICE_URL          = "https://equity." + ApplicationMain.TREEM_SERVICE_DOMAIN + "/";
    public static final String FEED_SERVICE_URL            = "https://feed." + ApplicationMain.TREEM_SERVICE_DOMAIN + "/";
    public static final String PROFILE_SERVICE_URL         = "https://profile." + ApplicationMain.TREEM_SERVICE_DOMAIN + "/";
    public static final String SEEDING_SERVICE_URL         = "https://seeding." + ApplicationMain.TREEM_SERVICE_DOMAIN + "/";
    public static final String PUBLIC_SERVICE_URL          = "https://public." + ApplicationMain.TREEM_SERVICE_DOMAIN + "/";
    public static final String searchOptionLimit           ="limit";
    public static final String searchScale                  = "scale";
    public static final String searchStartDate              = "start_date";
    public static final String searchEndDate                = "end_date";
    public static final String scaleDay                     = "day";
    public static final String searchReason                 = "reason";
    public static final String paginationPage               = "page";
    public static final String paginationPageSize           = "pagesize";
    public static final String paramFDate                   = "f_date";


    private TreemService() {
        // Prevent direct instantiation
    }

    // return true if response code checks out
    private static boolean isValidHTTPResponse(int response) {
        return (response >= 200 && response < 300);
    }

    @Override
    public String getAccessTokenEndpoint() {
        return BASE_SERVICE_URL;
    }

    @Override
    public String getRequestTokenEndpoint() {
        return BASE_SERVICE_URL;
    }

    @Override
    public String getAuthorizationUrl(Token requestToken) {
        return BASE_SERVICE_URL;
    }

    // public method to perform a service request
    public NetworkRequestTask performRequest(TreemServiceRequest treemServiceRequest) {
        // execute the asynchronous network request task
        NetworkRequestTask task = new NetworkRequestTask(treemServiceRequest);
        task.execute();
        return task;
    }

    // asynchronous task that actually performs the network request
    public static class NetworkRequestTask extends AsyncTask<String, Integer, String> {
        // options for creating the request
        private TreemServiceRequest treemServiceRequest;

        // request response status variables
        private TreemServiceResponseCode responseCode;
        private String data;
        private boolean wasHandled;

        // constructor
        NetworkRequestTask(TreemServiceRequest treemRequest) {
            this.treemServiceRequest = treemRequest;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {


            Pair<String,String> keys = TreemOAuthConsumerToken.SHARED_INSTANCE.getConsumerTokens();

            // get service with consumer keys
            OAuth10aService service = new ServiceBuilder().apiKey(keys.first).apiSecret(keys.second).build(TreemService.SHARED_INSTANCE);

            OAuthRequest request = new AdvancedOAuthRequest(treemServiceRequest.method, treemServiceRequest.url, service);

            //Get user's tookens
            Pair<String,String> userTokens = TreemOAuthUserToken.SHARED_INSTANCE.getUserTokens();

            Token requestToken;
            if (userTokens!=null) //create request token from user tokens if exists
                requestToken = new Token(userTokens.first,userTokens.second);
            else //or create empty request token
                requestToken = new Token("", ""); // leave as empty string so that token is still signed using plugin method

            // append Accept-Language to header
            request.addHeader("Accept-Language", Resources.getSystem().getConfiguration().locale.getLanguage());

            /**
             * Update headers with custom headers from request
             */
            if (treemServiceRequest.HTTPCustomHeaders!=null){
                for (Object headerName:treemServiceRequest.HTTPCustomHeaders.keySet()){
                    Object headerValue = treemServiceRequest.HTTPCustomHeaders.get(headerName);
                    if (headerName!=null&&headerName instanceof String &&headerValue!=null&&headerValue instanceof String)
                        request.addHeader((String)headerName,(String)headerValue);
                }
            }

            /*
            Add search options to the query string
             */
            if (treemServiceRequest.searchOptions!=null){
                for (String key:treemServiceRequest.searchOptions.keySet()){
                    request.addQuerystringParameter(key,treemServiceRequest.searchOptions.get(key));
                }
            }
            // if POST method, check for JSON body
            if ((this.treemServiceRequest.method == Verb.POST||
                    this.treemServiceRequest.method == Verb.PUT||
                    this.treemServiceRequest.method == Verb.DELETE) && (this.treemServiceRequest.bodyData != null)) {

                request.addBodyParameter("d", this.treemServiceRequest.bodyData.toString());
            }

            // sign the request
            service.signRequest(requestToken, request);

            // Log the network call prior to sending
            Log.d(DEBUG_TAG, "# HTTP " + this.treemServiceRequest.method.toString() + " Request");
            Log.d(DEBUG_TAG, request.getCompleteUrl());
            Log.d(DEBUG_TAG, "Headers:");
            Log.d(DEBUG_TAG, request.getHeaders().toString());
            Log.d(DEBUG_TAG, "Body parameters:");

            if (ApplicationMain.SHARED_INSTANCE.DEBUG) {
                try {
                    LogHelper.d(DEBUG_TAG, java.net.URLDecoder.decode(request.getBodyContents().toString(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            Response response = null;
            try {
                response = request.send();
            }
            catch (OAuthConnectionException e){
                e.printStackTrace();
                responseCode = TreemServiceResponseCode.NETWORK_ERROR;
                return null;
            }


            int httpResponseCode = response.getCode();

            Log.d(DEBUG_TAG, "Service Network Response Code:" + Integer.toString(httpResponseCode));

            // check http response code
            if (isValidHTTPResponse(httpResponseCode)) {
                // parse response to general response object
                final Gson gson = new Gson();
                LogHelper.d(DEBUG_TAG,"Response body: "+response.getBody());
                if (response.getBody()!=null) {
                    TreemServiceResponse serviceResponse = gson.fromJson(response.getBody(), TreemServiceResponse.class);
                    TreemServiceResponseCode responseCode = serviceResponse.getResponseCode();

                    LogHelper.d(DEBUG_TAG, serviceResponse.toString());

                    this.responseCode = responseCode;

                    // check for success response code from service
                    if (responseCode == TreemServiceResponseCode.SUCCESS) {
                        this.data = gson.toJson(serviceResponse.data);
                    }
                }
                else{
                    responseCode = TreemServiceResponseCode.SUCCESS;
                    this.data = null;
                }
            }
            else {
                // assign Treem response code based on http response code
                this.responseCode = (httpResponseCode == 500) ? TreemServiceResponseCode.INTERNAL_SERVER_ERROR : TreemServiceResponseCode.NETWORK_ERROR;
            }

            return "";
        }

        @Override
        // onPostExecute is called on UI thread
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (this.responseCode == TreemServiceResponseCode.SUCCESS) {
                treemServiceRequest.onSuccess(this.data);
            }
            else {
                if (responseCode.isSessionExpired()){
                    ApplicationMain.SHARED_INSTANCE.userTokenExpired(responseCode.isConsumerKeyExpired());
                    wasHandled = true;
                }
                treemServiceRequest.onFailure(this.responseCode, this.wasHandled);
            }
        }

        @Override
        protected void onCancelled() {
            if (treemServiceRequest!=null)
                treemServiceRequest.onFailure(TreemServiceResponseCode.CANCELED,false);
        }
    }
}
